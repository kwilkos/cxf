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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion;

import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.tools.common.ToolConstants;

public class JAXWSBindingParser {

    private ExtensionRegistry extReg;

    public JAXWSBindingParser(ExtensionRegistry ext) {
        extReg = ext;
    }

    public JAXWSBinding parse(Class parentType, Element element, String namespace) throws WSDLException {
        JAXWSBinding jaxwsBinding = (JAXWSBinding)extReg.createExtension(parentType,
                                                                         ToolConstants.JAXWS_BINDINGS);

        jaxwsBinding.setElementType(ToolConstants.JAXWS_BINDINGS);
        jaxwsBinding.setElement(element);
        jaxwsBinding.setDocumentBaseURI(namespace);
        parseElement(jaxwsBinding, element);
        return jaxwsBinding;
    }

    public void parseElement(JAXWSBinding jaxwsBinding, Element element) {
        NodeList children = element.getChildNodes();
        if (children != null && children.getLength() == 0) {
            // global binding
            if (isAsyncElement(element)) {
                jaxwsBinding.setEnableAsyncMapping(getNodeValue(element));
            }
            if (isMIMEElement(element)) {
                jaxwsBinding.setEnableMime(getNodeValue(element));
            }
            if (isPackageElement(element)) {
                jaxwsBinding.setPackage(getPackageName(element));
            }
            if (isJAXWSParameterElement(element)) {
                JAXWSParameter jpara = new JAXWSParameter();
                jpara.setName(element.getAttribute("name"));
                jpara.setElementName(element.getAttribute("childElementName"));
                jaxwsBinding.setJaxwsPara(jpara);
            }

        }

        if (children != null && children.getLength() > 0) {
            // other binding
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (isAsyncElement(child)) {
                    jaxwsBinding.setEnableAsyncMapping(getNodeValue(child));
                }
                if (isMIMEElement(child)) {
                    jaxwsBinding.setEnableMime(getNodeValue(child));
                }
                if (isPackageElement(child)) {
                    jaxwsBinding.setPackage(getPackageName(child));
                }

                if (isJAXWSMethodElement(child)) {
                    jaxwsBinding.setMethodName(getMethodName(child));
                }

            }
        }

    }

    private boolean isJAXWSMethodElement(Node node) {
        return ToolConstants.NS_JAXWS_BINDINGS.equals(node.getNamespaceURI())
               && "method".equals(node.getLocalName());
    }

    private String getMethodName(Node node) {
        Element ele = (Element)node;
        return ele.getAttribute("name");
    }

    private boolean isPackageElement(Node node) {
        if (ToolConstants.NS_JAXWS_BINDINGS.equals(node.getNamespaceURI())
            && "package".equals(node.getLocalName())) {
            return true;
        }
        return false;

    }

    private boolean isJAXWSParameterElement(Node node) {
        return (ToolConstants.NS_JAXWS_BINDINGS.equals(node.getNamespaceURI()))
               && "parameter".equals(node.getLocalName());

    }

    private String getPackageName(Node node) {
        Element ele = (Element)node;
        return ele.getAttribute("name");
    }

    private Boolean isAsyncElement(Node node) {
        return "enableAsyncMapping".equals(node.getLocalName())
               && ToolConstants.NS_JAXWS_BINDINGS.equals(node.getNamespaceURI());

    }

    private Boolean getNodeValue(Node node) {
        return Boolean.valueOf(node.getTextContent());
    }

    private Boolean isMIMEElement(Node node) {
        return "enableMIMEContent".equals(node.getLocalName())
               && ToolConstants.NS_JAXWS_BINDINGS.equals(node.getNamespaceURI());
    }

}
