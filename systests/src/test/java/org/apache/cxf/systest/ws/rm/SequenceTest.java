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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.greeter_control.Control;
import org.apache.cxf.greeter_control.ControlService;
import org.apache.cxf.greeter_control.Greeter;
import org.apache.cxf.greeter_control.GreeterService;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.ws.rm.RMConstants;


/**
 * Tests the addition of WS-RM properties to application messages and the
 * exchange of WS-RM protocol messages.
 */
public class SequenceTest extends ClientServerTestBase {

    private static final Logger LOG = Logger.getLogger(SequenceTest.class.getName());
    // private static final String APP_NAMESPACE =
    // "http://celtix.objectweb.org/greeter_control";
    // private static final String GREETMEONEWAY_ACTION = APP_NAMESPACE +
    // "/types/Greeter/greetMeOneWay";
    // private static final String GREETME_ACTION = APP_NAMESPACE +
    // "/types/Greeter/greetMe";
    // private static final String GREETME_RESPONSE_ACTION = GREETME_ACTION +
    // "Response";
    private static final String GREETMEONEWAY_ACTION = null;

    private Bus controlBus;
    private Control control;
    private Bus greeterBus;
    private Greeter greeter;
    private OutMessageRecorder outRecorder;
    private InMessageRecorder inRecorder;

    private boolean doTestOnewayAnonymousAcks = true;
    private boolean doTestOnewayDeferredAnonymousAcks = true;
    private boolean doTestOnewayDeferredNonAnonymousAcks = true;
    private boolean doTestOnewayAnonymousAcksSequenceLength1 = true;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SequenceTest.class);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(SequenceTest.class);
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

    public void setUp() throws Exception {
        SpringBusFactory bf = new SpringBusFactory();
        controlBus = bf.createBus();
        bf.setDefaultBus(controlBus);
        LOG.fine("Initialised control bus.");
        controlBus = new SpringBusFactory().getDefaultBus();

        ControlService service = new ControlService();
        LOG.fine("Created ControlService.");
        control = service.getControlPort();
        LOG.fine("Created Control.");
    }
    
    public void tearDown() {
        if (null != greeter) {
            assertTrue("Failed to stop greeter.", control.stopGreeter());            
            greeterBus.shutdown(true);
            LOG.fine("Shutdown greeter bus.");
            greeterBus = null;
        }
        if (null != control) {               
            controlBus.shutdown(true);
            LOG.fine("Shutdown control bus.");
        }
    }

    // --- tests ---
    
    public void testOnewayAnonymousAcks() throws Exception {
        if (!doTestOnewayAnonymousAcks) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/anonymous.xml");

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");
        greeter.greetMeOneWay("thrice");

        // three application messages plus createSequence

        awaitMessages(4, 4);
        
        MessageFlow mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());

        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {RMConstants.getCreateSequenceAction(), GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, GREETMEONEWAY_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse plus 3 partial responses
        
        mf.verifyMessages(4, false);
        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction(), null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null}, false);
        mf.verifyAcknowledgements(new boolean[] {false, true, true, true}, false);
    }
    
    public void testOnewayDeferredAnonymousAcks() throws Exception {
        if (!doTestOnewayDeferredAnonymousAcks) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/anonymous-deferred.xml");

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");

        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ex) {
            // ignore
        }

        greeter.greetMeOneWay("thrice");

        awaitMessages(4, 4);
        MessageFlow mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());
                
        // three application messages plus createSequence
        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {RMConstants.getCreateSequenceAction(), GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, GREETMEONEWAY_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse message plus 3 partial responses, only the
        // last one should include a sequence acknowledgment

        mf.verifyMessages(4, false);
        expectedActions = 
            new String[] {RMConstants.getCreateSequenceResponseAction(), null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null}, false);
        mf.verifyAcknowledgements(new boolean[] {false, false, false, true}, false);
    }
    
    public void testOnewayDeferredNonAnonymousAcks() throws Exception {
        if (!doTestOnewayDeferredNonAnonymousAcks) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/nonanonymous-deferred.xml");

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");

        // CreateSequence plus two greetMeOneWay requests

        awaitMessages(3, 4);
        MessageFlow mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());
        
        mf.verifyMessages(3, true);
        String[] expectedActions = new String[] {RMConstants.getCreateSequenceAction(), 
                                                 GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2"}, true);

        // CreateSequenceResponse plus two partial responses, no
        // acknowledgments included

        mf.verifyMessages(4, false);
        expectedActions = new String[] {null, RMConstants.getCreateSequenceResponseAction(), 
                                        null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[4], false);
        mf.verifyAcknowledgements(new boolean[4], false);

        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ex) {
            // ignore
        }

        // a standalone acknowledgement should have been sent from the server
        // side by now
        
        awaitMessages(3, 5);
        mf.reset(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());

        mf.verifyMessages(0, true);
        mf.verifyMessages(1, false);

    }
    
    public void testOnewayAnonymousAcksSequenceLength1() throws Exception {
        if (!doTestOnewayAnonymousAcksSequenceLength1) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/anonymous-seqlength1.xml");

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");

        // two application messages plus two createSequence plus two
        // terminateSequence

        awaitMessages(6, 6);
        
        MessageFlow mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());
        
        mf.verifyMessages(6, true);
        String[] expectedActions = new String[] {RMConstants.getCreateSequenceAction(), 
                                                 GREETMEONEWAY_ACTION,
                                                 RMConstants.getTerminateSequenceAction(),
                                                 RMConstants.getCreateSequenceAction(), 
                                                 GREETMEONEWAY_ACTION,
                                                 RMConstants.getTerminateSequenceAction()};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", null, null, "1", null}, true);
        mf.verifyLastMessage(new boolean[] {false, true, false, false, true, false}, true);

        // createSequenceResponse message plus partial responses to
        // greetMeOneWay and terminateSequence ||: 2

        mf.verifyMessages(6, false);

        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction(), 
                                        null, null,
                                        RMConstants.getCreateSequenceResponseAction(), 
                                        null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null, null, null}, false);
        mf.verifyLastMessage(new boolean[] {false, false, false, false, false, false}, false);
        mf.verifyAcknowledgements(new boolean[] {false, true, false, false, true, false}, false);
    }


    // --- test utilities ---

    private void setupGreeter(String cfgResource) {
        
        SpringBusFactory bf = new SpringBusFactory();
        greeterBus = bf.createBus(cfgResource);
        bf.setDefaultBus(greeterBus);
        LOG.fine("Initialised greeter bus.");

        outRecorder = new OutMessageRecorder();
        greeterBus.getOutInterceptors().add(new JaxwsInterceptorRemover());
        greeterBus.getOutInterceptors().add(outRecorder);
        inRecorder = new InMessageRecorder();
        greeterBus.getInInterceptors().add(inRecorder);

        assertTrue("Failed to start greeter", control.startGreeter(cfgResource));
        
        GreeterService service = new GreeterService();
        greeter = service.getGreeterPort();
        LOG.fine("Created greeter client.");
    }
    
    private void awaitMessages(int nExpectedOut, int nExpectedIn) {
        int i = 0;
        int nOut = 0;
        int nIn = 0;
        while (i < 20) {                
            synchronized (outRecorder) {
                nOut = outRecorder.getOutboundMessages().size();
            }
            synchronized (inRecorder) {
                nIn = inRecorder.getInboundMessages().size();
            }
            if (nIn >= nExpectedIn && nOut >= nExpectedOut) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
        assertEquals("Did not receive expected number of inbound messages", nExpectedIn, nIn);
        assertEquals("Did not send expected number of outbound messages", nExpectedOut, nOut);        
    }
}
