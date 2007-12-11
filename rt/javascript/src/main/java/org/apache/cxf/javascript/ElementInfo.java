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

import java.util.logging.Logger;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * All the information needed to create the JavaScript for an Xml Schema
 * element.
 */
public final class ElementInfo {
    private static final Logger LOG = LogUtils.getL7dLogger(ElementInfo.class);
    private String utilsVarName;
    private XmlSchemaElement element;
    private String javascriptName;
    private String xmlName;
    private XmlSchemaType containingType;
    // in the RPC case, we can have a type and no element.
    private XmlSchemaType type;
    private boolean empty;
    // These are exactly the same values as we find in the XmlSchemaElement.
    // there is no rationalization.
    private long minOccurs;
    private long maxOccurs;

    private ElementInfo() {
    }

    public static ElementInfo forGlobalElement(XmlSchemaElement element, String javascriptPrefix,
                                               XmlSchema currentSchema, SchemaCollection schemaCollection,
                                               NamespacePrefixAccumulator prefixAccumulator) {
        ElementInfo elementInfo = new ElementInfo();
        elementInfo.element = element;
        elementInfo.minOccurs = element.getMinOccurs();
        elementInfo.maxOccurs = element.getMaxOccurs();

        factoryCommon(element, 
                     true,
                     javascriptPrefix,
                     currentSchema, 
                     schemaCollection, 
                     prefixAccumulator, 
                     elementInfo);
        return elementInfo;
    }
    
    /**
     * Create element information for a part element. For a part, the JavaScript and Element names are 
     * calculated in advance, and the element itself might be null! In that case, the minOccurs
     * and maxOccurs are conventional. 
     * @param element the element, or null
     * @param schemaCollection the schema collection, for resolving types.
     * @param javascriptName javascript variable name
     * @param xmlElementName xml element string
     * @return
     */
    public static ElementInfo forPartElement(XmlSchemaElement element,
                                             SchemaCollection schemaCollection,
                                             String javascriptName,
                                             String xmlElementName) {
        ElementInfo elementInfo = new ElementInfo();
        elementInfo.element = element;
        if (element == null) {
            elementInfo.minOccurs = 1;
            elementInfo.maxOccurs = 1;
        } else {
            elementInfo.minOccurs = element.getMinOccurs();
            elementInfo.maxOccurs = element.getMaxOccurs();
            factorySetupType(element, schemaCollection, elementInfo);
        }
        elementInfo.javascriptName = javascriptName;
        elementInfo.xmlName = xmlElementName;
        
        return elementInfo;
    }

    public static ElementInfo forLocalElement(XmlSchemaElement sequenceElement, 
                                              String javascriptPrefix,
                                              XmlSchema currentSchema, 
                                              SchemaCollection schemaCollection,
                                              NamespacePrefixAccumulator prefixAccumulator) {
        ElementInfo elementInfo = new ElementInfo();
        XmlSchemaElement realElement = sequenceElement;
        boolean global = false;

        if (sequenceElement.getRefName() != null) {
            XmlSchemaElement refElement = schemaCollection.getElementByQName(sequenceElement.getRefName());
            if (refElement == null) {
                Message message = new Message("ELEMENT_DANGLING_REFERENCE", LOG, sequenceElement.getQName(),
                                              sequenceElement.getRefName());
                throw new UnsupportedConstruct(message.toString());
            }

            realElement = refElement;

        }
        elementInfo.minOccurs = sequenceElement.getMinOccurs();
        elementInfo.maxOccurs = sequenceElement.getMaxOccurs();
        
        factoryCommon(realElement, 
                     global, 
                     javascriptPrefix,
                     currentSchema, 
                     schemaCollection, 
                     prefixAccumulator, 
                     elementInfo);
        
        elementInfo.element = realElement;

        return elementInfo;
    }

    private static void factoryCommon(XmlSchemaElement element, 
                                      boolean global,
                                      String javascriptPrefix,
                                      XmlSchema currentSchema,
                                      SchemaCollection schemaCollection,
                                      NamespacePrefixAccumulator prefixAccumulator, 
                                      ElementInfo elementInfo) {
        String elementNamespaceURI = element.getQName().getNamespaceURI();
        boolean elementNoNamespace = "".equals(elementNamespaceURI);

        XmlSchema elementSchema = null;
        if (!elementNoNamespace) {
            elementSchema = schemaCollection.getSchemaByTargetNamespace(elementNamespaceURI);
            if (elementSchema == null) {
                throw new RuntimeException("Missing schema " + elementNamespaceURI);
            }
        }

        boolean qualified = !elementNoNamespace
                            && XmlSchemaUtils.isElementQualified(element, true, currentSchema, elementSchema);
        elementInfo.xmlName = prefixAccumulator.xmlElementString(element, qualified);
        
        factorySetupType(element, schemaCollection, elementInfo);
        
        // we are assuming here that we are not dealing, in close proximity,
        // with elements with identical local names and different namespaces.
        elementInfo.javascriptName = javascriptPrefix + element.getQName().getLocalPart();
    }

    private static void factorySetupType(XmlSchemaElement element, SchemaCollection schemaCollection,
                                         ElementInfo elementInfo) {
        elementInfo.type = element.getSchemaType();
        if (elementInfo.type == null) {
            elementInfo.type = schemaCollection.getTypeByQName(element.getSchemaTypeName());
            if (elementInfo.type == null) {
                throw new RuntimeException("null type");
            }
        }
    }

    public String getUtilsVarName() {
        return utilsVarName;
    }

    public void setUtilsVarName(String utilsVarName) {
        this.utilsVarName = utilsVarName;
    }

    public XmlSchemaElement getElement() {
        return element;
    }

    public String getElementJavascriptName() {
        return javascriptName;
    }
    
    public void setElementJavascriptName(String name) {
        javascriptName = name;
    }

    public String getElementXmlName() {
        return xmlName;
    }

    public void setElementXmlName(String elementXmlName) {
        this.xmlName = elementXmlName;
    }

    public XmlSchemaType getContainingType() {
        return containingType;
    }

    public void setContainingType(XmlSchemaType containingType) {
        this.containingType = containingType;
    }

    public XmlSchemaType getType() {
        return type;
    }

    public void setType(XmlSchemaType type) {
        this.type = type;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public long getMinOccurs() {
        return minOccurs;
    }

    public long getMaxOccurs() {
        return maxOccurs;
    }

    public boolean isArray() {
        return maxOccurs > 1;
    }
    
    public boolean isOptional() {
        return minOccurs == 0 && maxOccurs == 1;
    }

}
