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
package org.apache.cxf.management;

import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;


import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.event.ComponentEventFilter;
import org.apache.cxf.event.Event;
import org.apache.cxf.event.EventProcessor;
import org.apache.cxf.workqueue.WorkQueueInstrumentation;
import org.apache.cxf.workqueue.WorkQueueManagerImpl;

public class InstrumentationManagerTest extends TestCase {
    InstrumentationManager im;
    Bus bus;
    
    public void setUp() throws Exception {
        CXFBusFactory bf = new CXFBusFactory();
        bus = new CXFBusFactory().createBus();
        bf.setDefaultBus(bus);
        im = bus.getExtension(InstrumentationManager.class);
    }
    
    public void tearDown() throws Exception {
        //test case had done the bus.shutdown
        bus.shutdown(true);
    }
    
    // try to get WorkQueue information
    public void testWorkQueueInstrumentation() throws Exception {
        assertTrue("Instrumentation Manager should not be null", im != null);
        //im.getAllInstrumentation();
        WorkQueueManagerImpl wqm = new WorkQueueManagerImpl();
        wqm.setBus(bus);
        EventProcessor ep = bus.getExtension(EventProcessor.class);
        QName eventID = new QName(ComponentEventFilter.COMPONENT_CREATED_EVENT);
        if (null != ep) {
            System.out.println("send automaticWorkQueue created event");
            ep.sendEvent(new Event(wqm, eventID));
        }        
        
        //NOTE: now the bus WorkQueueManager is lazy load , if WorkQueueManager
        //create with bus , this test could be failed.
        List<Instrumentation> list = im.getAllInstrumentation();
        //NOTE: change for the BindingManager and TransportFactoryManager instrumentation
        // create with the bus.
        assertEquals("Too many instrumented items", 1, list.size());
        Instrumentation it1 = list.get(0);
        //Instrumentation it2 = list.get(3);
        assertTrue("Item 1 not a WorkQueueInstrumentation",
                   WorkQueueInstrumentation.class.isAssignableFrom(it1.getClass()));
        
        // not check for the instrumentation unique name
        // sleep for the MBServer connector thread startup
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // do nothing
        }
        eventID = new QName(ComponentEventFilter.COMPONENT_REMOVED_EVENT);
        if (null != ep) {
            ep.sendEvent(new Event(wqm, eventID));
        }    
        assertEquals("Instrumented stuff not removed from list", 0, list.size());
        bus.shutdown(true);
        assertEquals("Instrumented stuff not removed from list", 0, list.size());
    }




}
