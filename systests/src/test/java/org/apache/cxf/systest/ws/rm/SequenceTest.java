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

import java.math.BigInteger;
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
import org.apache.cxf.ws.rm.RMManager;


/**
 * Tests the addition of WS-RM properties to application messages and the
 * exchange of WS-RM protocol messages.
 */
public class SequenceTest extends ClientServerTestBase {

    private static final Logger LOG = Logger.getLogger(SequenceTest.class.getName());
    // private static final String APP_NAMESPACE ="http://celtix.objectweb.org/greeter_control";
    // private static final String GREETMEONEWAY_ACTION = APP_NAMESPACE +
    //     "/types/Greeter/greetMeOneWay";
    // private static final String GREETME_ACTION = APP_NAMESPACE +
    //     "/types/Greeter/greetMe";
    // private static final String GREETME_RESPONSE_ACTION = GREETME_ACTION +
    //     "Response";
    private static final String GREETMEONEWAY_ACTION = null;
    private static final String GREETME_ACTION = null;
    private static final String GREETME_RESPONSE_ACTION = null;

    private Bus controlBus;
    private Control control;
    private Bus greeterBus;
    private Greeter greeter;
    private OutMessageRecorder outRecorder;
    private InMessageRecorder inRecorder;

    private boolean testAll = true;
    private boolean doTestOnewayAnonymousAcks = testAll;
    private boolean doTestOnewayDeferredAnonymousAcks = testAll;
    private boolean doTestOnewayDeferredNonAnonymousAcks = testAll;
    private boolean doTestOnewayAnonymousAcksSequenceLength1 = testAll;
    private boolean doTestOnewayAnonymousAcksSupressed = testAll;
    private boolean doTestTwowayNonAnonymous = testAll;
    private boolean doTestTwowayNonAnonymousDeferred = testAll;
    private boolean doTestTwowayNonAnonymousMaximumSequenceLength2 = testAll;
    private boolean doTestOnewayMessageLoss = testAll;
    private boolean doTestTwowayMessageLoss = testAll;
    private boolean doTestTwowayNonAnonymousNoOffer = testAll;

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
    
    public void tearDown() {
        if (null != greeter) {
            assertTrue("Failed to stop greeter.", control.stopGreeter());                        
            RMManager manager = greeterBus.getExtension(RMManager.class);
            manager.shutdown();
            greeterBus.shutdown(true);
            greeterBus = null;
        }
        if (null != control) {  
            assertTrue("Failed to stop greeter", control.stopGreeter());
            controlBus.shutdown(true);
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

        // CreateSequenceResponse plus three partial responses, no
        // acknowledgments included

        mf.verifyMessages(4, false);
        mf.verifyMessageNumbers(new String[4], false);
        mf.verifyAcknowledgements(new boolean[4], false);
        
        mf.verifyPartialResponses(3);        
        mf.purgePartialResponses();
  
        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction()};
        mf.verifyActionsIgnoringPartialResponses(expectedActions);
        mf.purge();
        
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ex) {
            // ignore
        }

        // a standalone acknowledgement should have been sent from the server
        // side by now
        
        awaitMessages(0, 1);
        mf.reset(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());

        mf.verifyMessages(0, true);
        mf.verifyMessages(1, false);
        mf.verifyAcknowledgements(new boolean[] {true}, false);

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
    
    public void testOnewayAnonymousAcksSupressed() throws Exception {

        if (!doTestOnewayAnonymousAcksSupressed) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/anonymous-suppressed.xml");

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");
        greeter.greetMeOneWay("thrice");

        // three application messages plus createSequence
        
        awaitMessages(4, 4, 2000);
        
        MessageFlow mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());
        
        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {RMConstants.getCreateSequenceAction(), 
                                                 GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, 
                                                 GREETMEONEWAY_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse plus 3 partial responses, none of which
        // contain an acknowledgment

        mf.verifyMessages(4, false);
        mf.verifyPartialResponses(3, new boolean[3]);
        mf.purgePartialResponses();
        
        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction()};
        mf.verifyActions(expectedActions, false);
        
        mf.purge();
        assertEquals(0, outRecorder.getOutboundMessages().size());
        assertEquals(0, inRecorder.getInboundMessages().size());

        // allow resends to kick in
        // await multiple of 3 resends to avoid shutting down server
        // in the course of retransmission - this is harmless but pollutes test output
        
        awaitMessages(3, 0, 5000);
        
    }
    
    public void testTwowayNonAnonymous() throws Exception {
        if (!doTestTwowayNonAnonymous) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/twoway.xml");

        greeter.greetMe("one");
        greeter.greetMe("two");
        greeter.greetMe("three");

        // CreateSequence and three greetMe messages
        // TODO there should be partial responses to the decoupled responses!

        awaitMessages(4, 8);
        
        MessageFlow mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());
        
        
        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {RMConstants.getCreateSequenceAction(), 
                                                 GREETME_ACTION,
                                                 GREETME_ACTION, 
                                                 GREETME_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);
        mf.verifyLastMessage(new boolean[] {false, false, false, false}, true);
        mf.verifyAcknowledgements(new boolean[] {false, false, true, true}, true);

        // createSequenceResponse plus 3 greetMeResponse messages plus
        // one partial response for each of the four messages
        // the first partial response should no include an acknowledgement, the other three should

        mf.verifyMessages(8, false);
        mf.verifyPartialResponses(4, new boolean[4]);

        mf.purgePartialResponses();

        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction(), 
                                        GREETME_RESPONSE_ACTION, 
                                        GREETME_RESPONSE_ACTION, 
                                        GREETME_RESPONSE_ACTION};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, false);
        mf.verifyLastMessage(new boolean[4], false);
        mf.verifyAcknowledgements(new boolean[] {false, true, true, true}, false);
    }

    public void testTwowayNonAnonymousDeferred() throws Exception {
        if (!doTestTwowayNonAnonymousDeferred) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/twoway-deferred.xml");

        greeter.greetMe("one");
        greeter.greetMe("two");

        // CreateSequence and three greetMe messages, no acknowledgments
        // included

        awaitMessages(3, 6);
        MessageFlow mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());
        
        mf.verifyMessages(3, true);
        String[] expectedActions = new String[] {RMConstants.getCreateSequenceAction(), 
                                                 GREETME_ACTION,
                                                 GREETME_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2"}, true);
        mf.verifyLastMessage(new boolean[3], true);
        mf.verifyAcknowledgements(new boolean[3], true);

        // CreateSequenceResponse plus 2 greetMeResponse messages plus
        // one partial response for each of the three messages no acknowledgments
        // included

        mf.verifyMessages(6, false);
        mf.verifyLastMessage(new boolean[6], false);
        mf.verifyAcknowledgements(new boolean[6], false);
        
        mf.verifyPartialResponses(3);
        mf.purgePartialResponses();
        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction(), 
                                        GREETME_RESPONSE_ACTION, 
                                        GREETME_RESPONSE_ACTION};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, "1", "2"}, false);
        mf.purge();
        

        // one standalone acknowledgement should have been sent from the client and one
        // should have been received from the server
   
        awaitMessages(1, 0);
        mf.reset(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());
        
        mf.verifyMessageNumbers(new String[1], true);
        mf.verifyLastMessage(new boolean[1], true);
        mf.verifyAcknowledgements(new boolean[] {true}, true);

    }
    
    /**
     * A maximum sequence length of 2 is configured for the client only (server allows 10).
     * However, as we use the defaults regarding the including and acceptance
     * for inbound sequence offers and correlate offered sequences that are
     * included in a CreateSequence request and accepted with those that are
     * created on behalf of such a request, the server also tries terminate its
     * sequences. Note that as part of the sequence termination exchange a
     * standalone sequence acknowledgment needs to be sent regardless of whether
     * or nor acknowledgments are delivered steadily with every response.
     */

    public void testTwowayNonAnonymousMaximumSequenceLength2() throws Exception {

        if (!doTestTwowayNonAnonymousMaximumSequenceLength2) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/twoway-seqlength2.xml");
        
        RMManager manager = greeterBus.getExtension(RMManager.class);
        assertEquals("Unexpected maximum sequence length.", BigInteger.TEN, 
            manager.getSourcePolicy().getSequenceTerminationPolicy().getMaxLength());
        manager.getSourcePolicy().getSequenceTerminationPolicy().setMaxLength(
            new BigInteger("2"));
        
        greeter.greetMe("one");
        greeter.greetMe("two");
        greeter.greetMe("three");

        awaitMessages(7, 13, 5000);
        MessageFlow mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());
        
        mf.verifyMessages(7, true);
        String[] expectedActions = new String[] {RMConstants.getCreateSequenceAction(),
                                                 GREETME_ACTION,
                                                 GREETME_ACTION, 
                                                 RMConstants.getTerminateSequenceAction(),
                                                 RMConstants.getSequenceAckAction(),
                                                 RMConstants.getCreateSequenceAction(),
                                                 GREETME_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", null, null, null, "1"}, true);
        mf.verifyLastMessage(new boolean[] {false, false, true, false, false, false, false}, true);
        mf.verifyAcknowledgements(new boolean[] {false, false, true, false, true, false, false}, true);

        // 7 partial responses plus 2 full responses to CreateSequence requests
        // plus 3 full responses to greetMe requests plus server originiated
        // TerminateSequence request

        mf.verifyMessages(13, false);

        mf.verifyPartialResponses(7);

        mf.purgePartialResponses();

        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction(),
                                        GREETME_RESPONSE_ACTION,
                                        GREETME_RESPONSE_ACTION, 
                                        RMConstants.getTerminateSequenceAction(),
                                        RMConstants.getCreateSequenceResponseAction(), 
                                        GREETME_RESPONSE_ACTION};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", null, null, "1"}, false);
        boolean[] expected = new boolean[6];
        expected[2] = true;
        mf.verifyLastMessage(expected, false);
        expected[1] = true;
        expected[5] = true;
        mf.verifyAcknowledgements(expected, false);
    }
    
    public void testOnewayMessageLoss() throws Exception {
        if (!doTestOnewayMessageLoss) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/oneway-message-loss.xml");
        
        greeterBus.getOutInterceptors().add(new MessageLossSimulator());
        RMManager manager = greeterBus.getExtension(RMManager.class);
        manager.getRMAssertion().getBaseRetransmissionInterval().setMilliseconds(new BigInteger("2000"));
        
        greeter.greetMeOneWay("one");
        greeter.greetMeOneWay("two");
        greeter.greetMeOneWay("three");
        greeter.greetMeOneWay("four");
        
        awaitMessages(7, 5, 10000);
        
        MessageFlow mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());

        // Expected outbound:
        // CreateSequence 
        // + 4 greetMe messages
        // + at least 2 resends (message may be resent multiple times depending
        // on the timing of the ACKs)
       
        String[] expectedActions = new String[7];
        expectedActions[0] = RMConstants.getCreateSequenceAction();        
        for (int i = 1; i < expectedActions.length; i++) {
            expectedActions[i] = GREETMEONEWAY_ACTION;
        }
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3", "4", "2", "4"}, true, false);
        mf.verifyLastMessage(new boolean[7], true);
        mf.verifyAcknowledgements(new boolean[7], true);
 
        // Expected inbound:
        // createSequenceResponse
        // + 2 partial responses to successfully transmitted messages
        // + 2 partial responses to resent messages
        
        mf.verifyMessages(5, false);
        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction(),
                                        null, null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null, null}, false);
        mf.verifyAcknowledgements(new boolean[] {false, true, true, true, true}, false);
  
    }
    
    public void testTwowayMessageLoss() throws Exception {
        if (!doTestTwowayMessageLoss) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/twoway-message-loss.xml");
        
        greeterBus.getOutInterceptors().add(new MessageLossSimulator());
        RMManager manager = greeterBus.getExtension(RMManager.class);
        manager.getRMAssertion().getBaseRetransmissionInterval().setMilliseconds(new BigInteger("2000"));

        greeter.greetMe("one");
        greeter.greetMe("two");
        greeter.greetMe("three");
        greeter.greetMe("four");
        
        awaitMessages(7, 10, 10000);
        
        MessageFlow mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());

        // Expected outbound:
        // CreateSequence 
        // + 4 greetMe messages
        // + 2 resends
       
        String[] expectedActions = new String[7];
        expectedActions[0] = RMConstants.getCreateSequenceAction();        
        for (int i = 1; i < expectedActions.length; i++) {
            expectedActions[i] = GREETME_ACTION;
        }
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "2", "3", "4", "4"}, true);
        mf.verifyLastMessage(new boolean[7], true);
        boolean[] expectedAcks = new boolean[7];
        for (int i = 2; i < expectedAcks.length; i++) {
            expectedAcks[i] = true;
        }
        mf.verifyAcknowledgements(expectedAcks , true);
 
        // Expected inbound:
        // createSequenceResponse 
        // + 4 greetMeResponse actions (to original or resent) 
        // + 5 partial responses (to CSR & each of the initial greetMe messages)
        // + at least 2 further partial response (for each of the resends)
        
        mf.verifyPartialResponses(5);
        mf.purgePartialResponses();
        
        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction(),
                                        GREETME_RESPONSE_ACTION, GREETME_RESPONSE_ACTION,
                                        GREETME_RESPONSE_ACTION, GREETME_RESPONSE_ACTION};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3", "4"}, false);
        mf.verifyAcknowledgements(new boolean[] {false, true, true, true, true}, false);
  
    }
    
    public void testTwowayNonAnonymousNoOffer() throws Exception {
        if (!doTestTwowayNonAnonymousNoOffer) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/twoway-no-offer.xml");        
        
        greeter.greetMe("one");
        // greeter.greetMe("two");

        // Outbound expected:
        // CreateSequence + greetMe + CreateSequenceResponse = 3 messages
  
        awaitMessages(3, 6);
        MessageFlow mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());
        
        mf.verifyMessages(3, true);
        String[] expectedActions = new String[] {RMConstants.getCreateSequenceAction(), 
                                                 GREETME_ACTION,
                                                 RMConstants.getCreateSequenceResponseAction()};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", null}, true);
        mf.verifyLastMessage(new boolean[] {false, false, false}, true);
        mf.verifyAcknowledgements(new boolean[] {false, false, false}, true);

        mf.verifyPartialResponses(3, new boolean[3]);
        mf.purgePartialResponses();

        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction(),
                                        RMConstants.getCreateSequenceAction(), 
                                        GREETME_RESPONSE_ACTION};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, "1"}, false);
        mf.verifyAcknowledgements(new boolean[] {false, false, false}, false);
    }

    // --- test utilities ---

    private void setupGreeter(String cfgResource) {
        
        SpringBusFactory bf = new SpringBusFactory();
        
        controlBus = bf.createBus();
        bf.setDefaultBus(controlBus);
        controlBus = new SpringBusFactory().getDefaultBus();

        ControlService cs = new ControlService();
        control = cs.getControlPort();
        
        greeterBus = bf.createBus(cfgResource);
        bf.setDefaultBus(greeterBus);
        LOG.fine("Initialised greeter bus with configuration: " + cfgResource);

        outRecorder = new OutMessageRecorder();
        greeterBus.getOutInterceptors().add(new JaxwsInterceptorRemover());
        greeterBus.getOutInterceptors().add(outRecorder);
        inRecorder = new InMessageRecorder();
        greeterBus.getInInterceptors().add(inRecorder);

        assertTrue("Failed to start greeter", control.startGreeter(cfgResource));
        
        GreeterService gs = new GreeterService();
        greeter = gs.getGreeterPort();
        LOG.fine("Created greeter client.");
    }
    
    private void awaitMessages(int nExpectedOut, int nExpectedIn) {
        awaitMessages(nExpectedOut, nExpectedIn, 10000);
    }
    
    private void awaitMessages(int nExpectedOut, int nExpectedIn, int timeout) {
        int waited = 0;
        int nOut = 0;
        int nIn = 0;
        while (waited <= timeout) {                
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
            waited += 100;
        }
        assertEquals("Did not receive expected number of inbound messages", nExpectedIn, nIn);
        assertEquals("Did not send expected number of outbound messages", nExpectedOut, nOut);        
    }
}
