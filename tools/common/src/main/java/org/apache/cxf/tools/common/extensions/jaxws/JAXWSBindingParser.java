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

package org.apache.cxf.tools.common.extensions.jaxws;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.tools.common.ToolConstants;

public class JAXWSBindingParser {

    public JAXWSBinding parse(BindingsNode bindingsNode, Definition def) throws WSDLException {
        return parse(bindingsNode.getParentType(), bindingsNode.getElement(), def);
    }
    
    public JAXWSBinding parse(Class parentType, Element element, Definition def) throws WSDLException {
        ExtensionRegistry extReg = def.getExtensionRegistry();
        JAXWSBinding jaxwsBinding = (JAXWSBinding)extReg.createExtension(parentType,
                                                                         ToolConstants.JAXWS_BINDINGS);
        
        jaxwsBinding.setElementType(ToolConstants.JAXWS_BINDINGS);
        jaxwsBinding.setElement(element);
        jaxwsBinding.setDocumentBaseURI(def.getDocumentBaseURI());

        parseElement(jaxwsBinding, element);
        return jaxwsBinding;
    }

    public void parseElement(JAXWSBinding jaxwsBinding, Element element) {
        NodeList children = element.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child =  children.item(i);
                if (isAsyncElement(child)) {
                    jaxwsBinding.setAsyncMapping(true);
                    jaxwsBinding.setEnableAsyncMapping(isAsync(child));
                }
                if (isMIMEElement(child)) {
                    jaxwsBinding.setSetMimeEnable(true);
                    jaxwsBinding.setEnableMime(isMIMEEnabled(child));
                }                
            }
        }
    }
    
    private Boolean isAsyncElement(Node node) {
        return "enableAsyncMapping".equals(node.getNodeName());
    }

    private Boolean isAsync(Node node) {
        return Boolean.valueOf(node.getTextContent());
    }
    
    private Boolean isMIMEElement(Node node) {
        return "enableMIMEContent".equals(node.getNodeName());
    }

    private Boolean isMIMEEnabled(Node node) {
        return Boolean.valueOf(node.getTextContent());
    }
}
