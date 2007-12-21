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

package org.apache.cxf.javascript.types;

import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.javascript.XmlSchemaUtils;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * Information about a sequence 'element'
 * For now, only handles elements and 'any'. 
 */
public class SequenceItemInfo {
    private XmlSchemaParticle particle;
    private XmlSchemaElement sequenceElement;
    private XmlSchemaType elementType;
    private boolean nillable;
    private String elementName;
    private String elementJavascriptVariable;
    private String defaultValueString;
    private boolean any;
    
    /**
     * Gather information about a member of a sequence.
     * @param xmlSchemaCollection
     * @param containingType
     * @param sequenceMember
     */
    public SequenceItemInfo(SchemaCollection xmlSchemaCollection,
                            XmlSchemaComplexType containingType, 
                            XmlSchemaObject sequenceMember) {
        particle = XmlSchemaUtils.getObjectParticle(sequenceMember, containingType);
        if (particle instanceof XmlSchemaAny) {
            any = true;
            elementName = "any";
            elementJavascriptVariable = elementName;
        } else {
            sequenceElement = (XmlSchemaElement)sequenceMember;
            elementType = XmlSchemaUtils.getElementType(xmlSchemaCollection, 
                                                   null, sequenceElement, containingType);
            nillable = sequenceElement.isNillable();
            if (sequenceElement.isAbstract()) { 
                XmlSchemaUtils.unsupportedConstruct("ABSTRACT_ELEMENT", 
                                                    sequenceElement.getName(), containingType);
            }
            elementName = sequenceElement.getName();
            elementJavascriptVariable = sequenceElement.getName();
            defaultValueString = sequenceElement.getDefaultValue();
        }

        
    }
    
    
    /** 
     * @return Returns the particle.
     */
    public XmlSchemaParticle getParticle() {
        return particle;
    }
    /** * @return Returns the sequenceElement.
     */
    public XmlSchemaElement getSequenceElement() {
        return sequenceElement;
    }
    /** * @return Returns the elType.
     */
    public XmlSchemaType getElementType() {
        return elementType;
    }
    /** * @return Returns the nillable.
     */
    public boolean isNillable() {
        return nillable;
    }
    /** * @return Returns the elementName.
     */
    public String getElementName() {
        return elementName;
    }
    /** * @return Returns the elementJavascriptVariable.
     */
    public String getElementJavascriptVariable() {
        return elementJavascriptVariable;
    }
    /** * @return Returns the defaultValueString.
     */
    public String getDefaultValueString() {
        return defaultValueString;
    }
    /** * @return Returns the any.
     */
    public boolean isAny() {
        return any;
    }


    /**
     * @param particle The particle to set.
     */
    public void setParticle(XmlSchemaParticle particle) {
        this.particle = particle;
    }


    /**
     * @param sequenceElement The sequenceElement to set.
     */
    public void setSequenceElement(XmlSchemaElement sequenceElement) {
        this.sequenceElement = sequenceElement;
    }


    /**
     * @param elementType The elementType to set.
     */
    public void setElementType(XmlSchemaType elementType) {
        this.elementType = elementType;
    }


    /**
     * @param nillable The nillable to set.
     */
    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }


    /**
     * @param elementName The elementName to set.
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }


    /**
     * @param elementJavascriptVariable The elementJavascriptVariable to set.
     */
    public void setElementJavascriptVariable(String elementJavascriptVariable) {
        this.elementJavascriptVariable = elementJavascriptVariable;
    }


    /**
     * @param defaultValueString The defaultValueString to set.
     */
    public void setDefaultValueString(String defaultValueString) {
        this.defaultValueString = defaultValueString;
    }


    /**
     * @param any The any to set.
     */
    public void setAny(boolean any) {
        this.any = any;
    }
}
