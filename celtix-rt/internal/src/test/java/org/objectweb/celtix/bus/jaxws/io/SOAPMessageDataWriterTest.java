package org.objectweb.celtix.bus.jaxws.io;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bus.jaxws.DynamicDataBindingCallback;

public class SOAPMessageDataWriterTest<T> extends TestCase {
    
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
        is2 =  getClass().getResourceAsStream("GreetMeDocLiteralReq.xml");
        is3 =  getClass().getResourceAsStream("GreetMeDocLiteralReq.xml");
        SOAPMessage msg = MessageFactory.newInstance().createMessage(null, is);
        domSource = new DOMSource(msg.getSOAPPart());
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
            new TestDynamicDataBindingCallback(DOMSource.class, Mode.MESSAGE);        
        TestSOAPMessageDataWriter<SOAPMessage> soapMessageDataWriter = 
            new TestSOAPMessageDataWriter<SOAPMessage>(callback);
        obj = domSource;
        soapMessageDataWriter.write(obj, soapMsg);
        SOAPMessage soapMessage = soapMessageDataWriter.getTestSOAPMessage();
        assertTrue("TextContent should be TestSOAPInputMessage", 
                   "TestSOAPInputMessage".equals(soapMessage.getSOAPBody().getFirstChild().getTextContent()));
        
    }
    
    public void testSAXSourceWrite() throws Exception {
        TestDynamicDataBindingCallback callback =  
            new TestDynamicDataBindingCallback(SAXSource.class, Mode.MESSAGE);        
        TestSOAPMessageDataWriter<SOAPMessage> soapMessageDataWriter = 
            new TestSOAPMessageDataWriter<SOAPMessage>(callback);
        obj = saxSource;
        soapMessageDataWriter.write(obj, soapMsg);
        SOAPMessage soapMessage = soapMessageDataWriter.getTestSOAPMessage();
        assertTrue("TextContent should be TestSOAPInputMessage", 
                   "TestSOAPInputMessage".equals(soapMessage.getSOAPBody().getFirstChild().getTextContent()));
      
    }
    
    public void testStreamSourceWrite() throws Exception {
        TestDynamicDataBindingCallback callback =  
            new TestDynamicDataBindingCallback(StreamSource.class, Mode.MESSAGE);        
        TestSOAPMessageDataWriter<SOAPMessage> soapMessageDataWriter = 
            new TestSOAPMessageDataWriter<SOAPMessage>(callback);
        obj = streamSource;
        soapMessageDataWriter.write(obj, soapMsg);
        SOAPMessage soapMessage = soapMessageDataWriter.getTestSOAPMessage();
        assertTrue("TextContent should be TestSOAPInputMessage", 
                   "TestSOAPInputMessage".equals(soapMessage.getSOAPBody().getFirstChild().getTextContent()));
      
    }
    
    
    private class TestSOAPMessageDataWriter<X> extends SOAPMessageDataWriter<X> {

        public TestSOAPMessageDataWriter(DynamicDataBindingCallback cb) {
            super(cb);
        }
        
        public SOAPMessage getTestSOAPMessage() {
            return dest;
        }
        
    }

}
