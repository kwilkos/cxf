package org.objectweb.celtix.jaxb.io;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;

public class SOAPBodyDataReaderTest<T> extends TestCase {
    
    private SOAPMessage soapMsg;
    private InputStream is;
    
    public void setUp() throws IOException, SOAPException {
        is =  getClass().getResourceAsStream("GreetMeDocLiteralReq.xml");
        soapMsg = MessageFactory.newInstance().createMessage(null, is);
        assertNotNull(soapMsg);
    }
    
    public void tearDown() throws IOException {
        is.close();
    }
    
    public void testDOMSourceRead() throws Exception {
        TestDynamicDataBindingCallback callback =  
            new TestDynamicDataBindingCallback(DOMSource.class, Mode.PAYLOAD);        
        SOAPBodyDataReader<SOAPBody> soapBodyDataReader = new SOAPBodyDataReader<SOAPBody>(callback);
        DOMSource obj = (DOMSource)soapBodyDataReader.read(0, soapMsg.getSOAPBody());
        assertNotNull(obj);
        assertEquals("Message should contain TestSOAPInputMessage",
                     obj.getNode().getFirstChild().getTextContent(), "TestSOAPInputMessage");  
        
    }
    
    public void testSAXSourceRead() throws Exception {
        TestDynamicDataBindingCallback callback =  
            new TestDynamicDataBindingCallback(SAXSource.class, Mode.PAYLOAD);        
        SOAPBodyDataReader<SOAPBody> soapBodyDataReader = new SOAPBodyDataReader<SOAPBody>(callback);
        SAXSource obj = (SAXSource)soapBodyDataReader.read(0, soapMsg.getSOAPBody());
        assertNotNull(obj);
        checkSource("TestSOAPInputMessage", obj);        
    }
    
    public void testStreamSourceRead() throws Exception {
        TestDynamicDataBindingCallback callback =  
            new TestDynamicDataBindingCallback(StreamSource.class, Mode.PAYLOAD);        
        SOAPBodyDataReader<SOAPBody> soapBodyDataReader = new SOAPBodyDataReader<SOAPBody>(callback);
        StreamSource obj = (StreamSource)soapBodyDataReader.read(0, soapMsg.getSOAPBody());
        assertNotNull(obj);
        checkSource("TestSOAPInputMessage", obj);        
    }
    
    public void testJAXBObjectRead() throws Exception {
        
        JAXBContext jc = JAXBContext.newInstance("org.objectweb.hello_world_soap_http.types"); 
        TestDynamicDataBindingCallback callback =  
            new TestDynamicDataBindingCallback(jc, Mode.PAYLOAD);        
        SOAPBodyDataReader<SOAPBody> soapBodyDataReader = new SOAPBodyDataReader<SOAPBody>(callback);
        Object obj = soapBodyDataReader.read(0, soapMsg.getSOAPBody());
        assertNotNull(obj);
        assertTrue("Expect class of type org.objectweb.hello_world_soap_http.types.GreetMe", 
                   obj.getClass().getName().equals("org.objectweb.hello_world_soap_http.types.GreetMe"));    
    }
    
    private void checkSource(String expected, Source source) {
        
        InputStream inputStream = null;
        
        if (source.getClass().isAssignableFrom(SAXSource.class)) {    
            InputSource inputSource =  ((SAXSource)source).getInputSource();
            inputStream = inputSource.getByteStream();
        } else if (source.getClass().isAssignableFrom(StreamSource.class)) {    
            inputStream =  ((StreamSource)source).getInputStream();
        } 
        
        int i = 0;
        StringBuilder sb = new StringBuilder();
        try {
            while (i != -1) {
                i = inputStream.read();
                sb.append((char)i);           
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String received = sb.toString();
        assertTrue("Expected: " + expected, received.contains(expected));
        
    }

}
