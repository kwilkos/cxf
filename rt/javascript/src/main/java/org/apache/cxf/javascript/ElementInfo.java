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

package org.apache.cxf.javascript;

import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * All the information needed to create the JavaScript for an Xml Schema element. 
 */
public class ElementInfo {
    private String utilsVarName;
    private XmlSchemaElement element; 
    private String elementJavascriptName;
    private String elementXmlName;
    private SchemaCollection xmlSchemaCollection;
    private String referencingURI;
    private XmlSchemaType containingType;
    
    public String getUtilsVarName() {
        return utilsVarName;
    }
    public void setUtilsVarName(String utilsVarName) {
        this.utilsVarName = utilsVarName;
    }
    public XmlSchemaElement getElement() {
        return element;
    }
    public void setElement(XmlSchemaElement element) {
        this.element = element;
    }
    public String getElementJavascriptName() {
        return elementJavascriptName;
    }
    public void setElementJavascriptName(String elementJavascriptName) {
        this.elementJavascriptName = elementJavascriptName;
    }
    public String getElementXmlName() {
        return elementXmlName;
    }
    public void setElementXmlName(String elementXmlName) {
        this.elementXmlName = elementXmlName;
    }
    public SchemaCollection getXmlSchemaCollection() {
        return xmlSchemaCollection;
    }
    public void setXmlSchemaCollection(SchemaCollection xmlSchemaCollection) {
        this.xmlSchemaCollection = xmlSchemaCollection;
    }
    public String getReferencingURI() {
        return referencingURI;
    }
    public void setReferencingURI(String referencingURI) {
        this.referencingURI = referencingURI;
    }
    public XmlSchemaType getContainingType() {
        return containingType;
    }
    public void setContainingType(XmlSchemaType containingType) {
        this.containingType = containingType;
    }
}
