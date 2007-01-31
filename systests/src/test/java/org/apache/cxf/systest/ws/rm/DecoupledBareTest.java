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

package org.apache.cxf.systest.ws.rm;

import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.systest.common.TestServerBase;
import org.apache.hello_world_soap_http.DocLitBare;
import org.apache.hello_world_soap_http.DocLitBareGreeterImpl;
import org.apache.hello_world_soap_http.SOAPServiceAddressingDocLitBare;
import org.apache.hello_world_soap_http.types.BareDocumentResponse;



/**
 * Tests the addition of WS-RM properties to application messages and the
 * exchange of WS-RM protocol messages.
 */
public class DecoupledBareTest extends ClientServerTestBase {

    private static final Logger LOG = Logger.getLogger(DecoupledBareTest.class.getName());
    private Bus bus;

    public static class Server extends TestServerBase {
        
        protected void run()  {            
            SpringBusFactory bf = new SpringBusFactory();
            Bus bus = bf.createBus("/org/apache/cxf/systest/ws/rm/decoupled_bare.xml");
            BusFactory.setDefaultBus(bus);
            
            Object implementor = new DocLitBareGreeterImpl();
            String address = "http://localhost:7600/SoapContext/SoapPort";
            Endpoint.publish(address, implementor);
            LOG.info("Published server endpoint.");
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
        TestSuite suite = new TestSuite(DecoupledBareTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
            
            public void setUp() throws Exception {
                startServers();
                LOG.fine("Started server.");  
            }
        };
    }
    
    public void tearDown() {
        bus.shutdown(true);
    }
    
    public void testDecoupled() throws Exception {
        SpringBusFactory bf = new SpringBusFactory();
        bus = bf.createBus("/org/apache/cxf/systest/ws/rm/decoupled_bare.xml");
        BusFactory.setDefaultBus(bus);
       
        SOAPServiceAddressingDocLitBare service = new SOAPServiceAddressingDocLitBare();
        assertNotNull(service);

        DocLitBare greeter = service.getSoapPort();
       
        BareDocumentResponse bareres = greeter.testDocLitBare("MySimpleDocument");
        assertNotNull("no response for operation testDocLitBare", bareres);
        assertEquals("CXF", bareres.getCompany());
        assertTrue(bareres.getId() == 1);
    }
}
