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

package org.apache.cxf.service.model;

import org.w3c.dom.Element;

import org.apache.ws.commons.schema.XmlSchema;

public final class SchemaInfo extends AbstractPropertiesHolder {
  
    ServiceInfo serviceInfo;
    String namespaceUri;
    Element element;
    boolean isElementQualified;
    boolean isAttributeQualified;
    XmlSchema schema;
    
    public SchemaInfo(ServiceInfo serviceInfo, String namespaceUri) {
        this.serviceInfo = serviceInfo;
        this.namespaceUri = namespaceUri;
        this.isElementQualified = false;
        this.isAttributeQualified = false;
    }
    
    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public String getNamespaceURI() {
        return namespaceUri;
    }

    public void setNamespaceURI(String nsUri) {
        this.namespaceUri = nsUri;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;        
        String form = element.getAttribute("elementFormDefault");
        if ((form != null) && form.equals("qualified")) {
            isElementQualified = true;
        }
        form = element.getAttribute("attributeFormDefault");
        if ((form != null) && form.equals("qualified")) {
            isAttributeQualified = true;
        }
    }

    public boolean isElementFormQualified() {
        return isElementQualified;
    }

    public boolean isAttributeFormQualified() {
        return isAttributeQualified;
    }

    public XmlSchema getSchema() {
        return schema;
    }

    public void setSchema(XmlSchema schema) {
        this.schema = schema;
    }
}
