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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import junit.framework.TestCase;

import org.apache.cxf.helpers.XMLUtils;

public class SOAPMessageDataReaderTest extends TestCase {

    SOAPMessageDataReader reader;
    SOAPMessage message;    
    String expected = "<ns4:requestType>TestSOAPInputMessage</ns4:requestType>";
    
    public void setUp() throws Exception {
        reader = new SOAPMessageDataReader(null);
        assertNotNull(reader);
        MessageFactory msgFactory = MessageFactory.newInstance();
        InputStream is = getTestStream("/messages/SOAP_GreetMeDocLiteralReq.xml");
        assertNotNull(is);
        message = msgFactory.createMessage(new MimeHeaders(), 
                                           is);
        assertNotNull(message);
    }

    public void tearDown() throws IOException {

    }
    
    public void testReadSource() throws Exception {       
        Object source = reader.read(null, message, Source.class);        
        
        assertNotNull(source);
        assertTrue(source instanceof Source);        
        assertTrue(XMLUtils.toString((Source)source).contains(expected));
    }
    
    public void testReadStreamSource() throws Exception {
        Object source = reader.read(null, message, StreamSource.class);
        assertNotNull(source);
        assertTrue(source instanceof StreamSource);        
        assertTrue(XMLUtils.toString((StreamSource)source).contains(expected));        
    }
    
    public void testReadSOAPMessage() throws Exception {
        Object source = reader.read(null, message, SOAPMessage.class);
        assertNotNull(source);
        assertTrue(source instanceof SOAPMessage);
        SOAPMessage m = (SOAPMessage) source;
        Document doc = m.getSOAPBody().extractContentAsDocument();
        assertEquals("TestSOAPInputMessage", doc.getFirstChild().getTextContent());
    }
    
    private InputStream getTestStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }
}
