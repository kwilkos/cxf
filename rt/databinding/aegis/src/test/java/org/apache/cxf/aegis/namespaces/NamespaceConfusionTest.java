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
package org.apache.cxf.aegis.namespaces;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.aegis.namespaces.data.Name;
import org.apache.cxf.aegis.namespaces.impl.NameServiceImpl;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.TypeMapping;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.service.Service;

import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom.DOMXPath;

import org.junit.Before;
import org.junit.Test;

public class NamespaceConfusionTest extends AbstractAegisTest {
    
    private TypeMapping tm;
    private Service service;
        
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        Server s = createService(NameServiceImpl.class, null);
        service = s.getEndpoint().getService();

        tm = (TypeMapping)service.get(TypeMapping.class.getName());
    }
    
    private String getNamespaceForPrefix(Element rootElement, 
                                         NamespaceContext namespaceContext, 
                                         Element typeElement, 
                                         String prefix) throws JaxenException {
        DOMXPath findSchema = new DOMXPath("ancestor::xsd:schema");
        findSchema.setNamespaceContext(namespaceContext);
        Element schemaElement = (Element)findSchema.selectSingleNode(typeElement);

        NamedNodeMap attributes = schemaElement.getAttributes();
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attr = (Attr)attributes.item(x);
            if (attr.getName().startsWith("xmlns:")) {
                String attrPrefix = attr.getName().split(":")[1];
                if (attrPrefix.equals(prefix)) {
                    return attr.getValue();
                }
            }
        }

        attributes = rootElement.getAttributes();
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attr = (Attr)attributes.item(x);
            if (attr.getName().startsWith("xmlns:")) {
                String attrPrefix = attr.getName().split(":")[1];
                if (attrPrefix.equals(prefix)) {
                    return attr.getValue();
                }
            }
        }

        return null;
    }
    
    
    @Test
    public void testNameNamespace() throws WSDLException, JaxenException {
        org.w3c.dom.Document wsdlDoc = getWSDLDocument("NameServiceImpl");
        Element rootElement = wsdlDoc.getDocumentElement();
        
        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
        DOMXPath arrayOfNameFinder = 
            new DOMXPath("//xsd:complexType[@name='ArrayOfName']/xsd:sequence/xsd:element");
        arrayOfNameFinder.setNamespaceContext(namespaceContext);
        
        Element arrayOfNameElement = (Element)arrayOfNameFinder.selectSingleNode(rootElement);
        assertNotNull(arrayOfNameElement);

        String typename = arrayOfNameElement.getAttribute("type");
        String prefix = typename.split(":")[0];

        String uri = getNamespaceForPrefix(rootElement, namespaceContext, 
                                           arrayOfNameElement, prefix);
        assertNotNull(uri);
        Type nameType = tm.getTypeCreator().createType(Name.class);
        QName tmQname = nameType.getSchemaType();
        assertEquals(tmQname.getNamespaceURI(), uri);
    }
    
    
    

}
