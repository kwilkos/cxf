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

package org.apache.cxf.systest.ws.addressing;

import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Response;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.greeter_control.AbstractGreeterImpl;
import org.apache.cxf.greeter_control.Greeter;
import org.apache.cxf.greeter_control.GreeterService;
import org.apache.cxf.greeter_control.types.GreetMeResponse;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.apache.cxf.testutil.common.AbstractBusTestServerBase;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Tests the use of WS-Addressing and decoupled response endpoint in
 * conjunction with the JAX-WS asynchronous invocation model.
 */
@Ignore
public class DecoupledAsyncTest extends AbstractBusClientServerTestBase {

    private static final Logger LOG = Logger.getLogger(DecoupledAsyncTest.class.getName());
    private Bus bus;
    
    @WebService(serviceName = "GreeterService",
                portName = "GreeterPort",
                endpointInterface = "org.apache.cxf.greeter_control.Greeter",
                targetNamespace = "http://cxf.apache.org/greeter_control",
                wsdlLocation = "testutils/greeter_control.wsdl")
    public static class GreeterImpl extends AbstractGreeterImpl {
        
    }

    public static class Server extends AbstractBusTestServerBase {
        
        protected void run()  {            
            SpringBusFactory bf = new SpringBusFactory();
            Bus bus = bf.createBus("/org/apache/cxf/systest/ws/addressing/cxf.xml");
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
    
    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(Server.class));
    }
    
    // this works fine
    @Test
    public void testPlainAsync() throws Exception {
        testAsync(null);        
    }
    
    // but this does not
    @Test
    public void testDecoupledAsync() throws Exception {
        testAsync("/org/apache/cxf/systest/ws/addressing/cxf.xml");
    }
            
    void testAsync(String cfg) throws Exception {
        SpringBusFactory bf = new SpringBusFactory();
        if (null == cfg) {
            bus = bf.createBus(); 
        } else {
            bus = bf.createBus(cfg);
        }
        
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
        
        Response<GreetMeResponse> response = greeter.greetMeAsync("cxf");
        
        // allow for response to arrive asynchronously
        
        long wait = 3000;
        while (wait > 0 && !response.isDone()) {
            long start = System.currentTimeMillis();
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ex) {
                // ignore
            }
            wait -= System.currentTimeMillis() - start;
        }

        assertTrue(response.isDone());
        assertEquals("CXF", response.get().getResponseType());        
    }
}
