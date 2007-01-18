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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.wsdl11;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.xml.WSDLReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.helpers.FileUtils;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.tools.validator.internal.WSDL11Validator;
import org.apache.cxf.tools.wsdlto.core.AbstractWSDLBuilder;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion.CustomizationParser;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion.JAXWSBinding;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion.JAXWSBindingDeserializer;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion.JAXWSBindingSerializer;
import org.apache.cxf.wsdl11.WSDLDefinitionBuilder;

public class JAXWSDefinitionBuilder extends AbstractWSDLBuilder<Definition> {

    protected static final Logger LOG = LogUtils.getL7dLogger(JAXWSDefinitionBuilder.class);
    protected CustomizationParser cusParser;
    
    private WSDLDefinitionBuilder builder;
    private WSDLReader wsdlReader;
    private Definition wsdlDefinition;
    
    private Set<InputSource> jaxbBindings;
    private Element handlerChain;
       
    public JAXWSDefinitionBuilder() {
        builder = new WSDLDefinitionBuilder();
        ExtensionRegistry registry = builder.getExtenstionRegistry();
        registerJaxwsExtension(registry);
        wsdlReader = builder.getWSDLReader();
        wsdlReader.setExtensionRegistry(registry);
    }
    
    public Definition build() {
        String wsdlURL = (String)context.get(ToolConstants.CFG_WSDLURL);
        return build(wsdlURL);
    }

    public Definition build(String wsdlURL) {
        wsdlDefinition = builder.build(wsdlURL);
        context.put(ToolConstants.IMPORTED_DEFINITION, builder.getImportedDefinitions());
        checkSupported(wsdlDefinition);        
        return wsdlDefinition;
    }

    private void registerJaxwsExtension(ExtensionRegistry registry) {
        registerJAXWSBinding(registry, Definition.class);
        registerJAXWSBinding(registry, PortType.class);
        registerJAXWSBinding(registry, Operation.class);
        registerJAXWSBinding(registry, Binding.class);
        registerJAXWSBinding(registry, BindingOperation.class);
    }

    private void registerJAXWSBinding(ExtensionRegistry registry, Class clz) {
        registry.registerSerializer(clz, ToolConstants.JAXWS_BINDINGS, new JAXWSBindingSerializer());
        registry.registerDeserializer(clz, ToolConstants.JAXWS_BINDINGS, new JAXWSBindingDeserializer());
        registry.mapExtensionTypes(clz, ToolConstants.JAXWS_BINDINGS, JAXWSBinding.class);
    }

    public void customize() {
        if (!context.containsKey(ToolConstants.CFG_BINDING)) {
            return;
        }
        cusParser = new CustomizationParser();
        cusParser.parse(context);
        jaxbBindings = cusParser.getJaxbBindings();
        handlerChain = cusParser.getHandlerChains();

        context.put(ToolConstants.NS_JAXB_BINDINGS, jaxbBindings);
        context.put(ToolConstants.HANDLER_CHAIN, handlerChain);
        try {
            this.wsdlDefinition = buildCustomizedDefinition();
        } catch (Exception e) {
            Message msg = new Message("FAIL_TO_CREATE_WSDL_DEFINITION",
                                      LOG,
                                      cusParser.getCustomizedWSDLElement().getBaseURI());
            throw new RuntimeException(msg.toString(), e);
        }
    }

    private void checkSupported(Definition def) throws ToolException {
        if (isRPCEncoded(def)) {
            org.apache.cxf.common.i18n.Message msg = 
                new org.apache.cxf.common.i18n.Message("UNSUPPORTED_RPC_ENCODED"
                                                       , LOG);
            throw new ToolException(msg);
        }
    }

    private boolean isRPCEncoded(Definition def) {
        Iterator ite1 = def.getBindings().values().iterator();
        while (ite1.hasNext()) {
            Binding binding = (Binding)ite1.next();
            String bindingStyle = SOAPBindingUtil.getBindingStyle(binding);

            Iterator ite2 = binding.getBindingOperations().iterator();
            while (ite2.hasNext()) {
                BindingOperation bop = (BindingOperation)ite2.next();
                String bopStyle = SOAPBindingUtil.getSOAPOperationStyle(bop);

                String outputUse = "";
                if (SOAPBindingUtil.getBindingOutputSOAPBody(bop) != null) {
                    outputUse = SOAPBindingUtil.getBindingOutputSOAPBody(bop).getUse();
                }
                String inputUse = "";
                if (SOAPBindingUtil.getBindingInputSOAPBody(bop) != null) {
                    inputUse = SOAPBindingUtil.getBindingInputSOAPBody(bop).getUse();
                }
                if ((SOAPBinding.Style.RPC.name().equalsIgnoreCase(bindingStyle) || SOAPBinding.Style.RPC
                    .name().equalsIgnoreCase(bopStyle))
                    && (SOAPBinding.Use.ENCODED.name().equalsIgnoreCase(inputUse) || SOAPBinding.Use.ENCODED
                        .name().equalsIgnoreCase(outputUse))) {
                    return true;
                }
            }

        }
        return false;
    }
    
    private CustomizationParser getCustomizationParer() {
        return cusParser;
    }

    private Definition buildCustomizedDefinition() throws Exception {
        File tmpFile = File.createTempFile("customzied", ".wsdl");
        OutputStream outs = new FileOutputStream(tmpFile);
        DOMUtils.writeXml(getCustomizationParer().getCustomizedWSDLElement(), outs);
        InputStream ins = new FileInputStream(new File(tmpFile.toURI()));
        Document wsdlDoc = DOMUtils.readXml(ins);
        Definition def =  wsdlReader.readWSDL(this.wsdlDefinition.getDocumentBaseURI(), 
                                              wsdlDoc);
        FileUtils.delete(tmpFile);
        return def;
    }

    public Definition getWSDLModel() {
        if (this.wsdlDefinition == null) {
            this.build();
            this.customize();
        }
        
        return this.wsdlDefinition;
    }
    public WSDLReader getWSDLReader() {
        return wsdlReader;
    }
    

    public boolean validate(Definition def) throws ToolException {
        return new WSDL11Validator(def, context).isValid();
    }
}
