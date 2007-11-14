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

package org.apache.cxf.common.xmlschema;

import java.io.Reader;
import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import org.apache.ws.commons.schema.ValidationEventHandler;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.extensions.ExtensionRegistry;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.apache.ws.commons.schema.utils.TargetNamespaceValidator;

/**
 * Wrapper class for XmlSchemaCollection that deals with various quirks and bugs.
 * One bug is WSCOMMONS-272.
 */
public class SchemaCollection {
    
    private XmlSchemaCollection schemaCollection;
    
    public SchemaCollection() {
        this(new XmlSchemaCollection());
    }
    
    public SchemaCollection(XmlSchemaCollection col) {
        schemaCollection = col;
        col.getExtReg().setDefaultExtensionDeserializer(new FixedExtensionDeserializer());
    }

    public boolean equals(Object obj) {
        return schemaCollection.equals(obj);
    }

    public XmlSchemaElement getElementByQName(QName qname) {
        return schemaCollection.getElementByQName(qname);
    }

    public ExtensionRegistry getExtReg() {
        return schemaCollection.getExtReg();
    }

    public NamespacePrefixList getNamespaceContext() {
        return schemaCollection.getNamespaceContext();
    }

    public XmlSchemaType getTypeByQName(QName schemaTypeName) {
        return schemaCollection.getTypeByQName(schemaTypeName);
    }

    public XmlSchema[] getXmlSchema(String systemId) {
        return schemaCollection.getXmlSchema(systemId);
    }

    public XmlSchema[] getXmlSchemas() {
        return schemaCollection.getXmlSchemas();
    }

    public int hashCode() {
        return schemaCollection.hashCode();
    }

    public void init() {
        schemaCollection.init();
    }

    public XmlSchema read(Document doc, String uri, ValidationEventHandler veh,
                          TargetNamespaceValidator validator) {
        return schemaCollection.read(doc, uri, veh, validator);
    }

    public XmlSchema read(Document doc, String uri, ValidationEventHandler veh) {
        return schemaCollection.read(doc, uri, veh);
    }

    public XmlSchema read(Document doc, ValidationEventHandler veh) {
        return schemaCollection.read(doc, veh);
    }

    public XmlSchema read(Element elem, String uri) {
        return schemaCollection.read(elem, uri);
    }

    public XmlSchema read(Element elem) {
        return schemaCollection.read(elem);
    }

    public XmlSchema read(InputSource inputSource, ValidationEventHandler veh) {
        return schemaCollection.read(inputSource, veh);
    }

    public XmlSchema read(Reader r, ValidationEventHandler veh) {
        return schemaCollection.read(r, veh);
    }

    public XmlSchema read(Source source, ValidationEventHandler veh) {
        return schemaCollection.read(source, veh);
    }

    public void setBaseUri(String baseUri) {
        schemaCollection.setBaseUri(baseUri);
    }

    public void setExtReg(ExtensionRegistry extReg) {
        schemaCollection.setExtReg(extReg);
    }

    public void setNamespaceContext(NamespacePrefixList namespaceContext) {
        schemaCollection.setNamespaceContext(namespaceContext);
    }

    public void setSchemaResolver(URIResolver schemaResolver) {
        schemaCollection.setSchemaResolver(schemaResolver);
    }

    /**
     * This is a really ugly trick to get around a bug or oversight in XmlSchema, which is that
     * there is no way to programmatically construct an XmlSchema instance that ends up cataloged
     * in a collection. If there is a fix to WSCOMMONS-272, this can go away.
     * @param namespaceURI TNS for new schema.
     * @return new schema
     */

    public XmlSchema newXmlSchemaInCollection(String namespaceURI) {
        StringBuffer tinyXmlSchemaDocument = new StringBuffer();
        tinyXmlSchemaDocument.append("<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' ");
        tinyXmlSchemaDocument.append("targetNamespace='" + namespaceURI + "'/>");
        StringReader reader = new StringReader(tinyXmlSchemaDocument.toString());
        return schemaCollection.read(reader, new ValidationEventHandler() { });
    }
}
