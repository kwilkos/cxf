package org.objectweb.celtix.systest.dispatch;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

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

}
