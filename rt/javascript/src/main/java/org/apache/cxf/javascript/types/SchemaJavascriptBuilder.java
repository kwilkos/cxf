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

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.javascript.JavascriptUtils;
import org.apache.cxf.javascript.NameManager;
import org.apache.cxf.javascript.NamespacePrefixAccumulator;
import org.apache.cxf.javascript.UnsupportedConstruct;
import org.apache.cxf.javascript.XmlSchemaUtils;
import org.apache.cxf.service.model.SchemaInfo;
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
    private SchemaInfo schemaInfo;
    
    public SchemaJavascriptBuilder(SchemaCollection schemaCollection,
                                   NameManager nameManager, 
                                   SchemaInfo schemaInfo) {
        this.xmlSchemaCollection = schemaCollection;
        this.nameManager = nameManager;
        this.schemaInfo = schemaInfo;
    }
    
    public String generateCodeForSchema(SchemaInfo schema) {
        StringBuffer code = new StringBuffer();
        code.append("//\n");
        code.append("// Definitions for schema: " + schema.toString() + "\n");
        code.append("//\n");

        XmlSchemaObjectTable schemaTypes = schema.getSchema().getSchemaTypes();
        Iterator namesIterator = schemaTypes.getNames();
        while (namesIterator.hasNext()) {
            QName name = (QName)namesIterator.next();
            XmlSchemaObject xmlSchemaObject = (XmlSchemaObject)schemaTypes.getItem(name);
            if (xmlSchemaObject instanceof XmlSchemaComplexType) {
                try {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType)xmlSchemaObject;
                    code.append(complexTypeConstructorAndAccessors(complexType));
                    code.append(complexTypeSerializerFunction(complexType));
                    code.append(domDeserializerFunction(complexType));
                } catch (UnsupportedConstruct usc) {
                    continue; // it could be empty, but the style checker would complain.
                }
            }
        }
        
        String returnValue = code.toString();
        LOG.finer(returnValue);
        return returnValue;
    }
    
    
    
    public String complexTypeConstructorAndAccessors(XmlSchemaComplexType type) {
        StringBuilder code = new StringBuilder();
        StringBuilder accessors = new StringBuilder();
        JavascriptUtils utils = new JavascriptUtils(code);
        XmlSchemaSequence sequence = XmlSchemaUtils.getSequence(type);
        
        final String elementPrefix = "this._";
        
        String typeObjectName = nameManager.getJavascriptName(type);
        code.append("function " + typeObjectName + " () {\n");
        
        for (int i = 0; i < sequence.getItems().getCount(); i++) {
            XmlSchemaObject thing = sequence.getItems().getItem(i);
            if (!(thing instanceof XmlSchemaElement)) {
                XmlSchemaUtils.unsupportedConstruct("NON_ELEMENT_CHILD", 
                                                    thing.getClass().getSimpleName(), type);
            }
            
            XmlSchemaElement elChild = (XmlSchemaElement)thing;
            XmlSchemaType elType = XmlSchemaUtils.getElementType(xmlSchemaCollection, null, elChild, type);

            boolean nillable = elChild.isNillable();
            if (elChild.isAbstract()) { 
                XmlSchemaUtils.unsupportedConstruct("ABSTRACT_ELEMENT", elChild.getName(), type);
            }
            
            // Assume that no lunatic has created multiple elements that differ only by namespace.
            // if elementForm is unqualified, how can that be valid?
            String elementName = elementPrefix + elChild.getName();
            String accessorSuffix = StringUtils.capitalize(elChild.getName());

            String accessorName = typeObjectName + "_get" + accessorSuffix;
            accessors.append("function " + accessorName + "() { return " + elementName + ";}\n");
            accessors.append(typeObjectName + ".prototype.get" 
                             + accessorSuffix + " = " + accessorName + ";\n");
            
            accessorName = typeObjectName + "_set" + accessorSuffix;
            accessors.append("function " 
                             + accessorName + "(value) {" + elementName + " = value;}\n");
            accessors.append(typeObjectName 
                             + ".prototype.set" + accessorSuffix + " = " + accessorName + ";\n");
            
            if (XmlSchemaUtils.isParticleOptional(elChild) 
                || (nillable && !XmlSchemaUtils.isParticleArray(elChild))) {
                utils.appendLine(elementName + " = null;");
            } else if (XmlSchemaUtils.isParticleArray(elChild)) {
                utils.appendLine(elementName + " = [];");
            } else if (elType instanceof XmlSchemaComplexType) {
                // even for required complex elements, we leave them null. 
                // otherwise, we could end up in a cycle or otherwise miserable. The 
                // application code is responsible for this.
                utils.appendLine(elementName + " = null;");
            } else {
                String defaultValueString = elChild.getDefaultValue();
                if (defaultValueString == null) {
                    defaultValueString = 
                        utils.getDefaultValueForSimpleType(elType);
                }
                utils.appendLine(elementName + " = " + defaultValueString + ";");
            }
        }
        code.append("}\n");
        return code.toString() + "\n" + accessors.toString();
    }
    
    

    /**
     * Produce a serializer function for a type.
     * These functions emit the surrounding element XML if the caller supplies an XML element name.
     * It's not quite as simple as that, though. The element name may need namespace qualification,
     * and this function will add more namespace prefixes as needed.
     * @param type
     * @return
     */
    public String complexTypeSerializerFunction(XmlSchemaComplexType type) {
        
        StringBuilder bodyCode = new StringBuilder();
        JavascriptUtils bodyUtils = new JavascriptUtils(bodyCode);
        bodyUtils.setXmlStringAccumulator("xml");

        NamespacePrefixAccumulator prefixAccumulator = new NamespacePrefixAccumulator(schemaInfo);
        complexTypeSerializerBody(type, "this._", bodyUtils, prefixAccumulator);
        
        StringBuilder code = new StringBuilder();
        JavascriptUtils utils = new JavascriptUtils(code);
        String functionName = nameManager.getJavascriptName(type) + "_" + "serialize";
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
        code.append("return xml;\n");
        code.append("}\n");

        code.append(nameManager.getJavascriptName(type) + ".prototype.serialize = " + functionName + ";\n");
        return code.toString();
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
                                          JavascriptUtils utils, 
                                          NamespacePrefixAccumulator prefixAccumulator) {

        XmlSchemaSequence sequence = XmlSchemaUtils.getSequence(type);

        // XML Schema, please meet Iterable (not).
        for (int i = 0; i < sequence.getItems().getCount(); i++) {
            XmlSchemaElement elChild = (XmlSchemaElement)sequence.getItems().getItem(i);
            if (elChild.isAbstract()) {
                XmlSchemaUtils.unsupportedConstruct("ABSTRACT_ELEMENT", elChild.getName(), type);
            }
            
            // assume that no lunatic has created multiple elements that differ only by namespace.
            // or, perhaps, detect that when generating the parser?
            String elementName = elementPrefix + elChild.getName();
            String elementXmlRef = prefixAccumulator.xmlElementString(elChild);
            
            utils.generateCodeToSerializeElement("cxfjsutils", elChild, elementName, 
                                                 elementXmlRef, xmlSchemaCollection, null, type);
        }
    }
    /**
     * Generate a JavaScript function that takes an element for a complex type and walks through
     * its children using them to fill in the values for a JavaScript object.
     * @param type schema type for the process
     * @return the string contents of the JavaScript.
     */
    public String domDeserializerFunction(XmlSchemaComplexType type) {
        StringBuilder code = new StringBuilder();
        JavascriptUtils utils = new JavascriptUtils(code);
        XmlSchemaParticle particle = type.getParticle();
        XmlSchemaSequence sequence = null;
        
        if (particle == null) {
            XmlSchemaUtils.unsupportedConstruct("NULL_PARTICLE", type);
        }
        
        try {
            sequence = (XmlSchemaSequence) particle;
        } catch (ClassCastException cce) {
            XmlSchemaUtils.unsupportedConstruct("NON_SEQUENCE_PARTICLE", type);
        }
        
        String typeObjectName = nameManager.getJavascriptName(type);
        code.append("function " + typeObjectName + "_deserialize (cxfjsutils, element) {\n");
        // create the object we are deserializing into.
        utils.appendLine("var newobject = new " + typeObjectName + "();");
        
        utils.appendLine("var curElement = cxfjsutils.getFirstElementChild(element);");
        utils.appendLine("var item;");
        
        for (int i = 0; i < sequence.getItems().getCount(); i++) {
            utils.appendLine("cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));");
            XmlSchemaObject thing = sequence.getItems().getItem(i);
            if (!(thing instanceof XmlSchemaElement)) {
                XmlSchemaUtils.unsupportedConstruct("NON_ELEMENT_CHILD", 
                                                    thing.getClass().getSimpleName(), type);
            }
            
            XmlSchemaElement elChild = (XmlSchemaElement)thing;
            XmlSchemaType elType = XmlSchemaUtils.getElementType(xmlSchemaCollection, null, elChild, type);
            boolean simple = elType instanceof XmlSchemaSimpleType;

            String accessorName = "set" + StringUtils.capitalize(elChild.getName()); 
            // For optional or an array, we need to check if the element is the 
            // one we want.
            
            String elementName = elChild.getName();
            utils.appendLine("cxfjsutils.trace('processing " + elementName + "');");
            String elementNamespaceURI = XmlSchemaUtils.getElementQualifier(schemaInfo, elChild);
            
            String valueTarget = "item";

            if (XmlSchemaUtils.isParticleOptional(elChild) || XmlSchemaUtils.isParticleArray(elChild)) {
                utils.startIf("curElement != null && cxfjsutils.isNodeNamedNS(curElement, '" 
                              + elementNamespaceURI 
                              + "', '" 
                              + elementName
                              + "')");
                if (XmlSchemaUtils.isParticleArray(elChild)) {
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
                String elTypeJsName = nameManager.getJavascriptName((XmlSchemaComplexType)elType);
                utils.appendLine(valueTarget + " = " 
                                 + elTypeJsName 
                                 + "_deserialize(cxfjsutils, curElement);");
            }
             
            utils.endBlock(); // the if for the nil.
            if (XmlSchemaUtils.isParticleArray(elChild)) {
                utils.appendLine("item.push(arrayItem);");
                utils.appendLine("curElement = cxfjsutils.getNextElementSibling(curElement);");
                utils.endBlock();
                utils.appendLine("  while(curElement != null && cxfjsutils.isNodeNamedNS(curElement, '" 
                                  + elementNamespaceURI + "', '" 
                                  + elChild.getName() + "'));");
            }
            utils.appendLine("newobject." + accessorName + "(item);");
            if (!XmlSchemaUtils.isParticleArray(elChild)) {
                utils.startIf("curElement != null");
                utils.appendLine("curElement = cxfjsutils.getNextElementSibling(curElement);");
                utils.endBlock();
            }
            if (XmlSchemaUtils.isParticleOptional(elChild) || XmlSchemaUtils.isParticleArray(elChild)) {
                utils.endBlock();
            }
        }
        utils.appendLine("return newobject;");
        code.append("}\n");
        return code.toString() + "\n";
    }
}
