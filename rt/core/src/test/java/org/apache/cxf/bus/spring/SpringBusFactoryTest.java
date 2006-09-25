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

package org.apache.cxf.bus.spring;

import java.util.List;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.endpoint.ServerRegistry;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.workqueue.WorkQueueManager;
import org.apache.cxf.wsdl.WSDLManager;

public class SpringBusFactoryTest extends TestCase {

    
    public void testDefault() {
        Bus bus = new SpringBusFactory().createBus();
        assertNotNull(bus);
        BindingFactoryManager bfm = bus.getExtension(BindingFactoryManager.class);  
        assertNotNull("No binding factory manager", bfm);
        assertNotNull("No destination factory manager", bus.getExtension(DestinationFactoryManager.class));
        assertNotNull("No conduit initiator manager", bus.getExtension(ConduitInitiatorManager.class));
        assertNotNull("No wsdl manager", bus.getExtension(WSDLManager.class));
        assertNotNull("No phase manager", bus.getExtension(PhaseManager.class));
        assertNotNull("No workqueue manager", bus.getExtension(WorkQueueManager.class));
        assertNotNull("No lifecycle manager", bus.getExtension(BusLifeCycleManager.class));
        assertNotNull("No service registry", bus.getExtension(ServerRegistry.class));
        
        try {
            bfm.getBindingFactory("http://cxf.apache.org/unknown");
        } catch (BusException ex) {
            // expected
        }
        
        assertEquals("Unexpected interceptors", 0, bus.getInInterceptors().size());
        assertEquals("Unexpected interceptors", 0, bus.getInFaultInterceptors().size());
        assertEquals("Unexpected interceptors", 0, bus.getOutInterceptors().size());
        assertEquals("Unexpected interceptors", 0, bus.getOutFaultInterceptors().size());
        
    }
    
    public void testCustom() {
        String cfgFile = "org/apache/cxf/bus/spring/resources/bus-overwrite.xml";
        Bus bus = new SpringBusFactory().createBus(cfgFile, true);
        assertNotNull(bus);
        List<Interceptor> interceptors = bus.getInInterceptors();
        assertEquals("Unexpected number of interceptors", 2, interceptors.size());
        assertEquals("Unexpected interceptor", "in-a", interceptors.get(0).toString());
        assertEquals("Unexpected interceptor", "in-b", interceptors.get(1).toString());
        interceptors = bus.getInFaultInterceptors();
        assertEquals("Unexpected number of interceptors", 1, interceptors.size());
        assertEquals("Unexpected interceptor", "in-fault", interceptors.get(0).toString());
        interceptors = bus.getOutFaultInterceptors();
        assertEquals("Unexpected number of interceptors", 1, interceptors.size());
        assertEquals("Unexpected interceptor", "out-fault", interceptors.get(0).toString());
        interceptors = bus.getOutInterceptors();
        assertEquals("Unexpected number of interceptors", 1, interceptors.size());
        assertEquals("Unexpected interceptor", "out", interceptors.get(0).toString());
        
    }
    
    static class TestInterceptor implements Interceptor {

        private String name;
        
        public TestInterceptor() {            
        }
        
        public void setName(String n) {
            name = n;
        }
               
        @Override
        public String toString() {
            return name;
        }
        
        public void handleFault(Message message) {  
        }

        public void handleMessage(Message message) throws Fault {   
        }
        
    }
     
    
}
