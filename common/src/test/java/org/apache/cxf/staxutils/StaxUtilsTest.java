package org.apache.cxf.staxutils;

import java.io.*;

import javax.xml.stream.XMLStreamReader;
import junit.framework.TestCase;

public class StaxUtilsTest extends TestCase {

    public void testFactoryCreation() {
        XMLStreamReader reader = StaxUtils.createXMLStreamReader(getTestStream("./resources/amazon.xml"));
        assertTrue(reader != null);
    }

    private InputStream getTestStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }

    public void testToNextElement() {
        String soapMessage = "./resources/sayHiRpcLiteralReq.xml";
        XMLStreamReader r = StaxUtils.createXMLStreamReader(getTestStream(soapMessage));
        DepthXMLStreamReader reader = new DepthXMLStreamReader(r);
        assertTrue(StaxUtils.toNextElement(reader));
        assertEquals("Envelope", reader.getLocalName());

        StaxUtils.nextEvent(reader);

        assertTrue(StaxUtils.toNextElement(reader));
        assertEquals("Body", reader.getLocalName());
    }
}
