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

package org.apache.cxf.tools.common.extensions.xmlformat;

import java.io.Serializable;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

public class XMLFormat implements ExtensibilityElement, Serializable {

    private boolean required;
    private QName elementType;
    private Element element;
    private String documentBaseURI;

    private QName rootNode;
    
    public void setRequired(Boolean r) {
        this.required = r;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setElementType(QName elemType) {
        this.elementType = elemType;
    }

    public QName getElementType() {
        return elementType;
    }

    public QName getRootNode() {
        return rootNode;
    }

    public void setRootNode(QName pRootNode) {
        this.rootNode = pRootNode;
    }

    public String getDocumentBaseURI() {
        return documentBaseURI;
    }

    public void setDocumentBaseURI(String pDocumentBaseURI) {
        this.documentBaseURI = pDocumentBaseURI;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element pElement) {
        this.element = pElement;
    }

}
