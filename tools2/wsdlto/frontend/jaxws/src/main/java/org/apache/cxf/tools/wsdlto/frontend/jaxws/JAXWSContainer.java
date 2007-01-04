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

package org.apache.cxf.tools.wsdlto.frontend.jaxws;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.wsdl.Definition;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.ClassUtils;
import org.apache.cxf.tools.common.FrontEndGenerator;
import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.toolspec.ToolSpec;
import org.apache.cxf.tools.common.toolspec.parser.BadUsageException;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.wsdlto.WSDLToJavaContainer;
import org.apache.cxf.tools.wsdlto.core.DataBindingProfile;
import org.apache.cxf.tools.wsdlto.core.FrontEndProfile;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.wsdl11.JAXWSDefinitionBuilder;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;

public class JAXWSContainer extends WSDLToJavaContainer {
    
    private static final String TOOL_NAME = "wsdl2java";
    
    public JAXWSContainer(ToolSpec toolspec) throws Exception {
        super(TOOL_NAME, toolspec);
    }

    public Set<String> getArrayKeys() {
        Set<String> set = super.getArrayKeys();
        set.add(ToolConstants.CFG_BINDING);
        return set;
    }

    public void validate(ToolContext env) throws ToolException {
        super.validate(env);
        File tmpfile = new File("");
        if (env.containsKey(ToolConstants.CFG_BINDING)) {
            String[] bindings = (String[])env.get(ToolConstants.CFG_BINDING);
            for (int i = 0; i < bindings.length; i++) {               
                
                File bindingFile = null;
                //= new File(bindings[i]);
                try {
                    URI bindingURI = new URI(bindings[i]);
                    if (!bindingURI.isAbsolute()) {
                        bindingURI = tmpfile.toURI().resolve(bindingURI);
                    }
                    bindingFile = new File(bindingURI);
                } catch (URISyntaxException e) {
                    bindingFile = new File(bindings[i]);
                }
                bindings[i] = bindingFile.toURI().toString();
                if (!bindingFile.exists()) {
                    Message msg = new Message("FILE_NOT_EXIST", LOG, bindings[i]);
                    throw new ToolException(msg);
                } else if (bindingFile.isDirectory()) {
                    Message msg = new Message("NOT_A_FILE", LOG, bindings[i]);
                    throw new ToolException(msg);
                }
            }
            env.put(ToolConstants.CFG_BINDING, bindings);
        }
        
        String wsdl = (String)env.get(ToolConstants.CFG_WSDLURL);
        
        File wsdlFile = null;
        try {
            URI wsdlURI = new URI(wsdl);
            if (!wsdlURI.isAbsolute()) {
                wsdlURI = tmpfile.toURI().resolve(wsdlURI);
            }
            wsdlFile = new File(wsdlURI);
        } catch (URISyntaxException e) {
            wsdlFile = new File(wsdl);
        }
        if (!wsdlFile.exists()) {
            Message msg = new Message("FILE_NOT_EXIST", LOG, wsdl);
            throw new ToolException(msg);
        } else if (wsdlFile.isDirectory()) {
            Message msg = new Message("NOT_A_FILE", LOG, wsdl);
            throw new ToolException(msg);
        }
        env.put(ToolConstants.CFG_WSDLURL, wsdlFile.toURI().toString());        
    }
    
    public void execute(boolean exitOnFinish) throws ToolException {
        try {           
            super.execute(exitOnFinish);
            if (!hasInfoOption()) {
                
                buildToolContext();
                validate(context);
                
                context.put(ClassCollector.class, new ClassCollector());
                
                FrontEndProfile frontend = context.get(FrontEndProfile.class);
                
                Processor processor = frontend.getProcessor();
                
                ToolConstants.WSDLVersion version = getWSDLVersion();
                System.out.println("---version ---- " + version);
                
                ServiceInfo service = null;
                    
                // Build the ServiceModel from the WSDLModel
                if (version == ToolConstants.WSDLVersion.WSDL11) {
                   
                    JAXWSDefinitionBuilder  builder = 
                        (JAXWSDefinitionBuilder)frontend.getWSDLBuilder();
                    builder.setContext(context);
                    builder.build();
                    builder.customize();
                    context.put(ToolConstants.NS_JAXB_BINDINGS, 
                                builder.getJaxbBindings());                     
                    context.put(ToolConstants.HANDLER_CHAIN, builder.getHandlerChain());
                    //get the definition after customized
                    Definition definition = builder.getDefinition();
                    builder.validate(definition);
                    context.put(Definition.class, definition);
                    
                    
                    //get serviceInfo
                    WSDLServiceBuilder serviceBuilder = new WSDLServiceBuilder(getBus());
                    service = serviceBuilder.buildService(definition, getServiceQName(definition));
                    context.put(ServiceInfo.class, service);
                    
                } else {
                    // TODO: wsdl2.0 support
                }
                
                
                //initialize databinding
                DataBindingProfile databinding = (DataBindingProfile)context.get(DataBindingProfile.class);
                databinding.initialize(context);
                                
                generateTypes();                
                // Build the JavaModel from the ServiceModel
                processor.setEnvironment(context);
                processor.process();

                // Generate artifacts
                for (FrontEndGenerator generator : frontend.getGenerators()) {
                    generator.generate(context);
                }

                // Build projects: compile classes and copy resources etc.
                if (context.optionSet(ToolConstants.CFG_COMPILE)) {
                    new ClassUtils().compile(context);
                }

                if (context.isExcludeNamespaceEnabled()) {
                    removeExcludeFiles();
                }
            }
        } catch (ToolException ex) {
            if (ex.getCause() instanceof BadUsageException) {
                getInstance().printUsageException(TOOL_NAME, (BadUsageException)ex.getCause());
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

    
    
}
