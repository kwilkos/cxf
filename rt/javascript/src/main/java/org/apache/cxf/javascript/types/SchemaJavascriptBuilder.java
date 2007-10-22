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

import javax.xml.namespace.QName;

import org.apache.cxf.javascript.JavascriptUtils;
import org.apache.cxf.javascript.UnsupportedSchemaConstruct;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * Generate Javascript for a schema, and provide information needed for the service builder.
 * As of this pass, there is no support for non-sequence types or for attribute mappings.
 * @author bimargulies
 */
public class SchemaJavascriptBuilder {
    private static final XmlSchemaForm QUALIFIED = new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
    private static final String XSI_NS_ATTR = WSDLConstants.NP_XMLNS + ":" 
        + WSDLConstants.NP_SCHEMA_XSI + "=" + WSDLConstants.NU_SCHEMA_XSI;
    private static final String NIL_ATTRIBUTES = XSI_NS_ATTR + " xsi:nil='true'";
    // The schema that we are operating upon.
    private SchemaInfo schemaInfo;
    
    public SchemaJavascriptBuilder(SchemaInfo schemaInfo) {
        this.schemaInfo = schemaInfo;
    }
    
    public static boolean isParticleArray(XmlSchemaParticle particle) {
        return particle.getMaxOccurs() > 1;
    }
    
    public static boolean isParticleOptional(XmlSchemaParticle particle) {
        return particle.getMinOccurs() == 0 && particle.getMaxOccurs() == 1;
    }
    
    private String xmlElementString(XmlSchemaElement element) {
        return getQNameString(element.getQName());
    }
    
    private String getQNameString(QName name) {
        if (schemaInfo.getSchema().getElementFormDefault().equals(QUALIFIED)) {
            return getPrefix(name.getNamespaceURI()) + ":" + name.getLocalPart();
        } else {
            return name.getLocalPart();
        }

    }

    private String getPrefix(String string) {
        return schemaInfo.getSchema().getNamespaceContext().getPrefix(string);
    }

    /**
     * Build the serialization code for a complex type. At the top level, this operates on single items,
     * so it does not pay attention to minOccurs and maxOccurs. However, as it works through the sequence,
     * it manages optional elements and arrays.
     * @param type
     * @param elementPrefix
     * @return
     */
    public void complexTypeSerializerBody(XmlSchemaComplexType type, 
                                          String elementPrefix, 
                                          StringBuffer code) {
        JavascriptUtils utils = new JavascriptUtils(code);
        XmlSchemaParticle particle = type.getParticle();
        XmlSchemaSequence sequence = null;
        try {
            sequence = (XmlSchemaSequence) particle;
        } catch (ClassCastException cce) {
            throw new UnsupportedSchemaConstruct("Cannot build serializer for " + particle.toString());
        }
        
        utils.startXmlStringAccumulator("xml");

        // XML Schema, please meet Iterable (not).
        for (int i = 0; i < sequence.getItems().getCount(); i++) {
            XmlSchemaElement elChild = (XmlSchemaElement)sequence.getItems().getItem(i);
            XmlSchemaType elType = elChild.getSchemaType();
            boolean nillable = elChild.isNillable();
            if (elChild.isAbstract()) { 
                throw new UnsupportedSchemaConstruct("Abstract element " + elChild.toString());
            }
            
            // assume that no lunatic has created multiple elements that differ only by namespace.
            // or, perhaps, detect that when generating the parser?
            String elementName = elementPrefix + elChild.getName();
            String elementXmlRef = xmlElementString(elChild);
            
            // first question: optional?
            if (isParticleOptional(elChild)) {
                utils.startIf(elementName + " != null");
            }
            
            // nillable and optional would be very strange together.
            // and nillable in the array case can't be right, can it?
            if (nillable && !isParticleArray(elChild)) {
                utils.startIf(elementName + " == null");
                utils.appendAppend("<" + elementXmlRef + " " + NIL_ATTRIBUTES + "/>");
                utils.appendElse();
            }
            
            if (isParticleArray(elChild)) {
                utils.startFor("var ax = 0", "ax < " +  elementName + ".length", "ax ++");
                elementName = elementName + "[ax]";
                // we need an extra level of 'nil' testing here. Or do we, depending on the type structure?
                // Recode and fiddle appropriately.
                utils.startIf(elementName + " == null");
                utils.appendAppend("<" + elementXmlRef + " " + NIL_ATTRIBUTES + "/>");
                utils.appendElse();
            }
            
            // now for the thing itself.
            utils.appendAppend("<" + elementXmlRef + ">");
            if (elType instanceof XmlSchemaComplexType) {
                utils.appendAppend(elementName + ".serialize()");
            } else {
                // warning: this assumes that ordinary Javascript serialization is all we need.
                // except for &gt; ad all of that.
                utils.appendAppend("cxf_xml_serialize_string(" + elementName + ")");
            }
            utils.appendAppend("</" + elementXmlRef + ">");
            
            if (isParticleArray(elChild)) {
                utils.endBlock(); // for the extra level of nil checking, which might be wrong.
                utils.endBlock(); // for the for loop.
            }
            
            if (nillable && !isParticleArray(elChild)) {
                utils.endBlock();
            }
            
            if (isParticleOptional(elChild)) {
                utils.endBlock();
            }
        }
    }
}
