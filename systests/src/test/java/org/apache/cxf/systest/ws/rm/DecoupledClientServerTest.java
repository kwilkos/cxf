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
import org.apache.cxf.greeter_control.Greeter;
import org.apache.cxf.greeter_control.GreeterService;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.systest.common.TestServerBase;


/**
 * Tests the addition of WS-RM properties to application messages and the
 * exchange of WS-RM protocol messages.
 */
public class DecoupledClientServerTest extends ClientServerTestBase {

    private static final Logger LOG = Logger.getLogger(DecoupledClientServerTest.class.getName());
    private Bus bus;

    public static class Server extends TestServerBase {
        
        protected void run()  {            
            SpringBusFactory bf = new SpringBusFactory();
            Bus bus = bf.createBus("/org/apache/cxf/systest/ws/rm/decoupled.xml");
            BusFactory.setDefaultBus(bus);
            LoggingInInterceptor in = new LoggingInInterceptor();
            bus.getInInterceptors().add(in);
            bus.getInFaultInterceptors().add(in);
            LoggingOutInterceptor out = new LoggingOutInterceptor();
            bus.getOutInterceptors().add(out);
            bus.getOutFaultInterceptors().add(out);
            
            GreeterImpl implementor = new GreeterImpl();
            implementor.setDelay(8000);
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
        TestSuite suite = new TestSuite(DecoupledClientServerTest.class);
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
        bus = bf.createBus("/org/apache/cxf/systest/ws/rm/decoupled.xml");
        BusFactory.setDefaultBus(bus);
        LoggingInInterceptor in = new LoggingInInterceptor();
        bus.getInInterceptors().add(in);
        bus.getInFaultInterceptors().add(in);
        LoggingOutInterceptor out = new LoggingOutInterceptor();
        bus.getOutInterceptors().add(out);
        bus.getOutFaultInterceptors().add(out);
        
        GreeterService gs = new GreeterService();
        final Greeter greeter = gs.getGreeterPort();
        LOG.fine("Created greeter client.");
        
        class TwowayThread extends Thread {

            String response;
            
            @Override
            public void run() {
                response = greeter.greetMe("twoway");
            }
   
        }
        
        TwowayThread t = new TwowayThread();    
        t.start();
        
        // allow for partial response to twoway request to arrive
        
        long wait = 3000;
        while (wait > 0) {
            long start = System.currentTimeMillis();
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ex) {
                // ignore
            }
            wait -= System.currentTimeMillis() - start;
        }

        greeter.greetMeOneWay("oneway");
        
        t.join();
        assertEquals("Unexpected response to twoway request", "oneway", t.response);
        
    }
}
