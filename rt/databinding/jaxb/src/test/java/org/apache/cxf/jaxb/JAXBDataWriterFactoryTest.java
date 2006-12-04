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
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Node;

import junit.framework.TestCase;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.jaxb.io.EventDataWriter;
import org.apache.cxf.jaxb.io.NodeDataWriter;
import org.apache.cxf.jaxb.io.SOAPBodyDataWriter;
import org.apache.cxf.jaxb.io.XMLStreamDataWriter;
import org.apache.cxf.message.Message;

public class JAXBDataWriterFactoryTest extends TestCase {
    JAXBDataWriterFactory factory;

    public void setUp() {
        factory = new JAXBDataWriterFactory();
    }

    public void testSupportedFormats() {
        List<Class<?>> cls = Arrays.asList(factory.getSupportedFormats());
        assertNotNull(cls);
        assertEquals(5, cls.size());
        assertTrue(cls.contains(XMLStreamWriter.class));
        assertTrue(cls.contains(XMLEventWriter.class));
        assertTrue(cls.contains(Message.class));
        assertTrue(cls.contains(Node.class));
        assertTrue(cls.contains(SOAPBody.class));
    }

    public void testCreateWriter() {
        DataWriter writer = factory.createWriter(XMLStreamWriter.class);
        assertTrue(writer instanceof XMLStreamDataWriter);
        
        writer = factory.createWriter(XMLEventWriter.class);
        assertTrue(writer instanceof EventDataWriter);
        
        writer = factory.createWriter(Node.class);
        assertTrue(writer instanceof NodeDataWriter);

        writer = factory.createWriter(SOAPBody.class);
        assertTrue(writer instanceof SOAPBodyDataWriter);

        writer = factory.createWriter(null);
        assertNull(writer);
    }
    
}

