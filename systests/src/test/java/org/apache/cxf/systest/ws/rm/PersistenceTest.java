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

import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.greeter_control.Greeter;
import org.apache.cxf.greeter_control.GreeterService;
import org.apache.cxf.systest.ws.policy.GreeterImpl;
import org.apache.cxf.systest.ws.util.InMessageRecorder;
import org.apache.cxf.systest.ws.util.MessageFlow;
import org.apache.cxf.systest.ws.util.MessageRecorder;
import org.apache.cxf.systest.ws.util.OutMessageRecorder;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.apache.cxf.testutil.common.AbstractBusTestServerBase;
import org.apache.cxf.ws.rm.DestinationSequence;
import org.apache.cxf.ws.rm.RMConstants;
import org.apache.cxf.ws.rm.RMManager;
import org.apache.cxf.ws.rm.SourceSequence;
import org.apache.cxf.ws.rm.persistence.RMMessage;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.apache.cxf.ws.rm.persistence.jdbc.RMTxStore;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the addition of WS-RM properties to application messages and the
 * exchange of WS-RM protocol messages.
 */
public class PersistenceTest extends AbstractBusClientServerTestBase {

    private static final Logger LOG = Logger.getLogger(PersistenceTest.class.getName());
    private static final String GREETMEONEWAY_ACTION = null;
    
    private Greeter greeter;
    private OutMessageRecorder out;
    private InMessageRecorder in;

    public static class Server extends AbstractBusTestServerBase {

        protected void run() {
            SpringBusFactory bf = new SpringBusFactory();
            Bus bus = bf.createBus("/org/apache/cxf/systest/ws/rm/oneway-client-crash.xml");
            BusFactory.setDefaultBus(bus);

            GreeterImpl implementor = new GreeterImpl();
            String address = "http://localhost:9020/SoapContext/GreeterPort";
            Endpoint.publish(address, implementor);
            LOG.info("Published greeter endpoint.");
        }

        public static void main(String[] args) {
            try {
                RMTxStore.deleteDatabaseFiles();
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
        String derbyHome = System.getProperty("derby.system.home");
        try {
            System.setProperty("derby.system.home", derbyHome + "-server");
            assertTrue("server did not launch correctly", launchServer(Server.class));
        } finally {
            System.setProperty("derby.system.home", derbyHome);
        }
        RMTxStore.deleteDatabaseFiles();
    }
    
    @AfterClass
    public static void tearDownOnce() {
        RMTxStore.deleteDatabaseFiles(RMTxStore.DEFAULT_DATABASE_DIR, false);
    }

    @Before
    public void setUp() {
        SpringBusFactory bf = new SpringBusFactory();
        bus = bf.createBus("/org/apache/cxf/systest/ws/rm/oneway-client-crash.xml");
        BusFactory.setDefaultBus(bus);

        GreeterService gs = new GreeterService();
        greeter = gs.getGreeterPort();

        out = new OutMessageRecorder();
        in = new InMessageRecorder();

        bus.getOutInterceptors().add(out);
        bus.getInInterceptors().add(in);
    }

    // TODO: refactor into one test as we cannot rely on the order in which the tests are executed
    // (especially on IBM)

    @Test
    public void testPopulateStore() throws Exception {
        greeter.greetMeOneWay("one");
        greeter.greetMeOneWay("two");
        greeter.greetMeOneWay("three");
        
        MessageFlow mf = new MessageFlow(out.getOutboundMessages(), in.getInboundMessages());
        
        awaitMessages(4, 4);
        
        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {RMConstants.getCreateSequenceAction(), 
                                                 GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, 
                                                 GREETMEONEWAY_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);


        mf.verifyMessages(4, false);
        mf.verifyPartialResponses(3);
        mf.verifyMessageNumbers(new String[4], false);
        boolean[] expectedAcks = new boolean[4];
        mf.verifyAcknowledgements(expectedAcks, false);
        mf.purgePartialResponses();
        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction()};
        mf.verifyActions(expectedActions, false);
                
        RMManager manager = bus.getExtension(RMManager.class);
        assertNotNull(manager);
        
        RMStore store = manager.getStore();
        assertNotNull(store);
        
        Client client = ClientProxy.getClient(greeter);
        String id = client.getEndpoint().getEndpointInfo().getService().getName()
            + "." + client.getEndpoint().getEndpointInfo().getName();
        
        Collection<DestinationSequence> dss =
            store.getDestinationSequences(id);
        assertEquals(1, dss.size());
        
        Collection<SourceSequence> sss =
            store.getSourceSequences(id);
        assertEquals(1, sss.size());
        
        Collection<RMMessage> msgs = 
            store.getMessages(sss.iterator().next().getIdentifier(), true);
        assertEquals(3, msgs.size());  
        
        msgs = 
            store.getMessages(sss.iterator().next().getIdentifier(), false);
        assertEquals(0, msgs.size());  
    }
    
    @Ignore
    @Test
    public void testRecover() throws Exception {
        // do nothing - resends should happen in the background
        
        int expectedOut = 2;
        awaitMessages(2, 0);
    
        MessageFlow mf = new MessageFlow(out.getOutboundMessages(), in.getInboundMessages());        
        String[] expectedActions = new String[expectedOut];
        for (int i = 0; i < expectedOut; i++) {
            expectedActions[i] = GREETMEONEWAY_ACTION;
        }
        mf.verifyActions(expectedActions, true);
    }
    
    private void awaitMessages(int nExpectedOut, int nExpectedIn) {
        awaitMessages(nExpectedOut, nExpectedIn, 10000);
    }
    
    private void awaitMessages(int nExpectedOut, int nExpectedIn, int timeout) {
        MessageRecorder mr = new MessageRecorder(out, in);
        mr.awaitMessages(nExpectedOut, nExpectedIn, timeout);
    }

}
