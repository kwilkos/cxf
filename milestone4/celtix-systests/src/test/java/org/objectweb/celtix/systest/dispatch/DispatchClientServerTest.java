package org.objectweb.celtix.systest.dispatch;

import java.io.InputStream;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;
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

        Dispatch<SOAPMessage> disp = service.createDispatch(portName,
                                                            SOAPMessage.class, Service.Mode.MESSAGE);
        SOAPMessage soapResMsg = disp.invoke(soapReqMsg);
        assertNotNull(soapResMsg);

        String expected = "Hello TestSOAPInputMessage";

        assertEquals("Response should be : Hello TestSOAPInputMessage",
                     expected, soapResMsg.getSOAPBody().getTextContent());
    }

    public void testDOMSourceMESSAGE() throws Exception {

        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        InputStream is =  getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null,  is);

        DOMSource domReqMsg = new DOMSource(soapReqMsg.getSOAPPart());

        assertNotNull(domReqMsg);

        Dispatch<DOMSource> disp = service.createDispatch(portName,
                                                            DOMSource.class, Service.Mode.MESSAGE);
        DOMSource domResMsg = disp.invoke(domReqMsg);
        assertNotNull(domResMsg);

        String expected = "Hello TestSOAPInputMessage";

        assertEquals("Response should be : Hello TestSOAPInputMessage",
                     expected, domResMsg.getNode().getFirstChild().getTextContent());

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

        Dispatch<DOMSource> disp = service.createDispatch(portName,
                                                            DOMSource.class, Service.Mode.PAYLOAD);
        DOMSource domResMsg = disp.invoke(domReqMsg);
        assertNotNull(domResMsg);

        String expected = "Hello TestSOAPInputMessage";

        assertEquals("Response should be : Hello TestSOAPInputMessage",
                     expected, domResMsg.getNode().getFirstChild().getTextContent());

    }

}
