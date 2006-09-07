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

package org.apache.cxf.systest.provider;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.systest.common.TestServerBase;
import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.SOAPService;

public class ProviderClientServerTest extends ClientServerTestBase {
    public static class Server extends TestServerBase {

        protected void run() {
            Object implementor = new HWSoapMessageDocProvider();
            String address = "http://localhost:9003/SoapContext/SoapProviderPort";
            Endpoint.publish(address, implementor);
                        
//            implementor = new HWDOMSourceMessageProvider();
//            address = new String("http://localhost:9002/SOAPServiceRPCLit/SoapPort1");
//            Endpoint.publish(address, implementor);
//            
//            implementor = new HWDOMSourcePayloadProvider();
//            address = new String("http://localhost:9002/SOAPServiceRPCLit/SoapPort2");
//            Endpoint.publish(address, implementor); 
//            
//            implementor = new HWSAXSourceMessageProvider();
//            address = new String("http://localhost:9002/SOAPServiceRPCLit/SoapPort3");
//            Endpoint.publish(address, implementor); 
//            
//            implementor = new HWStreamSourceMessageProvider();
//            address = new String("http://localhost:9002/SOAPServiceRPCLit/SoapPort4");
//            Endpoint.publish(address, implementor); 
//            
//            implementor = new HWSAXSourcePayloadProvider();
//            address = new String("http://localhost:9002/SOAPServiceRPCLit/SoapPort5");
//            Endpoint.publish(address, implementor); 
//            
//            implementor = new HWStreamSourcePayloadProvider();
//            address = new String("http://localhost:9002/SOAPServiceRPCLit/SoapPort6");
//            Endpoint.publish(address, implementor);             
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
        TestSuite suite = new TestSuite(ProviderClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }

//    public void testSOAPMessageModeRPC() throws Exception {
//        
//        QName serviceName = 
//            new QName("http://apache.org/hello_world_rpclit", "SOAPServiceRPCLit");
//        QName portName = 
//            new QName("http://apache.org/hello_world_rpclit", "SoapPortRPCLit");
//
//        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
//        assertNotNull(wsdl);
//
//        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
//        assertNotNull(service);
//
//        String response1 = new String("TestGreetMeResponse");
//        String response2 = new String("TestSayHiResponse");
//        try {
//            GreeterRPCLit greeter = service.getPort(portName, GreeterRPCLit.class);
//            for (int idx = 0; idx < 2; idx++) {
//                String greeting = greeter.greetMe("Milestone-" + idx);
//                assertNotNull("no response received from service", greeting);
//                assertEquals(response1, greeting);
//
//                String reply = greeter.sayHi();
//                assertNotNull("no response received from service", reply);
//                assertEquals(response2, reply);
//            }
//        } catch (UndeclaredThrowableException ex) {
//            throw (Exception)ex.getCause();
//        }
//    }

    public void testSOAPMessageModeDocLit() throws Exception {
        
        QName serviceName = 
            new QName("http://apache.org/hello_world_soap_http", "SOAPProviderService");
        QName portName = 
            new QName("http://apache.org/hello_world_soap_http", "SoapProviderPort");

        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("TestSOAPOutputPMessage");
        String response2 = new String("Bonjour");
        try {
            Greeter greeter = service.getPort(portName, Greeter.class);
            for (int idx = 0; idx < 2; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                assertEquals(response1, greeting);

                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

}
