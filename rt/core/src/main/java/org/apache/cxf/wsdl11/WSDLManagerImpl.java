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

package org.apache.cxf.wsdl11;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.bind.JAXBException;

import org.w3c.dom.Element;

import org.apache.cxf.BusException;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.PropertiesLoaderUtils;
import org.apache.cxf.wsdl.JAXBExtensionHelper;
import org.apache.cxf.wsdl.WSDLManager;

/**
 * WSDLManagerImpl
 * 
 * @author dkulp
 */
public class WSDLManagerImpl implements WSDLManager {

    private static final Logger LOG = LogUtils
            .getL7dLogger(WSDLManagerImpl.class);
    private static final String EXTENSIONS_RESOURCE = "META-INF/extensions.xml";

    final ExtensionRegistry registry;

    final WSDLFactory factory;

    final WeakHashMap<Object, Definition> definitionsMap;
    
    public WSDLManagerImpl() throws BusException {
        try {
            factory = WSDLFactory.newInstance();
            registry = factory.newPopulatedExtensionRegistry();
        } catch (WSDLException e) {
            throw new BusException(e);
        }
        definitionsMap = new WeakHashMap<Object, Definition>();
        
        registerInitialExtensions();
    }

    public WSDLFactory getWSDLFactory() {
        return factory;
    }

    /*
     * (non-Javadoc)
     * 
     * XXX - getExtensionRegistry()
     *
     * @see org.apache.cxf.wsdl.WSDLManager#getExtenstionRegistry()
     */
    public ExtensionRegistry getExtenstionRegistry() {
        return registry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cxf.wsdl.WSDLManager#getDefinition(java.net.URL)
     */
    public Definition getDefinition(URL url) throws WSDLException {
        synchronized (definitionsMap) {
            if (definitionsMap.containsKey(url)) {
                return definitionsMap.get(url);
            }
        }
        Definition def = loadDefinition(url.toString());
        synchronized (definitionsMap) {
            definitionsMap.put(url, def);
        }
        return def;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cxf.wsdl.WSDLManager#getDefinition(java.lang.String)
     */
    public Definition getDefinition(String url) throws WSDLException {
        synchronized (definitionsMap) {
            if (definitionsMap.containsKey(url)) {
                return definitionsMap.get(url);
            }
        }
        return loadDefinition(url);
    }

    public Definition getDefinition(Element el) throws WSDLException {
        synchronized (definitionsMap) {
            if (definitionsMap.containsKey(el)) {
                return definitionsMap.get(el);
            }
        }
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        reader.setExtensionRegistry(registry);
        Definition def = reader.readWSDL(null, el);
        synchronized (definitionsMap) {
            definitionsMap.put(el, def);
        }
        return def;
    }

    public Definition getDefinition(Class<?> sei) throws WSDLException {
        
        if (null == sei.getAnnotation(WebService.class)) {
            return null;
        }
        
        synchronized (definitionsMap) {
            if (definitionsMap.containsKey(sei)) {
                return definitionsMap.get(sei);
            }
        }
        Definition def = null;
        try {
            def = createDefinition(sei);
        } catch (Exception ex) {            
            throw new WSDLException(WSDLException.PARSER_ERROR, ex.getMessage());
        }
        
        synchronized (definitionsMap) {
            definitionsMap.put(sei, def);
        }
        return def;
    }
    
    public void addDefinition(Object key, Definition wsdl) {
        synchronized (definitionsMap) {
            definitionsMap.put(key, wsdl);
        }
    }
    
    private Definition loadDefinition(String url) throws WSDLException {
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        reader.setExtensionRegistry(registry);
        Definition def = reader.readWSDL(url);
        synchronized (definitionsMap) {
            definitionsMap.put(url, def);
        }
        return def;
    }

    private Definition createDefinition(Class<?> sei) throws Exception {
        Definition definition = null;
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("createDefinition for class: " + sei.getName());
        }
        File tmp = null;
        try {
            tmp = File.createTempFile("tmp", ".wsdl");
            tmp.delete();
            tmp.mkdir();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "WSDL_GENERATION_TMP_DIR_MSG", ex);
            return null;
        }

        /*
         * JAXWSWsdlGenerator generator = new JAXWSWsdlGenerator(sei.getName(),
         * sei.getClassLoader()); Configuration config = new ToolConfig(new
         * String[] {"-wsdl", "-d", tmp.getPath()});
         * generator.setConfiguration(config); generator.generate();
         */

        try {
            int result = 0;
            org.apache.cxf.tools.java2wsdl.JavaToWSDL.runTool(new String[] {"-o",
                    tmp.getPath() + "/tmp.wsdl", sei.getName() });            
            if (0 != result) {
                LOG.log(Level.SEVERE, "WSDL_GENERATION_BAD_RESULT_MSG", result);
                return null;
            }

            // schema and WSDL file should have been created in tmp directory

            File[] generated = tmp.listFiles();
            File schema = null;
            File wsdl = null;
            for (File f : generated) {
                if (f.isFile()) {
                    if (null == wsdl && f.getName().endsWith(".wsdl")) {
                        wsdl = f;
                    } else if (null == schema && f.getName().endsWith(".xsd")) {
                        schema = f;
                    }
                    if (null != schema && null != wsdl) {
                        break;
                    }
                }
            }
            if (null == wsdl || null == schema) {
                LOG.severe("WSDL_SCHEMA_GENERATION_FAILURE_MSG");
                return null;
            } else if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Generated " + wsdl.getPath() + " and "
                        + schema.getPath());
            }

            /*
             * WSDLFactory wf = getWSDLFactory();
             * 
             * try { WSDLReader reader = wf.newWSDLReader();
             * reader.setFeature("javax.wsdl.verbose", false);
             * reader.setExtensionRegistry(registry); definition =
             * reader.readWSDL(wsdl.getPath()); } catch (WSDLException ex) {
             * LOG.log(Level.SEVERE, "WSDL_UNREADABLE_MSG", ex); }
             */

            // definition = org.apache.cxf.tools.java2wsdl.JavaToWSDL.getDefinition();

        } finally {
            class Directory {
                private final File dir;

                Directory(File d) {
                    dir = d;
                }

                void delete() {
                    File[] entries = dir.listFiles();
                    for (File f : entries) {
                        if (f.isDirectory()) {
                            Directory d = new Directory(f);
                            d.delete();
                        }
                        f.delete();
                    }
                    dir.delete();
                }
            }
            Directory dir = new Directory(tmp);
            dir.delete();
        }

        return definition;
    }
    
    private void registerInitialExtensions() throws BusException {
        Properties initialExtensions = null;
        try {
            initialExtensions = PropertiesLoaderUtils
                .loadAllProperties(EXTENSIONS_RESOURCE, Thread.currentThread()
                    .getContextClassLoader());
        } catch (IOException ex) {
            throw new BusException(ex);
        }
        
        for (Iterator it = initialExtensions.keySet().iterator(); it.hasNext();) {
            String elementType = (String)it.next();
            String parentType = initialExtensions.getProperty(elementType);
            
            try {
                JAXBExtensionHelper.addExtensions(registry, 
                                                  parentType, 
                                                  elementType, 
                                                  getClass().getClassLoader());
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.WARNING, "EXTENSION_ADD_FAILED_MSG", ex);
            } catch (JAXBException ex) {
                LOG.log(Level.WARNING, "EXTENSION_ADD_FAILED_MSG", ex);
            }
        }
    }

}
