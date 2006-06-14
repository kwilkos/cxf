package org.objectweb.celtix.bus.bindings.soap;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.TestCase;

import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.header_test.types.TestHeader1;
import org.objectweb.header_test.types.TestHeader2Response;

public class SoapMessageContextImplTest extends TestCase {
    private MessageFactory factory;
    
    public SoapMessageContextImplTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SoapMessageContextImplTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        factory = MessageFactory.newInstance();
    }
    
    public void testGetHeaders() throws Exception {
        SOAPMessageContext smc = new SOAPMessageContextImpl(new GenericMessageContext());
        assertNotNull(smc);

        JAXBContext jaxbContext = JAXBContext.newInstance(TestHeader1.class.getPackage().getName());
        //Test 1 No Headers in SOAP Message
        setSOAPMessage(smc, "resources/TestIntDocLitTypeTestReq.xml");
        Object[] obj1 = smc.getHeaders(null, jaxbContext, true);
        
        assertEquals(0, obj1.length);
        
        //Test 2 Headers in SOAP Message
        QName headerName2 =
            new QName("http://objectweb.org/header_test/types", "testHeader1");
        setSOAPMessage(smc, "resources/TestHeader1Req.xml");
        Object[] obj2 = smc.getHeaders(headerName2, jaxbContext, true);
        
        assertEquals(1, obj2.length);
        assertTrue(TestHeader1.class.isAssignableFrom(obj2[0].getClass()));
        
        //Test 2 Headers in SOAP Message
        QName headerName3 =
            new QName("http://objectweb.org/header_test/types", "testHeader2Response");
        setSOAPMessage(smc, "resources/TestHeader2.xml");
        Object[] obj3 = smc.getHeaders(headerName3, jaxbContext, true);
        
        assertEquals(2, obj3.length);
        assertTrue(TestHeader2Response.class.isAssignableFrom(obj3[0].getClass()));
        assertTrue(TestHeader2Response.class.isAssignableFrom(obj3[1].getClass()));
        
        TestHeader2Response val = (TestHeader2Response)obj3[0];
        assertEquals("Header1", val.getResponseType());
        
        val = (TestHeader2Response)obj3[1];
        assertEquals("Header2", val.getResponseType());
        
    }
    
    private void setSOAPMessage(SOAPMessageContext context, String resource) throws Exception {
        InputStream is =  getClass().getResourceAsStream(resource);        
        SOAPMessage msg = factory.createMessage(null, is);
        context.setMessage(msg);        
    }    
}
