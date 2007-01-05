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

import java.io.*;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import org.w3c.dom.*;

public class JAXWSBinding implements ExtensibilityElement, Serializable {

    private boolean isSetAsyncMapping;
    private boolean enableAsyncMapping;
    
    private boolean isSetMimeEnable;
    private boolean enableMime = true;
    
    private Element element;
    private boolean required;
    private QName elementType;
    private String documentBaseURI;

    public void setDocumentBaseURI(String baseURI) {
        this.documentBaseURI = baseURI;
    }

    public String getDocumentBaseURI() {
        return this.documentBaseURI;
    }
    
    public void setElement(Element elem) {
        this.element = elem;
    }

    public Element getElement() {
        return element;
    }

    public boolean isSetAsyncMapping() {
        return this.isSetAsyncMapping;
    }

    public void setAsyncMapping(boolean set) {
        this.isSetAsyncMapping = set;
    }
    
    public boolean isEnableAsyncMapping() {
        return this.enableAsyncMapping;
    }

    public void setEnableAsyncMapping(boolean enableAsync) {
        this.enableAsyncMapping = enableAsync;
    }

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

    public boolean isEnableMime() {
        return enableMime;
    }

    public void setEnableMime(boolean pEnableMime) {
        this.enableMime = pEnableMime;
    }

    public boolean isSetMimeEnable() {
        return isSetMimeEnable;
    }

    public void setSetMimeEnable(boolean pIsSetMimeEnable) {
        this.isSetMimeEnable = pIsSetMimeEnable;
    }
}
