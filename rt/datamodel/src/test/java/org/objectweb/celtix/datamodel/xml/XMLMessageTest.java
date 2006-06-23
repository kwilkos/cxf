package org.objectweb.celtix.datamodel.xml;

import java.io.*;
import javax.xml.namespace.QName;
import org.w3c.dom.*;
import junit.framework.TestCase;

import org.objectweb.celtix.helpers.XMLUtils;

public class XMLMessageTest extends TestCase {

    protected XMLMessageFactory msgFactory = XMLMessageFactory.newInstance();
    private XMLUtils xmlUtils = new XMLUtils();

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

    public void testXMLFaultMessage() throws Exception {
        XMLMessage message = msgFactory.createMessage();
        XMLFault fault = message.addFault();
        assertNotNull(fault);
        fault.addFaultString("exception raised");
        assertEquals("exception raised", fault.getFaultString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);
        String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        expected += "<XMLFault xmlns=\"http://celtix.objectweb.org/bindings/xmlformat\">";
        expected += "<faultstring>exception raised</faultstring></XMLFault>";
        assertTrue(expected.equalsIgnoreCase(out.toString()));
    }

    public void testXMLFaultDetail() throws Exception {
        XMLMessage message = msgFactory.createMessage();
        XMLFault fault = message.addFault();
        assertNotNull(fault);

        fault.addFaultDetail();
        String namespace = "http://objectweb.org/hello_world_soap_http/types";
        Node detailNode = xmlUtils.createElementNS(fault.getFaultRoot(), new QName(namespace, "faultDetail"));
        Node minor = xmlUtils.createElementNS(detailNode, new QName(namespace, "minor"));
        Node major = xmlUtils.createElementNS(detailNode, new QName(namespace, "major"));
        
        Text minorValue = xmlUtils.createTextNode(minor, "1");
        Text majorValue = xmlUtils.createTextNode(major, "2");
        minor.appendChild(minorValue);
        major.appendChild(majorValue);
        detailNode.appendChild(minor);
        detailNode.appendChild(major);

        fault.appendFaultDetail(detailNode);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);

        InputStream is =  getClass().getResourceAsStream("resources/xmlfaultdetail.xml");
        Document expectDOM = xmlUtils.parse(is);

        assertEquals(xmlUtils.toString(expectDOM), message.toString());
    }

    public void testCreateFaultXMLMessage() throws Exception {
        InputStream is =  getClass().getResourceAsStream("resources/xmlfault.xml");
        XMLMessage message = msgFactory.createMessage(is);
        assertNotNull(message);

        XMLFault xmlFault = message.getFault();
        assertNotNull(xmlFault);
        
        assertEquals("org.objectweb.hello_world_xml_http.wrapped.PingMeFault: PingMeFault raised by server",
                     xmlFault.getFaultString());
        assertNotNull(xmlFault.getFaultDetail());
    }
}
