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

import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * 
 */
public final class XmlSchemaUtils {
    public static final XmlSchemaForm QUALIFIED = new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
    public static final XmlSchemaForm UNQUALIFIED = new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
    
    public static final String XSI_NS_ATTR = WSDLConstants.NP_XMLNS + ":" 
                    + WSDLConstants.NP_SCHEMA_XSI + "='" + WSDLConstants.NS_SCHEMA_XSI + "'";
    public static final String NIL_ATTRIBUTES = XSI_NS_ATTR + " xsi:nil='true'";

    private static final Logger LOG = LogUtils.getL7dLogger(XmlSchemaUtils.class);
    
    private XmlSchemaUtils() {
    }
    
    private static String cleanedUpSchemaSource(XmlSchemaType subject) {
        if (subject.getSourceURI() == null) {
            return "";
        } else {
            return subject.getSourceURI() + ":" + subject.getLineNumber(); 
        }
    }
    
    public static void unsupportedConstruct(String messageKey, XmlSchemaType subject) {
        Message message = new Message(messageKey, LOG, subject.getQName(), 
                                      cleanedUpSchemaSource(subject));
        throw new UnsupportedConstruct(message);
        
    }
    
    public static void unsupportedConstruct(String messageKey, String what, XmlSchemaType subject) {
        Message message = new Message(messageKey, LOG, what, subject.getQName(),
                                      subject == null ? "(global)" 
                                          : cleanedUpSchemaSource(subject));
        LOG.severe(message.toString());
        throw new UnsupportedConstruct(message);
        
    }

    
    public static XmlSchemaSequence getSequence(XmlSchemaComplexType type) {
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
        
        return sequence;
    }
    
    /**
     * This copes with an observed phenomenon in the schema built by the ReflectionServiceFactoryBean. It 
     * is creating element such that: (a) the type is not set. (b) the refName is set. 
     * (c) the namespaceURI in the refName is set empty. This apparently indicates 
     * 'same Schema' to everyone else, so thus function implements
     * that convention here. It is unclear if that is a correct structure, 
     * and it if changes, we can simplify or eliminate this function.
     * @param name
     * @param referencingURI
     * @return
     */
    public static XmlSchemaElement findElementByRefName(XmlSchemaCollection xmlSchemaCollection,
                                                         QName name, 
                                                         String referencingURI) {
        String uri = name.getNamespaceURI();
        if ("".equals(uri)) {
            uri = referencingURI;
        }
        QName copyName = new QName(uri, name.getLocalPart());
        XmlSchemaElement target = xmlSchemaCollection.getElementByQName(copyName);
        assert target != null;
        return target;
    }
    
    
    /**
     * Follow a chain of references from element to element until we can obtain a type.
     * @param element
     * @return
     */
    public static XmlSchemaType getElementType(XmlSchemaCollection xmlSchemaCollection,
                                               String referencingURI, 
                                               XmlSchemaElement element,
                                               XmlSchemaType containingType) {
        assert element != null;
        if (referencingURI == null && containingType != null) {
            referencingURI = containingType.getQName().getNamespaceURI();
        }
        XmlSchemaElement originalElement = element;
        while (element.getSchemaType() == null && element.getRefName() != null) {
            XmlSchemaElement nextElement = findElementByRefName(xmlSchemaCollection,
                                                                element.getRefName(), 
                                                                referencingURI);
            assert nextElement != null;
            element = nextElement;
        }
        if (element.getSchemaType() == null) {
            XmlSchemaUtils.unsupportedConstruct("ELEMENT_HAS_NO_TYPE", originalElement.getName(), 
                                                containingType);
        }
        return element.getSchemaType();
    }
    
    public static boolean isComplexType(XmlSchemaType type) {
        return type instanceof XmlSchemaComplexType;
    }
    
    public static boolean isElementNameQualified(XmlSchemaElement element, SchemaInfo schemaInfo) {
        if (element.getForm().equals(QUALIFIED)) {
            return true;
        }
        if (element.getForm().equals(UNQUALIFIED)) {
            return false;
        }
        return schemaInfo.getSchema().getElementFormDefault().equals(QUALIFIED);
    }
    
    /**
     * Return an empty string if this element should be unqualified in XML
     * or the namespace URI if it should be qualified. 
     * @param element
     * @return
     */
    public static String getElementQualifier(SchemaInfo schemaInfo, XmlSchemaElement element) {
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
        if (forceQualification || isElementNameQualified(element, schemaInfo)) {
            return qname.getNamespaceURI();
        } else {
            return "";
        }
    }
    
    public static boolean isParticleArray(XmlSchemaParticle particle) {
        return particle.getMaxOccurs() > 1;
    }
    
    public static boolean isParticleOptional(XmlSchemaParticle particle) {
        return particle.getMinOccurs() == 0 && particle.getMaxOccurs() == 1;
    }
      

}
