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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.bind.JAXBException;

import org.apache.cxf.BusException;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.PropertiesLoaderUtils;
import org.apache.cxf.wsdl.JAXBExtensionHelper;
import org.apache.cxf.wsdl.WSDLBuilder;
import org.apache.cxf.wsdl4jutils.WSDLLocatorImpl;

public class WSDLDefinitionBuilder implements WSDLBuilder<Definition> {    
    
    protected static final Logger LOG = LogUtils.getL7dLogger(WSDLDefinitionBuilder.class);
    private static final String EXTENSIONS_RESOURCE = "META-INF/extensions.xml";
    
    protected WSDLReader wsdlReader;
    protected Definition wsdlDefinition;
    final WSDLFactory wsdlFactory;
    final ExtensionRegistry registry;    
    private List<Definition> importedDefinitions = new ArrayList<Definition>();
    
    public WSDLDefinitionBuilder() {    
        try {
            wsdlFactory = WSDLFactory.newInstance();
            registry = wsdlFactory.newPopulatedExtensionRegistry();
            registerInitialExtensions();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Definition build(String wsdlURL) {
        parseWSDL(wsdlURL);
        return wsdlDefinition;
    }

    protected void parseWSDL(String wsdlURL) {
        try {            
            wsdlReader = wsdlFactory.newWSDLReader();
            // TODO enable the verbose if in verbose mode.
            wsdlReader.setFeature("javax.wsdl.verbose", false);
            wsdlReader.setExtensionRegistry(registry);
            
            // REVIST: URIResolve is to solve the wsdl import and schema import, 
            //         but seems it works fine now without URIResolver
            //         URIResolve has a bug, it can not resolve the wsdl in testutils
            
            //URIResolver resolver = new URIResolver(wsdlURL);
            //InputSource insource = new InputSource(resolver.getInputStream());
            //wsdlURL = resolver.getURI().toString();
            //wsdlDefinition = wsdlReader.readWSDL(new WSDLResolver(wsdlURL, insource));
            WSDLLocatorImpl wsdlLocator = new WSDLLocatorImpl(wsdlURL);
            wsdlDefinition = wsdlReader.readWSDL(wsdlLocator);            

            parseImports(wsdlDefinition);
        } catch (Exception we) {
            Message msg = new Message("FAIL_TO_CREATE_WSDL_DEFINITION",
                                      LOG, 
                                      wsdlURL);
            throw new RuntimeException(msg.toString(), we);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void parseImports(Definition def) {
        List<Import> importList = new ArrayList<Import>();
        Map imports = def.getImports();
        for (Iterator iter = imports.keySet().iterator(); iter.hasNext();) {
            String uri = (String)iter.next();
            importList.addAll((List<Import>)imports.get(uri));
        }
        for (Import impt : importList) {
            parseImports(impt.getDefinition());
            importedDefinitions.add(impt.getDefinition());
        }
    }
    
    public List<Definition> getImportedDefinitions() {
        return importedDefinitions;
    }

    private void registerInitialExtensions() throws BusException {
        Properties initialExtensions = null;
        try {
            initialExtensions = PropertiesLoaderUtils.loadAllProperties(EXTENSIONS_RESOURCE, Thread
                            .currentThread().getContextClassLoader());
        } catch (IOException ex) {
            throw new BusException(ex);
        }

        for (Iterator it = initialExtensions.keySet().iterator(); it.hasNext();) {
            StringTokenizer st = new StringTokenizer(initialExtensions.getProperty((String) it.next()), "=");
            String parentType = st.nextToken();
            String elementType = st.nextToken();
            try {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Registering extension: " + elementType + " for parent: " + parentType);
                }
                JAXBExtensionHelper.addExtensions(registry, parentType, elementType, getClass()
                                .getClassLoader());
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.WARNING, "EXTENSION_ADD_FAILED_MSG", ex);
            } catch (JAXBException ex) {
                LOG.log(Level.WARNING, "EXTENSION_ADD_FAILED_MSG", ex);
            }
        }
    }
    
    public ExtensionRegistry getExtenstionRegistry() {
        return registry;
    }

    public WSDLFactory getWSDLFactory() {
        return wsdlFactory;
    }
}
