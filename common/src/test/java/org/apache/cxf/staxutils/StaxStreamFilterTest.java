package org.apache.cxf.staxutils;

import java.io.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class StaxStreamFilterTest extends TestCase {
    public static final QName  SOAP_ENV = 
        new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
    public static final QName  SOAP_BODY = 
        new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body");

    public void testFilter() throws Exception {
        StaxStreamFilter filter = new StaxStreamFilter(new QName[]{SOAP_ENV, SOAP_BODY});
        String soapMessage = "./resources/sayHiRpcLiteralReq.xml";
        XMLStreamReader reader = StaxUtils.createXMLStreamReader(getTestStream(soapMessage));
        reader = StaxUtils.createFilteredReader(reader, filter);
        
        DepthXMLStreamReader dr = new DepthXMLStreamReader(reader);

        StaxUtils.toNextElement(dr);
        QName sayHi = new QName("http://apache.org/hello_world_rpclit", "sayHi");
        
        assertEquals(sayHi, dr.getName());
    }

    public void testFilterRPC() throws Exception {
        StaxStreamFilter filter = new StaxStreamFilter(new QName[]{SOAP_ENV, SOAP_BODY});
        String soapMessage = "./resources/greetMeRpcLitReq.xml";
        XMLStreamReader reader = StaxUtils.createXMLStreamReader(getTestStream(soapMessage));
        reader = StaxUtils.createFilteredReader(reader, filter);
        
        DepthXMLStreamReader dr = new DepthXMLStreamReader(reader);

        StaxUtils.toNextElement(dr);
        assertEquals(new QName("http://apache.org/hello_world_rpclit", "sendReceiveData"), dr.getName());

        StaxUtils.nextEvent(dr);
        StaxUtils.toNextElement(dr);
        assertEquals(new QName("", "in"), dr.getName());

        StaxUtils.nextEvent(dr);
        StaxUtils.toNextElement(dr);
        assertEquals(new QName("http://apache.org/hello_world_rpclit/types", "elem1"), dr.getName());

        StaxUtils.nextEvent(dr);
        StaxUtils.toNextText(dr);
        assertEquals("this is element 1", dr.getText());
        
        StaxUtils.toNextElement(dr);
        assertEquals(new QName("http://apache.org/hello_world_rpclit/types", "elem1"), dr.getName());
        assertEquals(XMLStreamConstants.END_ELEMENT, dr.getEventType());

        StaxUtils.nextEvent(dr);
        StaxUtils.toNextElement(dr);
        
        assertEquals(new QName("http://apache.org/hello_world_rpclit/types", "elem2"), dr.getName());
    } 

    private InputStream getTestStream(String file) {
        return getClass().getResourceAsStream(file);
    }
}
