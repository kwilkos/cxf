package org.objectweb.celtix.jaxb;

import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Node;

import junit.framework.TestCase;

import org.objectweb.celtix.databinding.DataWriter;
import org.objectweb.celtix.jaxb.io.EventDataWriter;
import org.objectweb.celtix.jaxb.io.NodeDataWriter;
import org.objectweb.celtix.jaxb.io.XMLStreamDataWriter;

public class JAXBDataWriterFactoryTest extends TestCase {
    JAXBDataWriterFactory factory;

    public void setUp() {
        factory = JAXBDataWriterFactory.getInstance();
    }

    public void testSupportedFormats() {
        List<Class<?>> cls = Arrays.asList(factory.getSupportedFormats());
        assertNotNull(cls);
        assertEquals(3, cls.size());
        assertTrue(cls.contains(XMLStreamWriter.class));
        assertTrue(cls.contains(XMLEventWriter.class));
        assertTrue(cls.contains(Node.class));
    }

    public void testCreateWriter() {
        DataWriter writer = factory.createWriter(XMLStreamWriter.class);
        assertTrue(writer instanceof XMLStreamDataWriter);
        
        writer = factory.createWriter(XMLEventWriter.class);
        assertTrue(writer instanceof EventDataWriter);
        
        writer = factory.createWriter(Node.class);
        assertTrue(writer instanceof NodeDataWriter);

        writer = factory.createWriter(null);
        assertNull(writer);
    }
    
    public void testSingleton() {
        assertTrue(factory == JAXBDataWriterFactory.getInstance());
    }
}

