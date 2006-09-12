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

package org.apache.cxf.jaxb;

import java.util.Arrays;
import java.util.List;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import junit.framework.TestCase;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.jaxb.io.EventDataReader;
import org.apache.cxf.jaxb.io.NodeDataReader;
import org.apache.cxf.jaxb.io.SOAPBodyDataReader;
import org.apache.cxf.jaxb.io.SOAPMessageDataReader;
import org.apache.cxf.jaxb.io.XMLStreamDataReader;
import org.apache.cxf.message.Message;

public class JAXBDataReaderFactoryTest extends TestCase {
    JAXBDataReaderFactory factory;

    public void setUp() {
        factory = new JAXBDataReaderFactory();
    }

    public void testSupportedFormats() {
        List<Class<?>> cls = Arrays.asList(factory.getSupportedFormats());
        assertNotNull(cls);
        assertEquals(6, cls.size());
        assertTrue(cls.contains(XMLStreamReader.class));
        assertTrue(cls.contains(XMLEventReader.class));
        assertTrue(cls.contains(Message.class));
        assertTrue(cls.contains(Node.class));
        assertTrue(cls.contains(SOAPBody.class));
        assertTrue(cls.contains(SOAPMessage.class));
    }

    public void testCreateReader() {
        DataReader reader = factory.createReader(XMLStreamReader.class);
        assertTrue(reader instanceof XMLStreamDataReader);
        
        reader = factory.createReader(XMLEventReader.class);
        assertTrue(reader instanceof EventDataReader);

        reader = factory.createReader(Node.class);
        assertTrue(reader instanceof NodeDataReader);

        reader = factory.createReader(SOAPBody.class);
        assertTrue(reader instanceof SOAPBodyDataReader);

        reader = factory.createReader(SOAPMessage.class);
        assertTrue(reader instanceof SOAPMessageDataReader);

        reader = factory.createReader(null);
        assertNull(reader);
    }
    
}

