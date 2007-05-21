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

package org.apache.cxf.systest.clustering;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.endpoint.ServerLifeCycleListener;
import org.apache.cxf.endpoint.ServerLifeCycleManager;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.testutil.common.AbstractBusTestServerBase;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

public class Server extends AbstractBusTestServerBase {
   
    private static final String ADDRESS =
        "http://localhost:9001/SoapContext/ControlPort";
    private static final String TARGETS_CONFIG =
        "org/apache/cxf/systest/clustering/targets.xml";

    
    private String verified;

    protected void run()  {

        SpringBusFactory factory = new SpringBusFactory();
        Bus bus = factory.createBus(TARGETS_CONFIG);
        BusFactory.setDefaultBus(bus);
        setBus(bus);

        ServerLifeCycleManager manager =
            bus.getExtension(ServerLifeCycleManager.class);
        if (manager != null) {
            manager.registerListener(new ServerLifeCycleListener() {
                public void startServer(org.apache.cxf.endpoint.Server server) {
                    org.apache.cxf.endpoint.Endpoint endpoint
                        = server.getEndpoint();
                    String portName =
                        endpoint.getEndpointInfo().getName().getLocalPart();
                    if ("ReplicatedPortA".equals(portName)) {
                        
                        List<AbstractFeature> active = endpoint.getActiveFeatures();
                        if (!(active.size() == 1
                              && active.get(0) instanceof WSAddressingFeature)
                              && AbstractFeature.getActive(active,
                                                           WSAddressingFeature.class)
                                 == active.get(0)) {
                            verified = "unexpected active features: " + active;
                        }
                    } else {
                        List<AbstractFeature> active = endpoint.getActiveFeatures();
                        if (!(active == null 
                              || active.size() == 0
                              || AbstractFeature.getActive(active,
                                                           WSAddressingFeature.class)
                                 == null)) {
                            verified = "unexpected active features: " + active;
                        }                        
                    }
                }
                public void stopServer(org.apache.cxf.endpoint.Server server) {
                }                
            });
        } else {
            verified = "cannot access ServerLifeCycleManager";
        }

        ControlImpl implementor = new ControlImpl();
        Endpoint.publish(ADDRESS, implementor);
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
    
    /**
     * Used to facilitate assertions on server-side behaviour.
     *
     * @param log logger to use for diagnostics if assertions fail
     * @return true if assertions hold
     */
    protected boolean verify(Logger log) {
        if (verified != null) {
            log.log(Level.WARNING, 
                    "Active Feature verification failed: {0}",
                    verified);
        }
        return verified == null;
    }
}
