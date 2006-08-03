package org.objectweb.celtix.jaxb;

import java.io.*;
import java.util.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import junit.framework.TestCase;
import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.jaxb.io.EventDataReader;
import org.objectweb.celtix.jaxb.io.NodeDataReader;
import org.objectweb.celtix.jaxb.io.XMLStreamDataReader;

public class JAXBDataReaderFactoryTest extends TestCase {
    JAXBDataReaderFactory factory;

    public void setUp() {
        factory = new JAXBDataReaderFactory();
    }

    public void testSupportedFormats() {
        List<Class<?>> cls = Arrays.asList(factory.getSupportedFormats());
        assertNotNull(cls);
        assertEquals(3, cls.size());
        assertTrue(cls.contains(XMLStreamReader.class));
        assertTrue(cls.contains(XMLEventReader.class));
        assertTrue(cls.contains(Node.class));
    }

    public void testCreateReader() {
        DataReader reader = factory.createReader(XMLStreamReader.class);
        assertTrue(reader instanceof XMLStreamDataReader);
        
        reader = factory.createReader(XMLEventReader.class);
        assertTrue(reader instanceof EventDataReader);

        reader = factory.createReader(Node.class);
        assertTrue(reader instanceof NodeDataReader);

        reader = factory.createReader(null);
        assertNull(reader);
    }
}

