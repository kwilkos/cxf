package org.objectweb.celtix.staxutils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class DepthXMLStreamReaderTest extends TestCase {
    public void testReader() throws Exception {
        XMLInputFactory ifactory = StaxUtils.getXMLInputFactory();
        XMLStreamReader reader = 
            ifactory.createXMLStreamReader(getClass().getResourceAsStream("./resources/amazon.xml"));
        
        DepthXMLStreamReader dr = new DepthXMLStreamReader(reader);
        
        StaxUtils.toNextElement(dr);
        assertEquals("ItemLookup", dr.getLocalName());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.getEventType());

        assertEquals(1, dr.getDepth());

        assertEquals(0, dr.getAttributeCount());


        dr.next();

        assertEquals(1, dr.getDepth());
        assertTrue(dr.isWhiteSpace());

        dr.nextTag();

        assertEquals(2, dr.getDepth());
        assertEquals("SubscriptionId", dr.getLocalName());

        dr.next();
        assertEquals("1E5AY4ZG53H4AMC8QH82", dr.getText());
        
        dr.close();
    }
}
