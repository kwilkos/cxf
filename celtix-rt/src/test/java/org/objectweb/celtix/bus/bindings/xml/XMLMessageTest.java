package org.objectweb.celtix.bus.bindings.xml;

import java.io.*;
import junit.framework.TestCase;

public class XMLMessageTest extends TestCase {

    protected XMLMessageFactory msgFactory = XMLMessageFactory.newInstance();

    protected void setUp() throws Exception {
    }

    public void testXMLMessageFactory() {
        assertNotNull(msgFactory);
    }

    public void testDefaultXMLMessage() throws Exception {
        XMLMessage message = msgFactory.createMessage();
        assertNotNull(message);
        assertNotNull(message.getRoot());
        String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);
        assertTrue(expected.equalsIgnoreCase(out.toString()));
    }

    public void testStreamXMLMessage() {
        InputStream is =  getClass().getResourceAsStream("resources/SayHiWrappedResp.xml");
        XMLMessage message = msgFactory.createMessage(is);
        assertNotNull(message);
        assertNotNull(message.getRoot());
    }
}
