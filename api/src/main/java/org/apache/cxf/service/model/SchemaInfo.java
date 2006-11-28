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

public final class SchemaInfo extends AbstractPropertiesHolder {
  
    TypeInfo typeInfo;
    String namespaceUri;
    Element element;
    
    public SchemaInfo(TypeInfo typeInfo, String namespaceUri) {
        this.typeInfo = typeInfo;
        this.namespaceUri = namespaceUri;
    }
    
    public TypeInfo getTypeInfo() {
        return typeInfo;
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
    }
}
