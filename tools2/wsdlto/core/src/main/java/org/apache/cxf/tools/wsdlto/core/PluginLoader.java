/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.tools.wsdlto.core;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.tools.common.FrontEndGenerator;
import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.plugin.DataBinding;
import org.apache.cxf.tools.plugin.FrontEnd;
import org.apache.cxf.tools.plugin.Generator;
import org.apache.cxf.tools.plugin.Plugin;

public final class PluginLoader {
    public static final Logger LOG = LogUtils.getL7dLogger(PluginLoader.class);
    private static PluginLoader pluginLoader;
    private static final String DEFAULT_PLUGIN = "/org/apache/cxf/tools/wsdlto/core/cxf-tools-plugin.xml";
    
    private Map<String, Plugin> plugins = new LinkedHashMap<String, Plugin>();

    private Map<String, FrontEnd> frontends = new LinkedHashMap<String, FrontEnd>();
    private Map<String, FrontEndProfile> frontendProfiles = new LinkedHashMap<String, FrontEndProfile>();
    
    private Map<String, DataBinding> databindings = new LinkedHashMap<String, DataBinding>();
    private Map<String, DataBindingProfile> databindingProfiles
        = new LinkedHashMap<String, DataBindingProfile>();

    private Unmarshaller unmarshaller;
    
    private PluginLoader() {
        try {
            JAXBContext jc = JAXBContext.newInstance("org.apache.cxf.tools.plugin");
            unmarshaller = jc.createUnmarshaller();
        } catch (JAXBException e) {
            LOG.log(Level.SEVERE, "JAXB_CONTEXT_INIT_FAIL");
            Message msg = new Message("JAXB_CONTEXT_INIT_FAIL", LOG);
            throw new ToolException(msg);            
        }

        loadPlugin(DEFAULT_PLUGIN);
    }

    public static PluginLoader getInstance() {
        if (pluginLoader == null) {
            pluginLoader = new PluginLoader();
        }
        return pluginLoader;
    }

    public void loadPlugin(String resource) {
        try {
            LOG.log(Level.INFO, "PLUGIN_LOADING", resource);
            loadPlugin(getPlugin(resource));
        } catch (JAXBException e) {
            LOG.log(Level.SEVERE, "PLUGIN_LOAD_FAIL", resource);
            Message msg = new Message("PLUGIN_LOAD_FAIL", LOG, resource);
            throw new ToolException(msg, e);
        }
    }
    
    protected void loadPlugin(Plugin plugin) {
        LOG.log(Level.INFO, "FOUND_FRONTENDS", new Object[]{plugin.getName(), plugin.getFrontend().size()});
        for (FrontEnd frontend : plugin.getFrontend()) {
            LOG.log(Level.INFO, "LOADING_FRONTEND", new Object[]{frontend.getName(), plugin.getName()});
            if (StringUtils.isEmpty(frontend.getName())) {
                LOG.log(Level.WARNING, "FRONTEND_MISSING_NAME", plugin.getName());
                continue;
            }
            frontends.put(frontend.getName(), frontend);
        }
        
        LOG.log(Level.INFO, "FOUND_DATABINDINGS", new Object[]{plugin.getName(),
                                                               plugin.getDatabinding().size()});
        for (DataBinding databinding : plugin.getDatabinding()) {
            LOG.log(Level.INFO, "LOADING_DATABINDING", new Object[]{databinding.getName(), plugin.getName()});
            if (StringUtils.isEmpty(databinding.getName())) {
                LOG.log(Level.WARNING, "DATABINDING_MISSING_NAME", plugin.getName());
                continue;
            }
            databindings.put(databinding.getName(), databinding);
        }
    }

    protected Plugin getPlugin(String resource) throws JAXBException {
        Plugin plugin = plugins.get(resource);
        if (plugin == null) {
            InputStream is = getClass().getResourceAsStream(resource);
            if (is == null) {
                LOG.log(Level.SEVERE, "PLUGIN_MISSING", resource);
                Message msg = new Message("PLUGIN_MISSING", LOG, resource);
                throw new ToolException(msg);
            }
            plugin = getPlugin(is);
            if (plugin == null || StringUtils.isEmpty(plugin.getName())) {
                LOG.log(Level.SEVERE, "PLUGIN_LOAD_FAIL", resource);
                Message msg = new Message("PLUGIN_LOAD_FAIL", LOG, resource);
                throw new ToolException(msg);
            }
            plugins.put(resource, plugin);
        }
        return plugin;
    }

    private Plugin getPlugin(InputStream is) throws JAXBException {
        // TODO: schema validation
        return (Plugin) ((JAXBElement<?>)unmarshaller.unmarshal(is)).getValue();
    }

    public FrontEnd getFrontEnd(String name) {
        FrontEnd frontend = frontends.get(name);
        if (frontend == null) {
            // TODO: If null, we should search the whole classpath, to load all the plugins,
            //       otherwise throws Exception
            Message msg = new Message("FRONTEND_MISSING", LOG, name);
            throw new ToolException(msg);
        }
        return frontend;
    }

    private String getGeneratorClass(FrontEnd frontend, Generator generator) {
        String fullPackage = generator.getPackage();
        if (StringUtils.isEmpty(fullPackage)) {
            fullPackage = frontend.getGenerators().getPackage();
        }
        if (StringUtils.isEmpty(fullPackage)) {
            fullPackage = frontend.getPackage();
        }
        return fullPackage + "." + generator.getName();
    }
    
    private List<FrontEndGenerator> getFrontEndGenerators(FrontEnd frontend) {
        List<FrontEndGenerator> generators = new ArrayList<FrontEndGenerator>();

        String fullClzName = null;
        try {
            for (Generator generator : frontend.getGenerators().getGenerator()) {
                fullClzName = getGeneratorClass(frontend, generator);
                generators.add((FrontEndGenerator)Class.forName(fullClzName).newInstance());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FRONTEND_PROFILE_LOAD_FAIL", fullClzName);
            Message msg = new Message("FRONTEND_PROFILE_LOAD_FAIL", LOG, fullClzName);
            throw new ToolException(msg, e);
        }
        return generators;
    }

    private FrontEndProfile loadFrontEndProfile(String fullClzName) {
        FrontEndProfile profile = null;
        try {
            profile = (FrontEndProfile) Class.forName(fullClzName).newInstance();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "FRONTEND_PROFILE_LOAD_FAIL", fullClzName);
            Message msg = new Message("FRONTEND_PROFILE_LOAD_FAIL", LOG, fullClzName);
            throw new ToolException(msg, e);
        }
        return profile;
    }

    private Processor loadProcessor(String fullClzName) {
        Processor processor = null;
        try {
            processor = (Processor) Class.forName(fullClzName).newInstance();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "LOAD_PROCESSOR_FAILED", fullClzName);
            Message msg = new Message("LOAD_PROCESSOR_FAILED", LOG, fullClzName);
            throw new ToolException(msg, e);
        }
        return processor;
    }

    private Class loadContainerClass(String fullClzName) {
        Class clz = null;
        try {
            clz = Class.forName(fullClzName);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "LOAD_CONTAINER_CLASS_FAILED", fullClzName);
            Message msg = new Message("LOAD_CONTAINER_CLASS_FAILED", LOG, fullClzName);
            throw new ToolException(msg, e);
        }
        return clz;
    }

    private String getFrontEndProfileClass(FrontEnd frontend) {
        if (StringUtils.isEmpty(frontend.getProfile())) {
            return "org.apache.cxf.tools.wsdlto.core.FrontEndProfile";
        }
        return frontend.getPackage() + "." + frontend.getProfile();
    }

    private String getProcessorClass(FrontEnd frontend) {
        String pkgName = frontend.getProcessor().getPackage();
        if (StringUtils.isEmpty(pkgName)) {
            pkgName = frontend.getPackage();
        }
        return pkgName + "." + frontend.getProcessor().getName();
    }

    private String getContainerClass(FrontEnd frontend) {
        return getContainerPackage(frontend) + "." + frontend.getContainer().getName();
    }
    
    private String getContainerPackage(FrontEnd frontend) {
        String pkgName = frontend.getContainer().getPackage();
        if (StringUtils.isEmpty(pkgName)) {
            pkgName = frontend.getPackage();
        }
        return pkgName;
    }

    private String getToolspec(FrontEnd frontend) {
        String toolspec = frontend.getContainer().getToolspec();
        return "/" + getContainerPackage(frontend).replace(".", "/") + "/" + toolspec;
    }

    private AbstractWSDLBuilder<? extends Object> loadBuilder(String fullClzName) {
        AbstractWSDLBuilder<? extends Object> builder = null;
        try {
            builder = (AbstractWSDLBuilder<? extends Object>) Class.forName(fullClzName).newInstance();
            
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "LOAD_PROCESSOR_FAILED", fullClzName);
            Message msg = new Message("LOAD_PROCESSOR_FAILED", LOG, fullClzName);
            throw new ToolException(msg, e);
        }
        return builder;
    }

    private String getBuilderClass(FrontEnd frontend) {
        String pkgName = frontend.getBuilder().getPackage();
        if (StringUtils.isEmpty(pkgName)) {
            pkgName = frontend.getPackage();
        }
        return pkgName + "." + frontend.getBuilder().getName();
    }

    public FrontEndProfile getFrontEndProfile(String name) {
        FrontEndProfile profile = frontendProfiles.get(name);
        if (profile == null) {
            FrontEnd frontend = getFrontEnd(name);
            profile = loadFrontEndProfile(getFrontEndProfileClass(frontend));

            for (FrontEndGenerator generator : getFrontEndGenerators(frontend)) {
                profile.registerGenerator(generator);
            }
            
            if (frontend.getProcessor() != null) {
                profile.setProcessor(loadProcessor(getProcessorClass(frontend)));
            }
            if (frontend.getContainer() != null) {
                profile.setContainerClass(loadContainerClass(getContainerClass(frontend)));
                profile.setToolspec(getToolspec(frontend));
            }
            if (frontend.getBuilder() != null) {
                profile.setWSDLBuilder(loadBuilder(getBuilderClass(frontend)));
            }
            
            
            frontendProfiles.put(name, profile);
        }
        return profile;
    }

    public DataBinding getDataBinding(String name) {
        DataBinding databinding = databindings.get(name);
        if (databinding == null) {
            // TODO: If null, we should search the whole classpath, to load all the plugins,
            //       otherwise throws Exception
            Message msg = new Message("DATABINDING_MISSING", LOG, name);
            throw new ToolException(msg);            
        }
        return databinding;
    }

    private DataBindingProfile loadDataBindingProfile(String fullClzName) {
        DataBindingProfile profile = null;
        try {
            profile = (DataBindingProfile) Class.forName(fullClzName).newInstance();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "DATABINDING_PROFILE_LOAD_FAIL", fullClzName);
            Message msg = new Message("DATABINDING_PROFILE_LOAD_FAIL", LOG, fullClzName);
            throw new ToolException(msg);                
        }
        return profile;
    }
    
    public DataBindingProfile getDataBindingProfile(String name) {
        DataBindingProfile profile = databindingProfiles.get(name);
        if (profile == null) {
            DataBinding databinding = getDataBinding(name);
            profile = loadDataBindingProfile(databinding.getPackage() + "." + databinding.getProfile());
            databindingProfiles.put(name, profile);
        }
        return profile;
    }

    public Plugin findPlugin() {
        // TODO: find and load the plugin from the classpath
        return null;
    }

    public Map<String, FrontEnd> getFrontEnds() {
        return this.frontends;
    }

    public Map<String, DataBinding> getDataBindings() {
        return this.databindings;
    }

    public Map<String, Plugin> getPlugins() {
        return this.plugins;
    }
}
