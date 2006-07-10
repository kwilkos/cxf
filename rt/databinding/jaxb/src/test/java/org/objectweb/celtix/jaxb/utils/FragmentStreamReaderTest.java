package org.objectweb.celtix.jaxb.utils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class FragmentStreamReaderTest extends TestCase {

    public void testReader() throws Exception {
        XMLInputFactory ifactory = StaxUtils.getXMLInputFactory();
        XMLStreamReader reader = 
            ifactory.createXMLStreamReader(getClass().getResourceAsStream("../resources/amazon.xml"));
        
        DepthXMLStreamReader dr = new DepthXMLStreamReader(reader);
        
        StaxUtils.toNextElement(dr);
        assertEquals("ItemLookup", dr.getLocalName());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.getEventType());
        
        FragmentStreamReader fsr = new FragmentStreamReader(dr);
        assertTrue(fsr.hasNext());
        
        assertEquals(XMLStreamReader.START_DOCUMENT, fsr.next());
        assertEquals(XMLStreamReader.START_DOCUMENT, fsr.getEventType());
        
        fsr.next();

        assertEquals("ItemLookup", fsr.getLocalName());
        assertEquals("ItemLookup", dr.getLocalName());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.getEventType());
        
        fsr.close();
    }
}
