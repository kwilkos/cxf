package org.objectweb.celtix.bindings.soap2.utils;

import java.io.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import junit.framework.TestCase;
import static org.objectweb.celtix.datamodel.soap.SOAPConstants.SOAP_BODY;
import static org.objectweb.celtix.datamodel.soap.SOAPConstants.SOAP_ENV;

public class StaxStreamFilterTest extends TestCase {

    public void testFilter() throws Exception {
        StaxStreamFilter filter = new StaxStreamFilter(new QName[]{SOAP_ENV, SOAP_BODY});
        String soapMessage = "/org/objectweb/celtix/bindings/soap2/binding/resources/sayHiRpcLiteralReq.xml";
        XMLStreamReader reader = StaxUtils.createXMLStreamReader(getTestStream(soapMessage));
        reader = StaxUtils.createFilteredReader(reader, filter);
        
        DepthXMLStreamReader dr = new DepthXMLStreamReader(reader);

        StaxUtils.toNextElement(dr);
        QName sayHi = new QName("http://objectweb.org/hello_world_rpclit", "sayHi");
        
        assertEquals(sayHi, dr.getName());
    }

    private InputStream getTestStream(String file) {
        return getClass().getResourceAsStream(file);
    }
}
