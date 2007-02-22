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

package org.apache.cxf.systest.ws.policy;

import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.greeter_control.BasicGreeterService;
import org.apache.cxf.greeter_control.Greeter;
import org.apache.cxf.greeter_control.PingMeFault;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.systest.common.TestServerBase;


/**
 * Tests the use of the WS-Policy Framework to automatically engage WS-Addressing and
 * WS-RM in response to Policies defined for the endpoint via an external policy attachment.
 */
public class PolicyClientServerTest extends ClientServerTestBase {

    private static final Logger LOG = Logger.getLogger(PolicyClientServerTest.class.getName());
    private Bus bus;

    public static class Server extends TestServerBase {
    
        protected void run()  {            
            SpringBusFactory bf = new SpringBusFactory();
            Bus bus = bf.createBus("org/apache/cxf/systest/ws/policy/addr-only.xml");
            BusFactory.setDefaultBus(bus);
            LoggingInInterceptor in = new LoggingInInterceptor();
            bus.getInInterceptors().add(in);
            bus.getInFaultInterceptors().add(in);
            LoggingOutInterceptor out = new LoggingOutInterceptor();
            bus.getOutInterceptors().add(out);
            bus.getOutFaultInterceptors().add(out);
            
            GreeterImpl implementor = new GreeterImpl();
            String address = "http://localhost:9020/SoapContext/GreeterPort";
            Endpoint.publish(address, implementor);
            LOG.info("Published greeter endpoint.");
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
        TestSuite suite = new TestSuite(PolicyClientServerTest.class);
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
    
    public void testUsingAddressing() throws Exception {
        SpringBusFactory bf = new SpringBusFactory();
        bus = bf.createBus("org/apache/cxf/systest/ws/policy/addr-only.xml");
        BusFactory.setDefaultBus(bus);
        LoggingInInterceptor in = new LoggingInInterceptor();
        bus.getInInterceptors().add(in);
        bus.getInFaultInterceptors().add(in);
        LoggingOutInterceptor out = new LoggingOutInterceptor();
        bus.getOutInterceptors().add(out);
        bus.getOutFaultInterceptors().add(out);
        
        BasicGreeterService gs = new BasicGreeterService();
        final Greeter greeter = gs.getGreeterPort();
        LOG.fine("Created greeter client.");

        // oneway

        greeter.greetMeOneWay("CXF");

        // two-way

        assertEquals("CXF", greeter.greetMe("cxf")); 
     
        // exception

        try {
            greeter.pingMe();
        } catch (PingMeFault ex) {
            fail("First invocation should have succeeded.");
        } 
       
        try {
            greeter.pingMe();
            fail("Expected PingMeFault not thrown.");
        } catch (PingMeFault ex) {
            assertEquals(2, ex.getFaultInfo().getMajor());
            assertEquals(1, ex.getFaultInfo().getMinor());
        } 
    }
}
