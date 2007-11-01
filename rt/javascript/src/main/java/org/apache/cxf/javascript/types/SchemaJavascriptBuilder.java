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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.javascript.JavascriptUtils;
import org.apache.cxf.javascript.NameManager;
import org.apache.cxf.javascript.UnsupportedSchemaConstruct;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
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
    
    private static final XmlSchemaForm QUALIFIED = new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
    private static final XmlSchemaForm UNQUALIFIED = new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
    
    private static final String XSI_NS_ATTR = WSDLConstants.NP_XMLNS + ":" 
        + WSDLConstants.NP_SCHEMA_XSI + "='" + WSDLConstants.NU_SCHEMA_XSI + "'";
    private static final String NIL_ATTRIBUTES = XSI_NS_ATTR + " xsi:nil='true'";
    private XmlSchemaCollection xmlSchemaCollection;
    private SchemaInfo schemaInfo;
    private NameManager nameManager;
    private Map<String, String> fallbackNamespacePrefixMap;
    private int nsCounter;
    
    public SchemaJavascriptBuilder(XmlSchemaCollection schemaCollection,
                                   NameManager nameManager, 
                                   SchemaInfo schemaInfo) {
        this.xmlSchemaCollection = schemaCollection;
        this.nameManager = nameManager;
        this.schemaInfo = schemaInfo;
        fallbackNamespacePrefixMap = new HashMap<String, String>();
    }
    
    // this class assumes that the rest of the code is not going to try to use the same prefix twice
    // for two different URIs.
    private static class NamespacePrefixAccumulator {
        private StringBuffer attributes;
        private Set<String> prefixes;
        
        NamespacePrefixAccumulator() {
            attributes = new StringBuffer();
            prefixes = new HashSet<String>();
        }
        
        void collect(String prefix, String uri) {
            if (!prefixes.contains(prefix)) {
                attributes.append("xmlns:" + prefix + "='" + uri + "' ");
                prefixes.add(prefix);
            }
        }
        
        String getAttributes() {
            return attributes.toString();
        }
    }
    
    private String cleanedUpSchemaSource(XmlSchemaType subject) {
        if (subject.getSourceURI() == null) {
            return "";
        } else {
            return subject.getSourceURI() + ":" + subject.getLineNumber(); 
        }
    }
    
    private void unsupportedConstruct(String messageKey, XmlSchemaType subject) {
        Message message = new Message(messageKey, LOG, subject.getQName(), 
                                      cleanedUpSchemaSource(subject));
        throw new UnsupportedSchemaConstruct(message);
        
    }
    
    private void unsupportedConstruct(String messageKey, String what, XmlSchemaType subject) {
        Message message = new Message(messageKey, LOG, what, subject.getQName(), 
                                      cleanedUpSchemaSource(subject));
        LOG.severe(message.toString());
        throw new UnsupportedSchemaConstruct(message);
        
    }
    
    public static boolean isParticleArray(XmlSchemaParticle particle) {
        return particle.getMaxOccurs() > 1;
    }
    
    public static boolean isParticleOptional(XmlSchemaParticle particle) {
        return particle.getMinOccurs() == 0 && particle.getMaxOccurs() == 1;
    }
    
    /**
     * Return an empty string if this element should be unqualified in XML
     * or the namespace URI if it should be qualified. 
     * @param element
     * @return
     */
    private String getElementQualifier(XmlSchemaElement element) {
        QName qname;
        boolean forceQualification = false;
        // JAXB ends up with no form='qualified', but we qualify anyway if the namespace don't
        // match.
        if (element.getRefName() != null) {
            qname = element.getRefName();
            forceQualification = !qname.getNamespaceURI().equals(schemaInfo.getNamespaceURI());
        } else {
            qname = element.getQName();
        }
        // some elements don't have qnames, only local names.
        // one hopes that we aren't called upon to produce a qualified form for such an element, though
        // perhaps we're supposed to pull the TNS out of a hat.
        if (forceQualification || isElementNameQualified(element)) {
            return qname.getNamespaceURI();
        } else {
            return "";
        }
    }
    
    /**
     * This function obtains a name, perhaps namespace-qualified, for an element.
     * It also maintains a Map that records all the prefixes used in the course
     * of working on a single serializer function (and thus a single complex-type-element
     * XML element) which is used for namespace prefix management.
     * @param element
     * @param namespaceMap
     * @return
     */
    private String xmlElementString(XmlSchemaElement element, NamespacePrefixAccumulator accumulator) {
        String namespaceURI = getElementQualifier(element);
        if ("".equals(namespaceURI)) {
            return element.getName(); // use the non-qualified name.
        } else {
            // What if there were a prefix in the element's qname? This is not apparently 
            // something that happens in this environment.
            String prefix = getPrefix(namespaceURI);
            accumulator.collect(prefix, namespaceURI);
            return prefix + ":" + element.getName();
        }
    }
    
    private boolean isElementNameQualified(XmlSchemaElement element) {
        if (element.getForm().equals(QUALIFIED)) {
            return true;
        }
        if (element.getForm().equals(UNQUALIFIED)) {
            return false;
        }
        return schemaInfo.getSchema().getElementFormDefault().equals(QUALIFIED);
    }
    
    private String getPrefix(String namespaceURI) {
        String schemaPrefix = schemaInfo.getSchema().getNamespaceContext().getPrefix(namespaceURI);
        if (schemaPrefix == null || "tns".equals(schemaPrefix)) {
            schemaPrefix = fallbackNamespacePrefixMap.get(namespaceURI);
            if (schemaPrefix == null) {
                schemaPrefix = "jns" + nsCounter;
                nsCounter++;
                fallbackNamespacePrefixMap.put(namespaceURI, schemaPrefix);
            }
        }
        return schemaPrefix;
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
                } catch (UnsupportedSchemaConstruct usc) {
                    continue; // it could be empty, but the style checker would complain.
                }
            }
        }
        
        return code.toString();
    }
    
    public String complexTypeConstructorAndAccessors(XmlSchemaComplexType type) {
        StringBuffer code = new StringBuffer();
        StringBuffer accessors = new StringBuffer();
        JavascriptUtils utils = new JavascriptUtils(code);
        XmlSchemaParticle particle = type.getParticle();
        XmlSchemaSequence sequence = null;
        final String elementPrefix = "this._";
        
        String typeObjectName = nameManager.getJavascriptName(type);
        code.append("function " + typeObjectName + " () {\n");
        
        if (particle == null) {
            unsupportedConstruct("NULL_PARTICLE", type);
        }
        
        try {
            sequence = (XmlSchemaSequence) particle;
        } catch (ClassCastException cce) {
            unsupportedConstruct("NON_SEQUENCE_PARTICLE", type);
        }
        
        for (int i = 0; i < sequence.getItems().getCount(); i++) {
            XmlSchemaObject thing = sequence.getItems().getItem(i);
            if (!(thing instanceof XmlSchemaElement)) {
                unsupportedConstruct("NON_ELEMENT_CHILD", thing.getClass().getSimpleName(), type);
            }
            
            XmlSchemaElement elChild = (XmlSchemaElement)thing;
            XmlSchemaType elType = getElementType(type, elChild);

            boolean nillable = elChild.isNillable();
            if (elChild.isAbstract()) { 
                unsupportedConstruct("ABSTRACT_ELEMENT", elChild.getName(), type);
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
            
            if (isParticleOptional(elChild) || (nillable && !isParticleArray(elChild))) {
                utils.appendLine(elementName + " = null;");
            } else if (isParticleArray(elChild)) {
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
     * Follow a chain of references from element to element until we can obtain a type.
     * @param element
     * @return
     */
    private XmlSchemaType getElementType(XmlSchemaComplexType containingType, XmlSchemaElement element) {
        XmlSchemaElement originalElement = element;
        while (element.getSchemaType() == null && element.getRefName() != null) {
            element = xmlSchemaCollection.getElementByQName(element.getRefName());
        }
        if (element.getSchemaType() == null) {
            unsupportedConstruct("ELEMENT_HAS_NO_TYPE", originalElement.getName(), containingType);
        }
        return element.getSchemaType();
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
        
        StringBuffer bodyCode = new StringBuffer();
        JavascriptUtils bodyUtils = new JavascriptUtils(bodyCode);
        bodyUtils.setXmlStringAccumulator("xml");

        NamespacePrefixAccumulator prefixAccumulator = new NamespacePrefixAccumulator();
        complexTypeSerializerBody(type, "this._", bodyUtils, prefixAccumulator);
        
        StringBuffer code = new StringBuffer();
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

        XmlSchemaParticle particle = type.getParticle();
        XmlSchemaSequence sequence = null;
        sequence = (XmlSchemaSequence) particle;

        // XML Schema, please meet Iterable (not).
        for (int i = 0; i < sequence.getItems().getCount(); i++) {
            XmlSchemaElement elChild = (XmlSchemaElement)sequence.getItems().getItem(i);
            XmlSchemaType elType = getElementType(type, elChild);
            boolean nillable = elChild.isNillable();
            if (elChild.isAbstract()) {
                unsupportedConstruct("ABSTRACT_ELEMENT", elChild.getName(), type);
            }
            
            // assume that no lunatic has created multiple elements that differ only by namespace.
            // or, perhaps, detect that when generating the parser?
            String elementName = elementPrefix + elChild.getName();
            String elementXmlRef = xmlElementString(elChild, prefixAccumulator);
            
            // first question: optional?
            if (isParticleOptional(elChild)) {
                utils.startIf(elementName + " != null");
            }
            
            // nillable and optional would be very strange together.
            // and nillable in the array case applies to the elements.
            if (nillable && !isParticleArray(elChild)) {
                utils.startIf(elementName + " == null");
                utils.appendString("<" + elementXmlRef + " " + NIL_ATTRIBUTES + "/>");
                utils.appendElse();
            }
            
            if (isParticleArray(elChild)) {
                // protected against null in arrays.
                utils.startIf(elementName + " != null");
                utils.startFor("var ax = 0", "ax < " +  elementName + ".length", "ax ++");
                elementName = elementName + "[ax]";
                // we need an extra level of 'nil' testing here. Or do we, depending on the type structure?
                // Recode and fiddle appropriately.
                utils.startIf(elementName + " == null");
                utils.appendString("<" + elementXmlRef + " " + NIL_ATTRIBUTES + "/>");
                utils.appendElse();
            }
            
            // now for the thing itself.
            if (elType instanceof XmlSchemaComplexType) {
                utils.appendExpression(elementName + ".serialize(cxfjsutils, " + elementXmlRef + ")");
            } else {
                QName typeName = elType.getQName();
                utils.appendString("<" + elementXmlRef + ">");
                // warning: this assumes that ordinary Javascript serialization is all we need.
                // except for &gt; ad all of that.
                if (utils.isStringSimpleType(typeName)) {
                    utils.appendExpression("cxfjsutils.escapeXmlEntities(" + elementName + ")");
                } else {
                    utils.appendExpression(elementName);
                }
                utils.appendString("</" + elementXmlRef + ">");
            }
            
            if (isParticleArray(elChild)) {
                utils.endBlock(); // for the extra level of nil checking, which might be wrong.
                utils.endBlock(); // for the for loop.
                utils.endBlock(); // the null protection.
            }
            
            if (nillable && !isParticleArray(elChild)) {
                utils.endBlock();
            }
            
            if (isParticleOptional(elChild)) {
                utils.endBlock();
            }
        }
    }
    /**
     * Generate a JavaScript function that takes an element for a complex type and walks through
     * its children using them to fill in the values for a JavaScript object.
     * @param type schema type for the process
     * @return the string contents of the JavaScript.
     */
    public String domDeserializerFunction(XmlSchemaComplexType type) {
        StringBuffer code = new StringBuffer();
        JavascriptUtils utils = new JavascriptUtils(code);
        XmlSchemaParticle particle = type.getParticle();
        XmlSchemaSequence sequence = null;
        
        if (particle == null) {
            unsupportedConstruct("NULL_PARTICLE", type);
        }
        
        try {
            sequence = (XmlSchemaSequence) particle;
        } catch (ClassCastException cce) {
            unsupportedConstruct("NON_SEQUENCE_PARTICLE", type);
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
                unsupportedConstruct("NON_ELEMENT_CHILD", thing.getClass().getSimpleName(), type);
            }
            
            XmlSchemaElement elChild = (XmlSchemaElement)thing;
            XmlSchemaType elType = getElementType(type, elChild);
            boolean simple = elType instanceof XmlSchemaSimpleType;

            String accessorName = "set" + StringUtils.capitalize(elChild.getName()); 
            // For optional or an array, we need to check if the element is the 
            // one we want.
            
            String elementName = elChild.getName();
            utils.appendLine("cxfjsutils.trace('processing " + elementName + "');");
            String elementNamespaceURI = getElementQualifier(elChild);
            
            String valueTarget = "item";

            if (isParticleOptional(elChild) || isParticleArray(elChild)) {
                utils.startIf("curElement != null && cxfjsutils.isNodeNamedNS(curElement, '" 
                              + elementNamespaceURI 
                              + "', '" 
                              + elementName
                              + "')");
                if (isParticleArray(elChild)) {
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
            if (isParticleArray(elChild)) {
                utils.appendLine("item.push(arrayItem);");
                utils.endBlock();
                utils.appendLine("while(cfjsutils.isNodeNamedNS(curElement, '" 
                                  + elementNamespaceURI + "', '" 
                                  + elChild.getName() + "'));");
            }
            utils.appendLine("newobject." + accessorName + "(item);");
            utils.appendLine("curElement = cxfjsutils.getNextElementSibling(curElement);");
            if (isParticleOptional(elChild) || isParticleArray(elChild)) {
                utils.endBlock();
            }
        }
        utils.appendLine("return newobject;");
        code.append("}\n");
        return code.toString() + "\n";
    }
}
