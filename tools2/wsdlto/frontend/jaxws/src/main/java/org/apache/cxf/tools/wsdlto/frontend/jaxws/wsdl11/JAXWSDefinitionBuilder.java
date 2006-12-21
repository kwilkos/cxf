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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.xml.sax.InputSource;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.resource.URIResolver;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.tools.wsdlto.core.AbstractWSDLBuilder;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion.CustomizationParser;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion.WSDLExtensionRegister;
import org.apache.cxf.wsdl11.WSDLDefinitionBuilder;
import org.apache.cxf.wsdl4jutils.WSDLResolver;

public class JAXWSDefinitionBuilder extends AbstractWSDLBuilder {

    protected static final Logger LOG = LogUtils.getL7dLogger(WSDLDefinitionBuilder.class);
    protected WSDLFactory wsdlFactory;
    protected WSDLReader wsdlReader;
    protected Definition wsdlDefinition;
    protected List<Definition> importedDefs = new ArrayList<Definition>();
    protected CustomizationParser cusParser;
    
    public JAXWSDefinitionBuilder() {
    }
    
    public void setContext(ToolContext arg) {
        context = arg;
    }

    public Definition build() {
        String wsdlURL = (String)context.get(ToolConstants.CFG_WSDLURL);
        return build(wsdlURL);
    }

    public Definition build(String wsdlURL) {
        parseWSDL(wsdlURL);
        context.put(ToolConstants.WSDL_DEFINITION, wsdlDefinition);
        context.put(ToolConstants.IMPORTED_DEFINITION, this.importedDefs);
        return wsdlDefinition;
    }

    public void customize() {
        cusParser = CustomizationParser.getInstance();
        cusParser.parse(context);   
    }

    protected void parseWSDL(String wsdlURL) {
        try {
            wsdlFactory = WSDLFactory.newInstance();
            wsdlReader = wsdlFactory.newWSDLReader();
            wsdlReader.setFeature("javax.wsdl.verbose", false);
            // REVISIT: JAXWS/JMS/XML extensions, should be provided by the
            // WSDLServiceBuilder
            // Do we need to expose the wsdlReader and wsdlFactory?
            WSDLExtensionRegister register = new WSDLExtensionRegister(wsdlFactory, wsdlReader);
            register.registerExtensions();
            URIResolver resolver = new URIResolver(wsdlURL);
            InputSource insource = new InputSource(resolver.getInputStream());
            wsdlURL = resolver.getURI().toString();
            wsdlDefinition = wsdlReader.readWSDL(new WSDLResolver(wsdlURL, insource));
            checkSupported(wsdlDefinition);
            parseImports(wsdlDefinition);
        } catch (Exception we) {
            Message msg = new Message("FAIL_TO_CREATE_WSDL_DEFINITION", LOG, wsdlURL);
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
            importedDefs.add(impt.getDefinition());
        }
    }

    public List<Definition> getImportedDefinition() {
        return importedDefs;
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
    
    public CustomizationParser getCustomizationParer() {
        return cusParser;
    }
}
