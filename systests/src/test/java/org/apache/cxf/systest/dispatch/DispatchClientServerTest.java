/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.systest.dispatch;

import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.systest.common.TestServerBase;
import org.apache.hello_world_soap_http.GreeterImpl;
import org.apache.hello_world_soap_http.SOAPService;
import org.apache.hello_world_soap_http.types.GreetMe;
import org.apache.hello_world_soap_http.types.GreetMeResponse;

public class DispatchClientServerTest extends ClientServerTestBase {

    private final QName serviceName = new QName("http://apache.org/hello_world_soap_http", 
                                                "SOAPDispatchService");
    private final QName portName = new QName("http://apache.org/hello_world_soap_http", 
                                             "SoapDispatchPort");

    public static class Server extends TestServerBase {

        protected void run() {
            Object implementor = new GreeterImpl();
            String address = "http://localhost:9006/SOAPDispatchService/SoapDispatchPort";
            Endpoint.publish(address, implementor);

        }

        public static void main(String[] args) {
            try {
                Server s = new Server();
                s.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            } finally {
                System.out.println("done!");
            }
        }
    }

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

        InputStream is = getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null, is);
        assertNotNull(soapReqMsg);

        Dispatch<SOAPMessage> disp = service
            .createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        SOAPMessage soapResMsg = disp.invoke(soapReqMsg);
        assertNotNull(soapResMsg);
        String expected = "Hello TestSOAPInputMessage";
        assertEquals("Response should be : Hello TestSOAPInputMessage", expected, soapResMsg.getSOAPBody()
            .getTextContent());

    }

    public void testDOMSourceMESSAGE() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        InputStream is = getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null, is);
        DOMSource domReqMsg = new DOMSource(soapReqMsg.getSOAPPart());
        assertNotNull(domReqMsg);

        Dispatch<DOMSource> disp = service.createDispatch(portName, DOMSource.class, Service.Mode.MESSAGE);
        DOMSource domResMsg = disp.invoke(domReqMsg);
        assertNotNull(domResMsg);
        String expected = "Hello TestSOAPInputMessage";

        assertEquals("Response should be : Hello TestSOAPInputMessage", expected, domResMsg.getNode()
            .getFirstChild().getTextContent());
    }

    public void testDOMSourcePAYLOAD() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        InputStream is = getClass().getResourceAsStream("resources/GreetMeDocLiteralReq.xml");
        SOAPMessage soapReqMsg = MessageFactory.newInstance().createMessage(null, is);
        DOMSource domReqMsg = new DOMSource(soapReqMsg.getSOAPBody().extractContentAsDocument());
        assertNotNull(domReqMsg);

        Dispatch<DOMSource> disp = service.createDispatch(portName, DOMSource.class, Service.Mode.PAYLOAD);

        // invoke
        DOMSource domResMsg = disp.invoke(domReqMsg);
        assertNotNull(domResMsg);
        String expected = "Hello TestSOAPInputMessage";
        assertEquals("Response should be : Hello TestSOAPInputMessage", expected, domResMsg.getNode()
            .getFirstChild().getTextContent());
    }

    public void testJAXBObjectPAYLOAD() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        JAXBContext jc = JAXBContext.newInstance("org.apache.hello_world_soap_http.types");
        Dispatch<Object> disp = service.createDispatch(portName, jc, Service.Mode.PAYLOAD);

        String expected = "Hello Jeeves";
        GreetMe greetMe = new GreetMe();
        greetMe.setRequestType("Jeeves");

        Object response = disp.invoke(greetMe);
        assertNotNull(response);
        String responseValue = ((GreetMeResponse)response).getResponseType();
        assertTrue("Expected string, " + expected, expected.equals(responseValue));
    }

}
