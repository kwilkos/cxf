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

package org.apache.cxf.systest.lifecycle;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerLifeCycleListener;
import org.apache.cxf.endpoint.ServerLifeCycleManager;
import org.apache.cxf.systest.ws.addressing.GreeterImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LifeCycleTest extends Assert {
    private static final int RECURSIVE_LIMIT = 3;
    private static final String[] ADDRESSES = 
    {"http://localhost:9056/SoapContext/SoapPort",
     "http://localhost:9057/SoapContext/SoapPort",
     "http://localhost:9058/SoapContext/SoapPort",
     "http://localhost:9059/SoapContext/SoapPort"};

    private Bus bus;
    private ServerLifeCycleManager manager;
    private int recursiveCount;
    private Endpoint[] recursiveEndpoints;
    private Map<String, Integer> startNotificationMap;
    private Map<String, Integer> stopNotificationMap;
    
    @Before
    public void setUp() throws Exception {
        bus = BusFactory.getDefaultBus();
        manager = bus.getExtension(ServerLifeCycleManager.class);
        recursiveCount = 0;
        recursiveEndpoints = new Endpoint[RECURSIVE_LIMIT];
        startNotificationMap = new HashMap<String, Integer>();
        stopNotificationMap = new HashMap<String, Integer>();
    }
    
    @After
    public void tearDown() throws Exception {
        bus.shutdown(true);
    }
    
    @Test
    public void testRecursive() {        
        assertNotNull("unexpected non-null ServerLifeCycleManager", manager);
        
        manager.registerListener(new ServerLifeCycleListener() {
            public void startServer(Server server) {
                verifyNoPrior(startNotificationMap,
                              server.getEndpoint().getEndpointInfo().getAddress());
                if (recursiveCount < RECURSIVE_LIMIT) {
                    recursiveEndpoints[recursiveCount++] =
                        Endpoint.publish(ADDRESSES[recursiveCount],
                                         new GreeterImpl());                    
                }
            }
            public void stopServer(Server server) {
                verifyNoPrior(stopNotificationMap,
                              server.getEndpoint().getEndpointInfo().getAddress());
                if (recursiveCount > 0) {
                    recursiveEndpoints[--recursiveCount].stop();                    
                }
            }
        });
        
        Endpoint.publish(ADDRESSES[0], new GreeterImpl()).stop();
    }
    
    private void verifyNoPrior(Map<String, Integer> notificationMap, String address) {
        synchronized (notificationMap) {
            assertFalse("unexpected prior notification for: " + address,
                        notificationMap.containsKey(address));
        }
    }

}
