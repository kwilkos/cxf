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

package org.apache.cxf.jaxb.io;

import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

import org.apache.cxf.helpers.XMLUtils;

public class SOAPBodyDataReaderTest extends TestCase {
    
    SOAPBodyDataReader reader;
    SOAPBody body;    
    String expected = "<ns4:requestType>TestSOAPInputMessage</ns4:requestType>";
    
    public void setUp() throws Exception {
        reader = new SOAPBodyDataReader(null);
        assertNotNull(reader);
        MessageFactory msgFactory = MessageFactory.newInstance();
        InputStream is = getTestStream("/messages/SOAP_GreetMeDocLiteralReq.xml");
        assertNotNull(is);
        SOAPMessage message = msgFactory.createMessage(new MimeHeaders(), 
                                           is);
        assertNotNull(message);
        body = message.getSOAPBody();
        assertNotNull(body);
    }
    
    public void testReadSource() throws Exception {       
        Object source = reader.read(null, body, Source.class);                
        assertNotNull(source);
        assertTrue(source instanceof Source);         
        assertSame(XMLUtils.parse(XMLUtils.toString((Source)source)));
    }
    
    public void testReadDomSource() throws Exception {
        Object source = reader.read(null, body, DOMSource.class);
        assertNotNull(source);
        assertTrue(source instanceof DOMSource);
        Node node = ((DOMSource)source).getNode();
        assertTrue(node instanceof Document);
        assertSame((Document)node);
    }
    
    private void assertSame(Document doc) {
        Element element = doc.getDocumentElement();
        assertEquals("ns4:greetMe", element.getNodeName());
        NodeList list = element.getChildNodes();
        assertEquals(1, list.getLength());
        Node node = list.item(0);
        assertEquals("ns4:requestType", node.getNodeName());
        assertEquals("TestSOAPInputMessage", node.getTextContent());
    }

    private InputStream getTestStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }
}
