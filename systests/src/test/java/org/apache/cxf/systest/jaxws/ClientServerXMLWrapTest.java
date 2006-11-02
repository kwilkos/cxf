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

package org.apache.cxf.systest.jaxws;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.cxf.message.Message;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.TestServerBase;
import org.apache.hello_world_xml_http.wrapped.Greeter;
import org.apache.hello_world_xml_http.wrapped.GreeterImpl;
import org.apache.hello_world_xml_http.wrapped.PingMeFault;
import org.apache.hello_world_xml_http.wrapped.XMLService;

public class ClientServerXMLWrapTest extends TestCase {

    private final QName serviceName = new QName("http://apache.org/hello_world_xml_http/wrapped",
            "XMLService");

    private final QName portName = new QName("http://apache.org/hello_world_xml_http/wrapped", "XMLPort");

    private final QName fakePortName = 
        new QName("http://apache.org/hello_world_xml_http/wrapped", "FakePort");

    public static class Server extends TestServerBase {

        protected void run() {
            Object implementor = new GreeterImpl();
            String address = "http://localhost:9032/XMLService/XMLPort";
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
        TestSuite suite = new TestSuite(ClientServerXMLWrapTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }

    public void testBasicConnection() throws Exception {

        XMLService service = new XMLService(
                this.getClass().getResource("/wsdl/hello_world_xml_wrapped.wsdl"), serviceName);
        assertNotNull(service);

        String response1 = new String("Hello ");
        String response2 = new String("Bonjour");
        try {
            Greeter greeter = service.getPort(portName, Greeter.class);
            String username = System.getProperty("user.name");
            String reply = greeter.greetMe(username);

            assertNotNull("no response received from service", reply);
            assertEquals(response1 + username, reply);

            reply = greeter.sayHi();
            assertNotNull("no response received from service", reply);
            assertEquals(response2, reply);

            greeter.greetMeOneWay(System.getProperty("user.name"));

        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }

    public void testAddPort() throws Exception {

        Service service = Service.create(serviceName);
        service.addPort(fakePortName, "http://cxf.apache.org/bindings/xformat",
                "http://localhost:9032/XMLService/XMLPort");
        assertNotNull(service);

        String response1 = new String("Hello ");
        String response2 = new String("Bonjour");
        Greeter greeter = service.getPort(fakePortName, Greeter.class);
        try {            
            String username = System.getProperty("user.name");
            String reply = greeter.greetMe(username);

            assertNotNull("no response received from service", reply);
            assertEquals(response1 + username, reply);

            reply = greeter.sayHi();
            assertNotNull("no response received from service", reply);
            assertEquals(response2, reply);

            greeter.greetMeOneWay(System.getProperty("user.name"));

        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
        BindingProvider bp = (BindingProvider)greeter;
        Map<String, Object> responseContext = bp.getResponseContext();
        Integer responseCode = (Integer) responseContext.get(Message.RESPONSE_CODE);        
        assertEquals(200, responseCode.intValue());                                    
    }

    public void testXMLFault() throws Exception {
        XMLService service = new XMLService(
                this.getClass().getResource("/wsdl/hello_world_xml_wrapped.wsdl"), serviceName);
        assertNotNull(service);
        Greeter greeter = service.getPort(portName, Greeter.class);
        try {            
            greeter.pingMe();
            fail("did not catch expected PingMeFault exception");
        } catch (PingMeFault ex) {
            assertEquals("minor value", 1, ex.getFaultInfo().getMinor());
            assertEquals("major value", 2, ex.getFaultInfo().getMajor());
            
            BindingProvider bp = (BindingProvider)greeter;
            Map<String, Object> responseContext = bp.getResponseContext();
            String contentType = (String) responseContext.get(Message.CONTENT_TYPE);
            assertEquals("text/xml", contentType);
            Integer responseCode = (Integer) responseContext.get(Message.RESPONSE_CODE);
            assertEquals(500, responseCode.intValue());                                    
        }
    }
}
