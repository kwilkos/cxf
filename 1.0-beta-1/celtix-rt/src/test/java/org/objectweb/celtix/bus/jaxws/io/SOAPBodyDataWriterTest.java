package org.objectweb.celtix.bus.jaxws.io;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bus.jaxws.DynamicDataBindingCallback;
import org.objectweb.hello_world_soap_http.types.GreetMe;

public class SOAPBodyDataWriterTest<T> extends TestCase {
    
    
    private SOAPMessage soapMsg;
    private InputStream is;
    private InputStream is2;
    private InputStream is3;
    private Object obj;
    private DOMSource domSource;
    private SAXSource saxSource;
    private StreamSource streamSource;
    
    public void setUp() throws IOException, SOAPException {
        is =  getClass().getResourceAsStream("GreetMeDocLiteralReq.xml");
        is2 =  getClass().getResourceAsStream("GreetMeDocLiteralSOAPBodyReq.xml");
        is3 =  getClass().getResourceAsStream("GreetMeDocLiteralSOAPBodyReq.xml");
        SOAPMessage msg = MessageFactory.newInstance().createMessage(null, is);
        domSource = new DOMSource(msg.getSOAPBody().extractContentAsDocument());
        saxSource = new SAXSource(new InputSource(is2));
        streamSource = new StreamSource(is3);
        soapMsg = MessageFactory.newInstance().createMessage();
        assertNotNull(soapMsg);
    }
    
    public void tearDown() throws IOException {
        //is.close();
    }
    
    public void testDOMSourceWrite() throws Exception {
        TestDynamicDataBindingCallback callback =  
            new TestDynamicDataBindingCallback(DOMSource.class, Mode.PAYLOAD);        
        TestSOAPBodyDataWriter<SOAPBody> soapBodyDataWriter = new TestSOAPBodyDataWriter<SOAPBody>(callback);
        obj = domSource;
        soapBodyDataWriter.write(obj, soapMsg.getSOAPBody());
        SOAPBody soapBody = soapBodyDataWriter.getTestSOAPBody();
        assertTrue("TextContent should be TestSOAPInputMessage", 
                   "TestSOAPInputMessage".equals(soapBody.getFirstChild().getTextContent()));
        
    }
    
    public void testSAXSourceWrite() throws Exception {
        TestDynamicDataBindingCallback callback =  
            new TestDynamicDataBindingCallback(SAXSource.class, Mode.PAYLOAD);        
        TestSOAPBodyDataWriter<SOAPBody> soapBodyDataWriter = new TestSOAPBodyDataWriter<SOAPBody>(callback);
        obj = saxSource;
        soapBodyDataWriter.write(obj, soapMsg.getSOAPBody());
        SOAPBody soapBody = soapBodyDataWriter.getTestSOAPBody();
        assertTrue("TextContent should be TestSOAPInputMessage", 
                   "TestSOAPInputMessage".equals(soapBody.getFirstChild().getTextContent()));
      
    }
    
    public void testStreamSourceWrite() throws Exception {
        TestDynamicDataBindingCallback callback =  
            new TestDynamicDataBindingCallback(StreamSource.class, Mode.PAYLOAD);        
        TestSOAPBodyDataWriter<SOAPBody> soapBodyDataWriter = new TestSOAPBodyDataWriter<SOAPBody>(callback);
        obj = streamSource;
        soapBodyDataWriter.write(obj, soapMsg.getSOAPBody());
        SOAPBody soapBody = soapBodyDataWriter.getTestSOAPBody();
        assertTrue("TextContent should be TestSOAPInputMessage", 
                   "TestSOAPInputMessage".equals(soapBody.getFirstChild().getTextContent()));
      
    }
    
    public void testJAXBObjectWrite() throws Exception {
        
        JAXBContext jc = JAXBContext.newInstance("org.objectweb.hello_world_soap_http.types");  
        TestDynamicDataBindingCallback callback =  
            new TestDynamicDataBindingCallback(jc, Mode.PAYLOAD);        
        TestSOAPBodyDataWriter<SOAPBody> soapBodyDataWriter = new TestSOAPBodyDataWriter<SOAPBody>(callback);
        GreetMe greetMe = new GreetMe();
        greetMe.setRequestType("DIPLO");
        soapBodyDataWriter.write(greetMe, soapMsg.getSOAPBody());
        SOAPBody soapBody = soapBodyDataWriter.getTestSOAPBody();
        assertTrue("TextContent should be DIPLO", 
                   "DIPLO".equals(soapBody.getFirstChild().getTextContent()));
      
    }
    
    private class TestSOAPBodyDataWriter<X> extends SOAPBodyDataWriter<X> {

        public TestSOAPBodyDataWriter(DynamicDataBindingCallback cb) {
            super(cb);
        }
        
        public SOAPBody getTestSOAPBody() {
            return dest;
        }
        
    }

}
