package org.objectweb.celtix.systest.dispatch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

import org.xml.sax.InputSource;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.basic.Server;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_soap_http.SOAPService;

public class DispatchClientServerTest extends ClientServerTestBase {

    private final QName serviceName = new QName("http://objectweb.org/hello_world_soap_http",
                                                "SOAPService");
    private final QName portName = new QName("http://objectweb.org/hello_world_soap_http",
                                             "SoapPort");

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(DispatchClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }
 

    public void testSOAPMessage() throws Exception {

        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        InputStream is =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null,  is);
        assertNotNull(soapReqMsg);
        
        InputStream is1 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq1.xml");
        SOAPMessage soapReqMsg1 = MessageFactory.newInstance().createMessage(null,  is1);
        assertNotNull(soapReqMsg1);
        
        InputStream is2 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq2.xml");
        SOAPMessage soapReqMsg2 = MessageFactory.newInstance().createMessage(null,  is2);
        assertNotNull(soapReqMsg2);
        
        InputStream is3 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq3.xml");
        SOAPMessage soapReqMsg3 = MessageFactory.newInstance().createMessage(null,  is3);
        assertNotNull(soapReqMsg3);

        Dispatch<SOAPMessage> disp = service.createDispatch(portName,
                                                            SOAPMessage.class, Service.Mode.MESSAGE);
        SOAPMessage soapResMsg = disp.invoke(soapReqMsg);
        assertNotNull(soapResMsg);
        String expected = "Hello TestSOAPInputMessage";
        assertEquals("Response should be : Hello TestSOAPInputMessage",
                     expected, soapResMsg.getSOAPBody().getTextContent());
        
        disp.invokeOneWay(soapReqMsg1);
        
        Response response = disp.invokeAsync(soapReqMsg2);
        SOAPMessage soapResMsg2 = (SOAPMessage)response.get();
        assertNotNull(soapResMsg2);
        String expected2 = "Hello TestSOAPInputMessage2";
        assertEquals("Response should be : Hello TestSOAPInputMessage2",
                     expected2, soapResMsg2.getSOAPBody().getTextContent());
        
        TestSOAPMessageHandler tsmh = new TestSOAPMessageHandler();
        Future f = disp.invokeAsync(soapReqMsg3, tsmh);
        assertNotNull(f);
        while (!f.isDone()) {
            //wait
        }
        String expected3 = "Hello TestSOAPInputMessage3";
        assertEquals("Response should be : Hello TestSOAPInputMessage3",
                     expected3, tsmh.getReplyBuffer());
        
        
        
        
    }
    


    public void testDOMSourceMESSAGE() throws Exception {

        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        InputStream is =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null, is);
        DOMSource domReqMsg = new DOMSource(soapReqMsg.getSOAPPart());
        assertNotNull(domReqMsg);
        
        InputStream is1 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq1.xml");
        SOAPMessage soapReqMsg1 = MessageFactory.newInstance().createMessage(null, is1);
        DOMSource domReqMsg1 = new DOMSource(soapReqMsg1.getSOAPPart());
        assertNotNull(domReqMsg1);
        
        InputStream is2 = getClass().getResourceAsStream("resources/GreetMeDocLiteralReq2.xml");
        SOAPMessage soapReqMsg2 = MessageFactory.newInstance().createMessage(null, is2);
        DOMSource domReqMsg2 = new DOMSource(soapReqMsg2.getSOAPPart());
        assertNotNull(domReqMsg2);
        
        InputStream is3 = getClass().getResourceAsStream("resources/GreetMeDocLiteralReq3.xml");
        SOAPMessage soapReqMsg3 = MessageFactory.newInstance().createMessage(null, is3);
        DOMSource domReqMsg3 = new DOMSource(soapReqMsg3.getSOAPPart());
        assertNotNull(domReqMsg3);
    

        Dispatch<DOMSource> disp = service.createDispatch(portName,
                                                            DOMSource.class, Service.Mode.MESSAGE);
        DOMSource domResMsg = disp.invoke(domReqMsg);
        assertNotNull(domResMsg);
        String expected = "Hello TestSOAPInputMessage";
        assertEquals("Response should be : Hello TestSOAPInputMessage",
                     expected, domResMsg.getNode().getFirstChild().getTextContent());
        
        disp.invokeOneWay(domReqMsg1);
        
        Response response = disp.invokeAsync(domReqMsg2);
        DOMSource domRespMsg2 = (DOMSource)response.get();
        assertNotNull(domReqMsg2);
        String expected2 = "Hello TestSOAPInputMessage2";
        assertEquals("Response should be : Hello TestSOAPInputMessage2",
                     expected2, domRespMsg2.getNode().getFirstChild().getTextContent());
        
        
        TestDOMSourceHandler tdsh = new TestDOMSourceHandler();
        Future fd = disp.invokeAsync(domReqMsg3, tdsh);
        assertNotNull(fd);
        while (!fd.isDone()) {
            //wait
        }
        String expected3 = "Hello TestSOAPInputMessage3";
        assertEquals("Response should be : Hello TestSOAPInputMessage3",
                     expected3, tdsh.getReplyBuffer());
    }
    
  
    public void testDOMSourcePAYLOAD() throws Exception {

        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        InputStream is =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null,  is);
        DOMSource domReqMsg = new DOMSource(soapReqMsg.getSOAPBody().extractContentAsDocument());
        assertNotNull(domReqMsg);
        
        InputStream is1 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq1.xml");
        SOAPMessage soapReqMsg1 = MessageFactory.newInstance().createMessage(null,  is1);
        DOMSource domReqMsg1 = new DOMSource(soapReqMsg1.getSOAPBody().extractContentAsDocument());
        assertNotNull(domReqMsg1);
        
        InputStream is2 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq2.xml");
        SOAPMessage soapReqMsg2 = MessageFactory.newInstance().createMessage(null,  is2);
        DOMSource domReqMsg2 = new DOMSource(soapReqMsg2.getSOAPBody().extractContentAsDocument());
        assertNotNull(domReqMsg2);
        
        InputStream is3 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq3.xml");
        SOAPMessage soapReqMsg3 = MessageFactory.newInstance().createMessage(null,  is3);
        DOMSource domReqMsg3 = new DOMSource(soapReqMsg3.getSOAPBody().extractContentAsDocument());
        assertNotNull(domReqMsg3);

        Dispatch<DOMSource> disp = service.createDispatch(portName,
                                                            DOMSource.class, Service.Mode.PAYLOAD);
        
        //invoke
        DOMSource domResMsg = disp.invoke(domReqMsg);
        assertNotNull(domResMsg);
        String expected = "Hello TestSOAPInputMessage";
        assertEquals("Response should be : Hello TestSOAPInputMessage",
                     expected, domResMsg.getNode().getFirstChild().getTextContent());
        
        //invokeOneWay
        disp.invokeOneWay(domReqMsg1);
        
        //invokeAsync
        Response response = disp.invokeAsync(domReqMsg2);
        DOMSource domRespMsg2 = (DOMSource)response.get();
        assertNotNull(domRespMsg2);
        String expected2 = "Hello TestSOAPInputMessage2";
        assertEquals("Response should be : Hello TestSOAPInputMessage2",
                     expected2, domRespMsg2.getNode().getFirstChild().getTextContent());
        
        
        //invokeAsync with AsyncHandler
        TestDOMSourceHandler tdsh = new TestDOMSourceHandler();
        Future fd = disp.invokeAsync(domReqMsg3, tdsh);
        assertNotNull(fd);
        while (!fd.isDone()) {
            //wait
        }
        String expected3 = "Hello TestSOAPInputMessage3";
        assertEquals("Response should be : Hello TestSOAPInputMessage3",
                     expected3, tdsh.getReplyBuffer());

    }
    
    public void testSAXSourceMESSAGE() throws Exception {

        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        InputStream is =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        InputSource inputSource =  new InputSource(is);
        SAXSource saxSourceReq = new SAXSource(inputSource);
        assertNotNull(saxSourceReq);
        
        InputStream is1 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq1.xml");
        InputSource inputSource1 =  new InputSource(is1);
        SAXSource saxSourceReq1 = new SAXSource(inputSource1);
        assertNotNull(saxSourceReq1);
        
        InputStream is2 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq2.xml");
        InputSource inputSource2 =  new InputSource(is2);
        SAXSource saxSourceReq2 = new SAXSource(inputSource2);
        assertNotNull(saxSourceReq2);
        
        InputStream is3 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq3.xml");
        InputSource inputSource3 =  new InputSource(is3);
        SAXSource saxSourceReq3 = new SAXSource(inputSource3);
        assertNotNull(saxSourceReq3);

        Dispatch<SAXSource> disp = service.createDispatch(portName,
                                                            SAXSource.class, Service.Mode.MESSAGE);
        SAXSource saxSourceResp = disp.invoke(saxSourceReq);
        assertNotNull(saxSourceResp);
        String expected = "Hello TestSOAPInputMessage";
        checkSAXSource(expected, saxSourceResp);
        
        disp.invokeOneWay(saxSourceReq1);
        
        Response response = disp.invokeAsync(saxSourceReq2);
        SAXSource saxSourceResp2 = (SAXSource)response.get();
        assertNotNull(saxSourceResp2);
        String expected2 = "Hello TestSOAPInputMessage2";
        checkSAXSource(expected2, saxSourceResp2);
        
        
        TestSAXSourceHandler tssh = new TestSAXSourceHandler();
        Future fd = disp.invokeAsync(saxSourceReq3, tssh);
        assertNotNull(fd);
        while (!fd.isDone()) {
            //wait
        }
        String expected3 = "Hello TestSOAPInputMessage3";
        SAXSource saxSourceResp3 = tssh.getSAXSource();
        assertNotNull(saxSourceResp3);
        checkSAXSource(expected3, saxSourceResp3);

    }    
    
    public void testStreamSourceMESSAGE() throws Exception {

        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        InputStream is =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        StreamSource streamSourceReq = new StreamSource(is);
        assertNotNull(streamSourceReq);
        
        InputStream is1 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq1.xml");
        StreamSource streamSourceReq1 = new StreamSource(is1);
        assertNotNull(streamSourceReq1);
        
        InputStream is2 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq2.xml");
        StreamSource streamSourceReq2 = new StreamSource(is2);
        assertNotNull(streamSourceReq2);
        
        InputStream is3 =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq3.xml");
        StreamSource streamSourceReq3 = new StreamSource(is3);
        assertNotNull(streamSourceReq3);
        
        
    

        Dispatch<StreamSource> disp = service.createDispatch(portName,
                                                            StreamSource.class, Service.Mode.MESSAGE);
        StreamSource streamSourceResp = disp.invoke(streamSourceReq);
        assertNotNull(streamSourceResp);
        String expected = "Hello TestSOAPInputMessage";
        checkStreamSource(expected, streamSourceResp);
        
        disp.invokeOneWay(streamSourceReq1);
        
        Response response = disp.invokeAsync(streamSourceReq2);
        StreamSource streamSourceResp2 = (StreamSource)response.get();
        assertNotNull(streamSourceResp2);
        String expected2 = "Hello TestSOAPInputMessage2";
        checkStreamSource(expected2, streamSourceResp2);
        
        
        TestStreamSourceHandler tssh = new TestStreamSourceHandler();
        Future fd = disp.invokeAsync(streamSourceReq3, tssh);
        assertNotNull(fd);
        while (!fd.isDone()) {
            //wait
        }
        String expected3 = "Hello TestSOAPInputMessage3";
        StreamSource streamSourceResp3 = tssh.getStreamSource();
        assertNotNull(streamSourceResp3);
        checkStreamSource(expected3, streamSourceResp3);

    }
   
    
    private void checkSAXSource(String expected, SAXSource source) {
        
        InputSource inputSource =  source.getInputSource();
        InputStream is = inputSource.getByteStream();
        
        int i = 0;
        StringBuilder sb = new StringBuilder();
        try {
            while (i != -1) {
                i = is.read();
                sb.append((char)i);           
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String received = sb.toString();
        assertTrue("Expected: " + expected, received.contains(expected));
        
    }
    
    private void checkStreamSource(String expected, StreamSource source) {
        
        InputStream is = source.getInputStream();
        
        int i = 0;
        StringBuilder sb = new StringBuilder();
        try {
            while (i != -1) {
                i = is.read();
                sb.append((char)i);           
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String received = sb.toString();
        assertTrue("Expected: " + expected, received.contains(expected));
        
    }
    
    
    
    class TestSOAPMessageHandler implements AsyncHandler<SOAPMessage> {   
        
        String replyBuffer;
        
        public void handleResponse(Response<SOAPMessage> response) {
            try {
                SOAPMessage reply = response.get();
                replyBuffer = reply.getSOAPBody().getTextContent();
            } catch (Exception e) {
                e.printStackTrace();
            }            
        } 
        
        public String getReplyBuffer() {
            return replyBuffer;
        }
    }
    
    class TestDOMSourceHandler implements AsyncHandler<DOMSource> {   
        
        String replyBuffer;
        
        public void handleResponse(Response<DOMSource> response) {
            try {
                DOMSource reply = response.get();
                replyBuffer = reply.getNode().getFirstChild().getTextContent();
            } catch (Exception e) {
                e.printStackTrace();
            }            
        } 
        
        public String getReplyBuffer() {
            return replyBuffer;
        }
    }
    
    class TestSAXSourceHandler implements AsyncHandler<SAXSource> {   
        
        SAXSource reply;
        
        public void handleResponse(Response<SAXSource> response) {
            try {
                reply = response.get();
   
            } catch (Exception e) {
                e.printStackTrace();
            }            
        } 
        
        public SAXSource getSAXSource() {
            return reply;
        }
    }
    
    class TestStreamSourceHandler implements AsyncHandler<StreamSource> {   
        
        StreamSource reply;
        
        public void handleResponse(Response<StreamSource> response) {
            try {
                reply = response.get();
   
            } catch (Exception e) {
                e.printStackTrace();
            }            
        } 
        
        public StreamSource getStreamSource() {
            return reply;
        }
    }

}
