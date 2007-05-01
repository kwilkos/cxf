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

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.clustering.FailoverTargetSelector;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.greeter_control.ClusteredGreeterService;
import org.apache.cxf.greeter_control.Control;
import org.apache.cxf.greeter_control.ControlService;
import org.apache.cxf.greeter_control.Greeter;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Tests failover within a static cluster.
 */
public class FailoverTest extends AbstractBusClientServerTestBase {

    private static final Logger LOG =
        Logger.getLogger(FailoverTest.class.getName());
    private static final String FAILOVER_CONFIG =
        "org/apache/cxf/systest/clustering/failover.xml";
    private static final String REPLICA_A =
        "http://localhost:9051/SoapContext/GreeterPort";
    private static final String REPLICA_B =
        "http://localhost:9052/SoapContext/GreeterPort"; 
    private static final String REPLICA_C =
        "http://localhost:9053/SoapContext/GreeterPort"; 
    private static final String REPLICA_D =
        "http://localhost:9054/SoapContext/GreeterPort"; 


    private Bus bus;
    private Control control;
    private Greeter greeter;
    private List<String> targets;

    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly",
                   launchServer(Server.class));
    }
            
    @Before
    public void setUp() {
        targets = new ArrayList<String>();
        SpringBusFactory bf = new SpringBusFactory();    
        bus = bf.createBus(FAILOVER_CONFIG);
        BusFactory.setDefaultBus(bus);
    }
    
    @After
    public void tearDown() {
        if (null != control) {
            for (String address : targets) {
                assertTrue("Failed to stop greeter",
                           control.stopGreeter(address));
            }
        }
        targets = null;
        if (bus != null) {
            bus.shutdown(true);
        }
    }

    @Test
    public void testNoFailoverAcrossBindings() throws Exception {        
        startTarget(REPLICA_D);
        setupGreeter();

        try {
            greeter.greetMe("fred");
            fail("expected exception");
        } catch (Exception e) {
            verifyCurrentEndpoint(REPLICA_A);
        }
    }
    
    @Test
    public void testRevertExceptionOnUnsucessfulFailover() throws Exception {        
        startTarget(REPLICA_B);
        startTarget(REPLICA_C);
        setupGreeter();
        stopTarget(REPLICA_C);
        stopTarget(REPLICA_B);
        
        try {
            greeter.greetMe("fred");
            fail("expected exception");
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            // failover attempt bails after retried invocations on 
            // started & stopped replicas B & C fail with HTTP 404  
            // indicated by a thrown IOException("Not found"),
            // in which case we should revert back to the original 
            // java.net.ConnectionException on the unavailable 
            // replica A
            //
            assertTrue("should revert to original exception when no failover",
                       cause instanceof ConnectException);
            
            // similarly the current endpoint referenced by the client 
            // should also revert back to the origianl replica A
            //
            verifyCurrentEndpoint(REPLICA_A);
        }
    }

    @Test
    public void testInitialFailoverOnPrimaryReplicaUnavailable() throws Exception {
        startTarget(REPLICA_C);
        setupGreeter();
        String response = null;
        
        response = greeter.greetMe("fred");
        assertNotNull("expected non-null response", response);
        assertTrue("response unexpected unexpected target: " + response,
                   response.endsWith(REPLICA_C));
        verifyCurrentEndpoint(REPLICA_C);
        
        response = greeter.greetMe("joe");
        assertNotNull("expected non-null response", response);
        assertTrue("response unexpected unexpected target: " + response,
                   response.endsWith(REPLICA_C));        
    }

    @Test
    public void testFailoverOnCurrentReplicaDeath() throws Exception {
        startTarget(REPLICA_C);
        setupGreeter();
        String response = null;
        
        response = greeter.greetMe("fred");
        assertNotNull("expected non-null response", response);
        assertTrue("response unexpected unexpected target: " + response,
                   response.endsWith(REPLICA_C));
        verifyCurrentEndpoint(REPLICA_C);
        
        startTarget(REPLICA_B);
        stopTarget(REPLICA_C);
        
        response = greeter.greetMe("joe");
        assertNotNull("expected non-null response", response);
        assertTrue("response unexpected unexpected target: " + response,
                   response.endsWith(REPLICA_B));
        verifyCurrentEndpoint(REPLICA_B);
    }
    
    @Test
    public void testNoFailbackWhileCurrentReplicaLive() throws Exception {
        startTarget(REPLICA_C);
        setupGreeter();
        String response = null;
        
        response = greeter.greetMe("fred");
        assertNotNull("expected non-null response", response);
        assertTrue("response unexpected unexpected target: " + response,
                   response.endsWith(REPLICA_C));
        verifyCurrentEndpoint(REPLICA_C);

        startTarget(REPLICA_A);
        response = greeter.greetMe("joe");
        assertNotNull("expected non-null response", response);
        assertTrue("response unexpected unexpected target: " + response,
                   response.endsWith(REPLICA_C));
        verifyCurrentEndpoint(REPLICA_C);
        
        startTarget(REPLICA_B);
        response = greeter.greetMe("bob");
        assertNotNull("expected non-null response", response);
        assertTrue("response unexpected unexpected target: " + response,
                   response.endsWith(REPLICA_C));
        verifyCurrentEndpoint(REPLICA_C);
        
        stopTarget(REPLICA_B);
        response = greeter.greetMe("john");
        assertNotNull("expected non-null response", response);
        assertTrue("response unexpected unexpected target: " + response,
                   response.endsWith(REPLICA_C));
        verifyCurrentEndpoint(REPLICA_C);
        
        stopTarget(REPLICA_A);
        response = greeter.greetMe("mike");
        assertNotNull("expected non-null response", response);
        assertTrue("response unexpected unexpected target: " + response,
                   response.endsWith(REPLICA_C));
        verifyCurrentEndpoint(REPLICA_C);
    }
    
    private void startTarget(String address) {
        ControlService cs = new ControlService();
        control = cs.getControlPort();

        LOG.info("starting replicated target: " + address);
        assertTrue("Failed to start greeter", control.startGreeter(address));
        targets.add(address);
    }
    
    private void stopTarget(String address) {
        if (control != null
            && targets.contains(address)) {
            LOG.info("starting replicated target: " + address);
            assertTrue("Failed to start greeter", control.stopGreeter(address));
            targets.remove(address);
        }
    }

    private void verifyCurrentEndpoint(String replica) {
        assertEquals("unexpected current endpoint",
                     replica,
                     ClientProxy.getClient(greeter).getEndpoint().getEndpointInfo().getAddress());
    }
    
    private void setupGreeter() {
        ClusteredGreeterService cs = new ClusteredGreeterService();
        // REVISIT: why doesn't the generic (i.e. non-Port-specific)
        // Service.getPort() load the <jaxws:client> configuration?
        greeter = cs.getReplicatedPortA();
        assertTrue("unexpected conduit slector",
                   ClientProxy.getClient(greeter).getConduitSelector()
                   instanceof FailoverTargetSelector);
    }
}
