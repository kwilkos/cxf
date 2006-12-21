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

package org.apache.cxf.tools.wsdlto;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.wsdl.Definition;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactoryHelper;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.tools.common.AbstractCXFToolContainer;
import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.toolspec.ToolSpec;
import org.apache.cxf.tools.common.toolspec.parser.BadUsageException;
import org.apache.cxf.tools.common.toolspec.parser.CommandDocument;
import org.apache.cxf.tools.common.toolspec.parser.ErrorVisitor;
import org.apache.cxf.tools.wsdlto.core.AbstractWSDLBuilder;
import org.apache.cxf.tools.wsdlto.core.FrontEndProfile;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;

public class WSDLToJavaContainer extends AbstractCXFToolContainer {

    private static final String DEFAULT_NS2PACKAGE = "http://www.w3.org/2005/08/addressing";
    String toolName;

    public WSDLToJavaContainer(String name, ToolSpec toolspec) throws Exception {        
        super(name, toolspec);
        this.toolName = name;
    }

    protected Set<String> getArrayKeys() {
        Set<String> set = new HashSet<String>();
        set.add(ToolConstants.CFG_PACKAGENAME);
        set.add(ToolConstants.CFG_NEXCLUDE);
        return set;
    }

    private ToolConstants.WSDLVersion getWSDLVersion() {
        String version = (String) context.get(ToolConstants.CFG_WSDL_VERSION);
        return WSDLVersion.getVersion(version);
    }

    private Bus getBus() {
        return BusFactoryHelper.newInstance().getDefaultBus();
    }
    
    public void execute(boolean exitOnFinish) throws ToolException {
        try {
            super.execute(exitOnFinish);
            if (!hasInfoOption()) {
                buildToolContext();
                validate(context);
                
                FrontEndProfile frontend = context.get(FrontEndProfile.class);
                
                Processor processor = frontend.getProcessor();

                AbstractWSDLBuilder builder = frontend.getWSDLBuilder();
                
                ToolConstants.WSDLVersion version = getWSDLVersion();

                //List<ServiceInfo> services;
                String wsdlURL = (String)context.get(ToolConstants.CFG_WSDLURL);
                if (version == ToolConstants.WSDLVersion.WSDL11) {
                    Definition definition = (Definition) builder.build(wsdlURL);
                    
                    if (context.optionSet(ToolConstants.CFG_BINDING)) {
                        builder.setContext(context);
                        builder.customize();
                    }
                    WSDLServiceBuilder serviceBuilder = new WSDLServiceBuilder(getBus());
                    //services = 
                    serviceBuilder.buildService(definition);
                } else {
                    // TODO: wsdl2.0 support
                }
                
                processor.setEnvironment(context);
                // TODO: replace the wsdl4j with service model
                //processor.setServiceModel(services);
                processor.process();
            }
        } catch (ToolException ex) {
            if (ex.getCause() instanceof BadUsageException) {
                getInstance().printUsageException(toolName, (BadUsageException)ex.getCause());
            }
            System.err.println();
            if (isVerboseOn()) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            System.err.println("Error : " + ex.getMessage());
            System.err.println();
            if (isVerboseOn()) {
                ex.printStackTrace();
            }
        }
    }
    
    private void loadDefaultNSPackageMapping(ToolContext env) {
        if (!env.hasExcludeNamespace(DEFAULT_NS2PACKAGE) 
            && env.getBooleanValue(ToolConstants.CFG_DEFAULT_NS, "true")) {
            env.loadDefaultNS2Pck(getResourceAsStream("namespace2package.cfg"));
        }
        if (env.getBooleanValue(ToolConstants.CFG_DEFAULT_EX, "true")) {
            env.loadDefaultExcludes(getResourceAsStream("wsdltojavaexclude.cfg"));
        }
    }


    private void setExcludePackageAndNamespaces(ToolContext env) {
        if (env.get(ToolConstants.CFG_NEXCLUDE) != null) {
            String[] pns = (String[])env.get(ToolConstants.CFG_NEXCLUDE);
            for (int j = 0; j < pns.length; j++) {
                int pos = pns[j].indexOf("=");
                String excludePackagename = pns[j];
                if (pos != -1) {
                    String ns = pns[j].substring(0, pos);
                    excludePackagename = pns[j].substring(pos + 1);
                    env.addExcludeNamespacePackageMap(ns, excludePackagename);
                } else {
                    env.addExcludeNamespacePackageMap(pns[j], null);
                }
            }
        }
    }
    
    private void setPackageAndNamespaces(ToolContext env) {
        if (env.get(ToolConstants.CFG_PACKAGENAME) != null) {
            String[] pns = (String[])env.get(ToolConstants.CFG_PACKAGENAME);
            for (int j = 0; j < pns.length; j++) {
                int pos = pns[j].indexOf("=");
                String packagename = pns[j];
                if (pos != -1) {
                    String ns = pns[j].substring(0, pos);
                    packagename = pns[j].substring(pos + 1);
                    env.addNamespacePackageMap(ns, packagename);
                } else {
                    env.setPackageName(packagename);
                }
            }
        }
    }

    public void validate(ToolContext env) throws ToolException {
        String outdir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);
        if (outdir != null) {
            File dir = new File(outdir);
            if (!dir.exists()) {
                Message msg = new Message("DIRECTORY_NOT_EXIST", LOG, outdir);
                throw new ToolException(msg);
            }
            if (!dir.isDirectory()) {
                Message msg = new Message("NOT_A_DIRECTORY", LOG, outdir);
                throw new ToolException(msg);
            }
        }

        if (!env.optionSet(ToolConstants.CFG_EXTRA_SOAPHEADER)) {
            env.put(ToolConstants.CFG_EXTRA_SOAPHEADER, "false");
        }
        
        if (env.optionSet(ToolConstants.CFG_COMPILE)) {
            String clsdir = (String)env.get(ToolConstants.CFG_CLASSDIR);
            if (clsdir != null) {
                File dir = new File(clsdir);
                if (!dir.exists()) {
                    Message msg = new Message("DIRECTORY_NOT_EXIST", LOG, clsdir);
                    throw new ToolException(msg);
                }
            }
        }
    }

    protected void setAntProperties(ToolContext env) {
        String installDir = System.getProperty("install.dir");
        if (installDir != null) {
            env.put(ToolConstants.CFG_INSTALL_DIR, installDir);
        } else {
            env.put(ToolConstants.CFG_INSTALL_DIR, ".");
        }
    }

    protected void setLibraryReferences(ToolContext env) {
        Properties props = loadProperties(getResourceAsStream("wsdltojavalib.properties"));
        if (props != null) {
            for (Iterator keys = props.keySet().iterator(); keys.hasNext();) {
                String key = (String)keys.next();
                env.put(key, props.get(key));
            }
        }
        env.put(ToolConstants.CFG_ANT_PROP, props);
    }

    private void buildToolContext() {
        context = getContext();
        context.addParameters(getParametersMap(getArrayKeys()));

        if (context.get(ToolConstants.CFG_OUTPUTDIR) == null) {
            context.put(ToolConstants.CFG_OUTPUTDIR, ".");
        }
        
        if (context.containsKey(ToolConstants.CFG_ANT)) {
            setAntProperties(context);
            setLibraryReferences(context);
        }
        
        if (isVerboseOn()) {
            context.put(ToolConstants.CFG_VERBOSE, Boolean.TRUE);
        }

        //validate(env);
        setExcludePackageAndNamespaces(context);
        loadDefaultNSPackageMapping(context);
        setPackageAndNamespaces(context);
    }

    private static InputStream getResourceAsStream(String file) {
        return WSDLToJavaContainer.class.getResourceAsStream(file);
    }

    public void checkParams(ErrorVisitor errors) throws ToolException {
        CommandDocument doc = super.getCommandDocument();

        if (!doc.hasParameter("wsdlurl")) {
            errors.add(new ErrorVisitor.UserError("WSDL/SCHEMA URL has to be specified"));
        }
        if (errors.getErrors().size() > 0) {
            Message msg = new Message("PARAMETER_MISSING", LOG);
            throw new ToolException(msg, new BadUsageException(getUsage(), errors));
        }
    }    
    
}
