package org.apache.cxf.jaxb;

import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import junit.framework.TestCase;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.jaxb.io.EventDataReader;
import org.apache.cxf.jaxb.io.NodeDataReader;
import org.apache.cxf.jaxb.io.XMLStreamDataReader;

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

