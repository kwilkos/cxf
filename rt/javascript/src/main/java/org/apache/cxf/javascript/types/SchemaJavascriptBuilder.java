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

import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.javascript.ElementInfo;
import org.apache.cxf.javascript.JavascriptUtils;
import org.apache.cxf.javascript.NameManager;
import org.apache.cxf.javascript.NamespacePrefixAccumulator;
import org.apache.cxf.javascript.UnsupportedConstruct;
import org.apache.cxf.javascript.XmlSchemaUtils;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * Generate Javascript for a schema, and provide information needed for the service builder.
 * As of this pass, there is no support for non-sequence types or for attribute mappings.
 * @author bimargulies
 */
public class SchemaJavascriptBuilder {
    
    private static final Logger LOG = LogUtils.getL7dLogger(SchemaJavascriptBuilder.class);
    
    private SchemaCollection xmlSchemaCollection;
    private NameManager nameManager;
    private NamespacePrefixAccumulator prefixAccumulator;
    private SchemaInfo schemaInfo;

    private int anyCounter;

    // In general, I (bimargulies) hate fields that are temporary communications
    // between members of a class. However, given the style restrictions on the number
    // of parameters, it's the least of the evils.
    private StringBuilder code;
    private StringBuilder accessors;
    private JavascriptUtils utils;
    
    public SchemaJavascriptBuilder(SchemaCollection schemaCollection,
                                   NamespacePrefixAccumulator prefixAccumulator,
                                   NameManager nameManager) {
        this.xmlSchemaCollection = schemaCollection;
        this.nameManager = nameManager;
        this.prefixAccumulator = prefixAccumulator;
    }
    
    public String generateCodeForSchema(SchemaInfo schema) {
        schemaInfo = schema;
        code = new StringBuilder();
        code.append("//\n");
        code.append("// Definitions for schema: " + schema.getNamespaceURI());
        if (schema.getSystemId() != null) {
            code.append("\n//  " + schema.getSystemId());
        }
        code.append("\n//\n");

        XmlSchemaObjectTable schemaTypes = schema.getSchema().getSchemaTypes();
        Iterator namesIterator = schemaTypes.getNames();
        while (namesIterator.hasNext()) {
            QName name = (QName)namesIterator.next();
            XmlSchemaObject xmlSchemaObject = (XmlSchemaObject)schemaTypes.getItem(name);
            if (xmlSchemaObject instanceof XmlSchemaComplexType) {
                try {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType)xmlSchemaObject;
                    if (complexType.getName() != null) {
                        complexTypeConstructorAndAccessors(complexType.getQName(), complexType);
                        complexTypeSerializerFunction(complexType.getQName(), complexType);
                        domDeserializerFunction(complexType.getQName(), complexType);
                    }
                } catch (UnsupportedConstruct usc) {
                    LOG.warning(usc.toString());
                    continue; // it could be empty, but the style checker would complain.
                }
            }
        }
        
        // now add in global elements with anonymous types.        
        schemaTypes = schema.getSchema().getElements();
        namesIterator = schemaTypes.getNames();
        while (namesIterator.hasNext()) {
            QName name = (QName)namesIterator.next();
            XmlSchemaObject xmlSchemaObject = (XmlSchemaObject)schemaTypes.getItem(name);
            if (xmlSchemaObject instanceof XmlSchemaElement) { // the alternative is too wierd to contemplate.
                try {
                    XmlSchemaElement element = (XmlSchemaElement)xmlSchemaObject;
                    if (element.getSchemaTypeName() == null && element.getSchemaType() == null) {
                        Message message = new Message("ELEMENT_MISSING_TYPE", LOG, 
                                                      element.getQName(),
                                                      element.getSchemaTypeName(),
                                                      schema.getNamespaceURI());
                        LOG.warning(message.toString());
                        continue;
                    }
                    XmlSchemaType type;
                    if (element.getSchemaType() != null) {
                        type = element.getSchemaType();
                    } else {
                        type = schema.getSchema().getTypeByName(element.getSchemaTypeName());
                    }
                    if (!(type instanceof XmlSchemaComplexType)) { 
                        // we never make classes for simple type.
                        continue;
                    }

                    XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
                    // for named types we don't bother to generate for the element.
                    if (complexType.getName() == null) {
                        complexTypeConstructorAndAccessors(element.getQName(), complexType);
                        complexTypeSerializerFunction(element.getQName(), complexType);
                        domDeserializerFunction(element.getQName(), complexType); 
                    }
                } catch (UnsupportedConstruct usc) {
                    continue; // it could be empty, but the style checker would complain.
                }
            }
        }
        
        String returnValue = code.toString();
        LOG.finer(returnValue);
        return returnValue;
    }
    
    // In general, I (bimargulies) hate fields that are temporary communications
    // between members of a class. However, given the style restrictions on the number
    // of parameters, it's the least of the evils.
    
    public void complexTypeConstructorAndAccessors(QName name, XmlSchemaComplexType type) {
        accessors = new StringBuilder();
        utils = new JavascriptUtils(code);
        XmlSchemaSequence sequence = XmlSchemaUtils.getSequence(type);
        
        final String elementPrefix = "this._";
        
        String typeObjectName = nameManager.getJavascriptName(name);
        code.append("//\n");
        code.append("// Constructor for XML Schema item  " + name.toString() + "\n");
        code.append("//\n");
        code.append("function " + typeObjectName + " () {\n");
        for (int i = 0; i < sequence.getItems().getCount(); i++) {
            XmlSchemaObject thing = sequence.getItems().getItem(i);
            constructOneElement(type, sequence, elementPrefix, typeObjectName, thing);
        }
        code.append("}\n\n");
        code.append(accessors.toString());
    }

    private void constructOneElement(XmlSchemaComplexType type, 
                                     XmlSchemaSequence sequence,
                                     final String elementPrefix, 
                                     String typeObjectName, 
                                     XmlSchemaObject thing) {

        XmlSchemaParticle particle = XmlSchemaUtils.getObjectParticle(thing, type);
        XmlSchemaElement sequenceElement = null;
        XmlSchemaType elType = null;
        boolean nillable = false;
        String elementName = null;
        String elementJavascriptVariable = null;
        String defaultValueString = null;
        boolean any = false;
        
        if (particle instanceof XmlSchemaAny) {
            any = true;
            // TODO: what about a collision here?
            elementName = "any" + anyCounter;
            elementJavascriptVariable = elementPrefix + elementName;
            anyCounter++;
        } else {
            sequenceElement = (XmlSchemaElement)thing;
            elType = XmlSchemaUtils.getElementType(xmlSchemaCollection, null, sequenceElement, type);
            nillable = sequenceElement.isNillable();
            if (sequenceElement.isAbstract()) { 
                XmlSchemaUtils.unsupportedConstruct("ABSTRACT_ELEMENT", sequenceElement.getName(), type);
            }
            elementName = sequenceElement.getName();
            elementJavascriptVariable = elementPrefix + sequenceElement.getName();
            defaultValueString = sequenceElement.getDefaultValue();
        }

        
        String accessorSuffix = StringUtils.capitalize(elementName);

        String accessorName = typeObjectName + "_get" + accessorSuffix;
        String getFunctionProperty = typeObjectName + ".prototype.get" + accessorSuffix; 
        String setFunctionProperty = typeObjectName + ".prototype.set" + accessorSuffix; 
        accessors.append("//\n");
        accessors.append("// accessor is " + getFunctionProperty + "\n");
        accessors.append("// element get for " + elementName + "\n");
        if (any) {
            accessors.append("// - xs:any\n");
        } else {
            //  can we get an anonymous type on an element in the middle of a type?
            accessors.append("// - element type is " + elType.getQName() + "\n");
        }
        
        if (XmlSchemaUtils.isParticleOptional(particle)) {
            accessors.append("// - optional element\n");
        } else {
            accessors.append("// - required element\n");
            
        }
        
        if (XmlSchemaUtils.isParticleArray(particle)) {
            accessors.append("// - array\n");
            
        }
        
        if (nillable) {
            accessors.append("// - nillable\n");
        }
        
        accessors.append("//\n");
        accessors.append("// element set for " + elementName + "\n");
        accessors.append("// setter function is is " + setFunctionProperty + "\n");
        accessors.append("//\n");
        accessors.append("function " + accessorName + "() { return " 
                         + elementJavascriptVariable 
                         + ";}\n");
        accessors.append(getFunctionProperty + " = " + accessorName + ";\n");
        accessorName = typeObjectName + "_set" + accessorSuffix;
        accessors.append("function " 
                         + accessorName + "(value) {" 
                         + elementJavascriptVariable
                         + " = value;}\n");
        accessors.append(setFunctionProperty + " = " + accessorName + ";\n");
        
        if (XmlSchemaUtils.isParticleOptional(particle) 
            || (nillable && !XmlSchemaUtils.isParticleArray(particle))) {
            utils.appendLine(elementJavascriptVariable + " = null;");
        } else if (XmlSchemaUtils.isParticleArray(particle)) {
            utils.appendLine(elementJavascriptVariable + " = [];");
        } else if (elType instanceof XmlSchemaComplexType) {
            // even for required complex elements, we leave them null. 
            // otherwise, we could end up in a cycle or otherwise miserable. The 
            // application code is responsible for this.
            utils.appendLine(elementJavascriptVariable + " = null;");
        } else {
            if (defaultValueString == null) {
                defaultValueString = 
                    utils.getDefaultValueForSimpleType(elType);
            }
            utils.appendLine(elementJavascriptVariable + " = " + defaultValueString + ";");
        }
    }
    
    

    /**
     * Produce a serializer function for a type.
     * These functions emit the surrounding element XML if the caller supplies an XML element name.
     * It's not quite as simple as that, though. The element name may need namespace qualification,
     * and this function will add more namespace prefixes as needed.
     * @param type
     * @return
     */
    public void complexTypeSerializerFunction(QName name, XmlSchemaComplexType type) {
        
        StringBuilder bodyCode = new StringBuilder();
        JavascriptUtils bodyUtils = new JavascriptUtils(bodyCode);
        bodyUtils.setXmlStringAccumulator("xml");

        complexTypeSerializerBody(type, "this._", bodyUtils);
        
        utils = new JavascriptUtils(code);
        String functionName = nameManager.getJavascriptName(name) + "_" + "serialize";
        code.append("function " + functionName + "(cxfjsutils, elementName) {\n");
        utils.startXmlStringAccumulator("xml");
        utils.startIf("elementName != null");
        utils.appendString("<");
        utils.appendExpression("elementName");
        // now add any accumulated namespaces.
        String moreNamespaces = prefixAccumulator.getAttributes();
        if (moreNamespaces.length() > 0) {
            utils.appendString(" ");
            utils.appendString(moreNamespaces);
        }
        utils.appendString(">");
        utils.endBlock();
        code.append(bodyCode);
        utils.startIf("elementName != null");
        utils.appendString("</");
        utils.appendExpression("elementName");
        utils.appendString(">");
        utils.endBlock();
        utils.appendLine("return xml;");
        code.append("}\n");
        code.append(nameManager.getJavascriptName(name) + ".prototype.serialize = " + functionName + ";\n");
    }
   

    /**
     * Build the serialization code for a complex type. At the top level, this operates on single items,
     * so it does not pay attention to minOccurs and maxOccurs. However, as it works through the sequence,
     * it manages optional elements and arrays.
     * @param type
     * @param elementPrefix
     * @param bodyNamespaceURIs 
     * @return
     */
    protected void complexTypeSerializerBody(XmlSchemaComplexType type, 
                                             String elementPrefix, 
                                             JavascriptUtils bodyUtils) {

        XmlSchemaSequence sequence = XmlSchemaUtils.getSequence(type);

        // XML Schema, please meet Iterable (not).
        for (int i = 0; i < sequence.getItems().getCount(); i++) {
            XmlSchemaElement sequenceElement = (XmlSchemaElement)sequence.getItems().getItem(i);
            if (sequenceElement.isAbstract()) {
                XmlSchemaUtils.unsupportedConstruct("ABSTRACT_ELEMENT", sequenceElement.getName(), type);
            }
            
            ElementInfo elementInfo = ElementInfo.forLocalElement(sequenceElement, 
                                                                  elementPrefix, 
                                                                  schemaInfo.getSchema(),
                                                                  xmlSchemaCollection, 
                                                                  prefixAccumulator);
            elementInfo.setContainingType(type);
            elementInfo.setUtilsVarName("cxfjsutils");
            bodyUtils.generateCodeToSerializeElement(elementInfo, xmlSchemaCollection);
        }
    }

       /**
     * Generate a JavaScript function that takes an element for a complex type and walks through
     * its children using them to fill in the values for a JavaScript object.
     * @param type schema type for the process
     * @return the string contents of the JavaScript.
     */
    public void domDeserializerFunction(QName name, XmlSchemaComplexType type) {
        utils = new JavascriptUtils(code);
        XmlSchemaSequence sequence = null;
        
        sequence = XmlSchemaUtils.getSequence(type);
        String typeObjectName = nameManager.getJavascriptName(name);
        code.append("function " + typeObjectName + "_deserialize (cxfjsutils, element) {\n");
        // create the object we are deserializing into.
        utils.appendLine("var newobject = new " + typeObjectName + "();");
        utils.appendLine("cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));");
        utils.appendLine("var curElement = cxfjsutils.getFirstElementChild(element);");
        
        utils.appendLine("var item;");
        
        for (int i = 0; i < sequence.getItems().getCount(); i++) {
            utils.appendLine("cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));");
            XmlSchemaObject thing = sequence.getItems().getItem(i);
            if (!(thing instanceof XmlSchemaElement)) {
                XmlSchemaUtils.unsupportedConstruct("NON_ELEMENT_CHILD", 
                                                    thing.getClass().getSimpleName(), type);
            }
            
            XmlSchemaElement sequenceElement = (XmlSchemaElement)thing;
            XmlSchemaElement realElement = XmlSchemaUtils.getReferredElement(sequenceElement, 
                                                                             xmlSchemaCollection);
            boolean global = realElement != null;
            if (!global) {
                realElement = sequenceElement;
            }
            XmlSchemaType elType = XmlSchemaUtils.getElementType(xmlSchemaCollection, 
                                                                 null, realElement, type);
            boolean simple = elType instanceof XmlSchemaSimpleType;
            String accessorName = "set" + StringUtils.capitalize(realElement.getName()); 
            String elementName = realElement.getName();
            utils.appendLine("cxfjsutils.trace('processing " + elementName + "');");
            String elementNamespaceURI = realElement.getQName().getNamespaceURI();
            boolean elementNoNamespace = "".equals(elementNamespaceURI);
            XmlSchema elementSchema = null;
            if (!elementNoNamespace) {
                elementSchema = xmlSchemaCollection.getSchemaByTargetNamespace(elementNamespaceURI);
            }
            boolean qualified = !elementNoNamespace
                && XmlSchemaUtils.isElementQualified(realElement, 
                                                     global, 
                                                     schemaInfo.getSchema(),
                                                     elementSchema);
            
            if (!qualified) {
                elementNamespaceURI = "";
            }
                
            String valueTarget = "item";

            if (XmlSchemaUtils.isParticleOptional(sequenceElement) 
                || XmlSchemaUtils.isParticleArray(sequenceElement)) {
                utils.startIf("curElement != null && cxfjsutils.isNodeNamedNS(curElement, '" 
                              + elementNamespaceURI 
                              + "', '" 
                              + elementName
                              + "')");
                if (XmlSchemaUtils.isParticleArray(sequenceElement)) {
                    utils.appendLine("item = [];");
                    utils.startDo();
                    valueTarget = "arrayItem";
                    utils.appendLine("var arrayItem;");
                }
            }
                
            utils.appendLine("var value = null;");
            utils.startIf("!cxfjsutils.isElementNil(curElement)");
            if (simple) {
                utils.appendLine("value = cxfjsutils.getNodeText(curElement);");
                utils.appendLine(valueTarget 
                                 + " = " + utils.javascriptParseExpression(elType, "value") 
                                 + ";");
            } else {
                XmlSchemaComplexType complexType = (XmlSchemaComplexType)elType;
                QName baseQName = complexType.getQName();
                if (baseQName == null) {
                    baseQName = realElement.getQName();
                }
                
                String elTypeJsName = nameManager.getJavascriptName(baseQName);
                utils.appendLine(valueTarget + " = " 
                                 + elTypeJsName 
                                 + "_deserialize(cxfjsutils, curElement);");
            }
             
            utils.endBlock(); // the if for the nil.
            if (XmlSchemaUtils.isParticleArray(sequenceElement)) {
                utils.appendLine("item.push(arrayItem);");
                utils.appendLine("curElement = cxfjsutils.getNextElementSibling(curElement);");
                utils.endBlock();
                utils.appendLine("  while(curElement != null && cxfjsutils.isNodeNamedNS(curElement, '" 
                                  + elementNamespaceURI + "', '" 
                                  + sequenceElement.getName() + "'));");
            }
            utils.appendLine("newobject." + accessorName + "(item);");
            if (!XmlSchemaUtils.isParticleArray(sequenceElement)) {
                utils.startIf("curElement != null");
                utils.appendLine("curElement = cxfjsutils.getNextElementSibling(curElement);");
                utils.endBlock();
            }
            if (XmlSchemaUtils.isParticleOptional(sequenceElement) 
                || XmlSchemaUtils.isParticleArray(sequenceElement)) {
                utils.endBlock();
            }
        }
        utils.appendLine("return newobject;");
        code.append("}\n\n");
    }
}
