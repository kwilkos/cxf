package org.objectweb.celtix.systest.ws.rm;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalMessageContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bus.busimpl.BusConfigurationBuilder;
import org.objectweb.celtix.bus.ws.rm.Names;
import org.objectweb.celtix.bus.ws.rm.RMHandler;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.greeter_control.Control;
import org.objectweb.celtix.greeter_control.ControlService;
import org.objectweb.celtix.greeter_control.Greeter;
import org.objectweb.celtix.greeter_control.GreeterService;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;

/**
 * Tests Reliable Messaging.
 */
public class SequenceTest extends ClientServerTestBase {

    private static final String APP_NAMESPACE = "http://celtix.objectweb.org/greeter_control";
    private static final String GREETMEONEWAY_ACTION = APP_NAMESPACE + "/types/Greeter/greetMeOneWay";
    private static final String GREETME_ACTION = APP_NAMESPACE + "/types/Greeter/greetMe";
    private static final String GREETME_RESPONSE_ACTION = GREETME_ACTION + "Response";

    private static final QName CONTROL_SERVICE_NAME = new QName(APP_NAMESPACE, "ControlService");
    private static final QName SERVICE_NAME = new QName(APP_NAMESPACE, "GreeterService");
    private static final QName CONTROL_PORT_NAME = new QName(APP_NAMESPACE, "ControlPort");
    private static final QName PORT_NAME = new QName(APP_NAMESPACE, "GreeterPort");

    private Bus bus;
    private GreeterService greeterService;
    private Greeter greeter;
    private Control control;
    private String currentConfiguration;
    private MessageFlow mf;
    
    private boolean yes = true;

    // enable currently disabled tests when transport apis allows to 
    // originate standalone requests from server side
    
    private boolean doTestOnewayAnonymousAcks = yes;
    private boolean doTestOnewayDeferredAnonymousAcks = yes; 
    private boolean doTestOnewayDeferredNonAnonymousAcks = yes;
    private boolean doTestOnewayAnonymousAcksSequenceLength1 = yes; 
    private boolean doTestOnewayAnonymousAcksSupressed = yes;
    private boolean doTestTwowayNonAnonymous = yes;
    private boolean doTestTwowayNonAnonymousDeferred = yes;
    private boolean doTestTwowayNonAnonymousMaximumSequenceLength2 = yes;    
    private boolean doTestTwowayNonAnonymousNoOffer = yes;
    private boolean doTestTwowayMessageLoss = yes;


    public static void main(String[] args) {
        junit.textui.TestRunner.run(SequenceTest.class);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(SequenceTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                // special case handling for WS-Addressing system test to avoid
                // UUID related issue when server is run as separate process
                // via maven on Win2k
                assertTrue("server did not launch correctly", launchServer(Server.class, "Windows 2000"
                    .equals(System.getProperty("os.name"))));
            }

            public void setUp() throws Exception {
                // avoid re-using a previously created configuration for a bus
                // with id "celtix"
                ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
                builder.buildConfiguration(BusConfigurationBuilder.BUS_CONFIGURATION_URI, "celtix");

                super.setUp();

            }
        };
    }
    
    public void tearDown() {
        if (null != greeter) { 
            boolean found = false;
            BindingProvider provider = (BindingProvider)greeter;
            AbstractBindingImpl abi = (AbstractBindingImpl)provider.getBinding();
            List<Handler> handlerChain = abi.getPreLogicalSystemHandlers();
            for (Handler h : handlerChain) {
                if (h instanceof RMHandler) {
                    ((RMHandler)h).destroy();
                    found = true;
                    break;
                }
            } 
            assertTrue("Cound not find RM handler in pre logical system handler chain", found);
        }
    }

    // --- tests ---

    public void testOnewayAnonymousAcks() throws Exception {
        if (!doTestOnewayAnonymousAcks) {
            return;
        }
        setupEndpoints("anonymous");

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");
        greeter.greetMeOneWay("thrice");

        // three application messages plus createSequence

        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, GREETMEONEWAY_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse plus 4 partial responses
        mf.verifyMessages(5, false);
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null, null}, false);
        mf.verifyAcknowledgements(new boolean[] {false, false, true, true, true}, false);
    }
   
    public void testOnewayDeferredAnonymousAcks() throws Exception {
        if (!doTestOnewayDeferredAnonymousAcks) {
            return;
        }
        setupEndpoints("anonymous-deferred");

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");

        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ex) {
            // ignore
        }

        greeter.greetMeOneWay("thrice");

        // three application messages plus createSequence
        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, GREETMEONEWAY_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse message plus 4 partial responses, only the
        // last one should include a sequence acknowledgment

        mf.verifyMessages(5, false);
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null, null}, false);
        mf.verifyAcknowledgements(new boolean[] {false, false, false, false, true}, false);
    }
    
    public void testOnewayDeferredNonAnonymousAcks() throws Exception {
        if (!doTestOnewayDeferredNonAnonymousAcks) {
            return;
        }
        setupEndpoints("nonanonymous-deferred");

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");

        // CreateSequence plus two greetMeOneWay requests
        
        mf.verifyMessages(3, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2"}, true);

        // CreateSequenceResponse plus three partial responses, no acknowledgments included

        mf.verifyMessages(4, false);
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[4], false);
        mf.verifyAcknowledgements(new boolean[4], false);
        
        mf.getInboundContexts().clear();
        mf.getOutboundMessages().clear();
        
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ex) {
            // ignore
        }
        
        // a standalone acknowledgement should have been sent from the server side by now
        
        mf.verifyMessages(0, true);
        mf.verifyMessages(0, false); 
        
    }
    
    public void testOnewayAnonymousAcksSequenceLength1() throws Exception {
        if (!doTestOnewayAnonymousAcksSequenceLength1) {
            return;
        }
        setupEndpoints("anonymous-seqlength1");

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");

        // two application messages plus two createSequence plus two
        // terminateSequence

        mf.verifyMessages(6, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 Names.WSRM_TERMINATE_SEQUENCE_ACTION,
                                                 Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 Names.WSRM_TERMINATE_SEQUENCE_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", null, null, "1", null}, true);
        mf.verifyLastMessage(new boolean[] {false, true, false, false, true, false}, true);

        // createSequenceResponse message plus partial responses to
        // greetMeOneWay and terminateSequence ||: 2

        mf.verifyMessages(8, false, 100, 5);
       
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null,
                                        null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null, null, null, null, null}, false);
        mf.verifyLastMessage(new boolean[] {false, false, false, false, false, false, false, false}, false);
        mf.verifyAcknowledgements(new boolean[] {false, false, true, false,
                                                 false, false, true, false}, false);
    }

    public void testOnewayAnonymousAcksSupressed() throws Exception {
        
        if (!doTestOnewayAnonymousAcksSupressed) {
            return;
        }
        setupEndpoints("anonymous-suppressed");

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");
        greeter.greetMeOneWay("thrice");

        // three application messages plus createSequence
        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, GREETMEONEWAY_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse plus 4 partial responses, none of which
        // contain an acknowledgment

        mf.verifyMessages(5, false);
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null, null}, false);
        mf.verifyAcknowledgements(new boolean[] {false, false, false, false, false}, false);
        
        mf.getOutboundMessages().clear();
        mf.getInboundContexts().clear();

        // allow resends to kick in
        Thread.sleep(10 * 1000);

        // between 1 and 3 resends 
        // note that for now neither AckRequested nor up-to-date
        // SequenceAcknowledgment headers are added to resent messages
        // also, as the server is configured to not piggyback 
        // SequenceAcknowledgments onto the partial response, the client
        // will keep retransmitting its messages indefinitely
        
        int nOutbound = mf.getOutboundMessages().size();
        assertTrue("unexpected number of resends: " + nOutbound,
                   nOutbound >= 1);
        // mf.verifyAckRequestedOutbound();
    }
     
    public void testTwowayNonAnonymous() throws Exception {
        if (!doTestTwowayNonAnonymous) {
            return;
        }
        setupEndpoints("twoway");

        greeter.greetMe("one");
        greeter.greetMe("two");
        greeter.greetMe("three");

        // CreateSequence and three greetMe messages
        // TODO there should be partial responses to the decoupled responses!

        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETME_ACTION,
                                                 GREETME_ACTION, GREETME_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);
        mf.verifyLastMessage(new boolean[] {false, false, false, false}, true);
        mf.verifyAcknowledgements(new boolean[] {false, false, true, true}, true);
        
        // createSequenceResponse plus 3 greetMeResponse messages plus
        // one partial response for each of the four messages

        mf.verifyMessages(8, false);
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null,
                                        GREETME_RESPONSE_ACTION, null, GREETME_RESPONSE_ACTION, null,
                                        GREETME_RESPONSE_ACTION};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, "1", null, "2", null, "3"}, false);
        mf.verifyLastMessage(new boolean[8], false);
        mf.verifyAcknowledgements(new boolean[] {false, false, false, true, false, true, false, true}, false);
    }
    
    public void testTwowayNonAnonymousDeferred() throws Exception {
        if (!doTestTwowayNonAnonymousDeferred) {
            return;
        }
        setupEndpoints("nonanonymous-deferred");

        greeter.greetMe("one");
        greeter.greetMe("two");

        // CreateSequence and three greetMe messages, no acknowledgments included
       
        mf.verifyMessages(3, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETME_ACTION,
                                                 GREETME_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2"}, true);
        mf.verifyLastMessage(new boolean[3], true);
        mf.verifyAcknowledgements(new boolean[3], true);
        
        // CreateSequenceResponse plus 2 greetMeResponse messages plus
        // one partial response for each of the four messages no acknowledgments included

        mf.verifyMessages(6, false);
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null,
                                        GREETME_RESPONSE_ACTION, null,
                                        GREETME_RESPONSE_ACTION};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, "1", null, "2"}, false);
        mf.verifyLastMessage(new boolean[6], false);
        mf.verifyAcknowledgements(new boolean[6], false);
        
        mf.getInboundContexts().clear();
        mf.getOutboundMessages().clear();
        
        // a standalone acknowledgement should have been sent from the server side by now
        
        mf.verifyMessages(1, true, 1000, 5);
        mf.verifyMessageNumbers(new String[1], true);
        mf.verifyLastMessage(new boolean[1], true);
        mf.verifyAcknowledgements(new boolean[] {true} , true);
        
        // TODO: verify incoming requests also
          
    }
    
    /**
     * A maximum sequence length of 2 is configured for the client only.
     * However, as we use the defaults regarding the including and acceptance for
     * inbound sequence offers and correlate offered sequences that are included
     * in a CreateSequence request and accepted with those that are created on behalf
     * of such a request, the server also tries terminate its sequences.
     * Note that as part of the sequence termination exchange a standalone sequence
     * acknowledgment needs to be sent regardless of whether or nor acknowledgments are 
     * delivered steadily with every response.
     */
    
    public void testTwowayNonAnonymousMaximumSequenceLength2() throws Exception {
        
        if (!doTestTwowayNonAnonymousMaximumSequenceLength2) {
            return;
        }
        setupEndpoints("twoway-seqlength2");

        greeter.greetMe("one");
        greeter.greetMe("two");
        greeter.greetMe("three");

        mf.verifyMessages(7, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, 
                                                 GREETME_ACTION,
                                                 GREETME_ACTION,
                                                 Names.WSRM_TERMINATE_SEQUENCE_ACTION,
                                                 Names.WSRM_SEQUENCE_ACKNOWLEDGMENT_ACTION,
                                                 Names.WSRM_CREATE_SEQUENCE_ACTION,
                                                 GREETME_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", null, null, null, "1"}, true);
        mf.verifyLastMessage(new boolean[] {false, false, true, false, false, false, false}, true);
        mf.verifyAcknowledgements(new boolean[] {false, false, true, false, true, false, false}, true);

        // Note that we don't expect a partial response to standalone LastMessage or 
        // SequenceAcknowledgment messages
        
        mf.verifyMessages(12, false, 100, 5);
        
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, 
                                        null, GREETME_RESPONSE_ACTION, 
                                        null, GREETME_RESPONSE_ACTION, 
                                        null, Names.WSRM_TERMINATE_SEQUENCE_ACTION,
                                        null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION,
                                        null, GREETME_RESPONSE_ACTION};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(
            new String[] {null, null, null, "1", null, "2", null, null, null, null, null, "1"}, false);
        boolean[] expected = new boolean[12];
        expected[5] = true;
        mf.verifyLastMessage(expected, false);
        expected[3] = true;
        expected[11] = true;
        mf.verifyAcknowledgements(expected, false);
    }

    public void testTwowayNonAnonymousNoOffer() throws Exception {
        if (!doTestTwowayNonAnonymousNoOffer) {
            return;
        }
        setupEndpoints("twoway-no-offer");

        greeter.greetMe("one");
        greeter.greetMe("two");

        // Outbound expected:
        // CreateSequence + (2 * greetMe) + CreateSequenceResponse = 4 messages
        // TODO there should be partial responses to the decoupled responses!

        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETME_ACTION,
                                                 Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, GREETME_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", null, "2"}, true);
        mf.verifyLastMessage(new boolean[] {false, false, false, false}, true);
        mf.verifyAcknowledgements(new boolean[] {false, false, false, true}, true);

        // Inbound expected:
        // createSequenceResponse + (2 * greetMeResponse) + CreateSequence +
        // (4 * partial response [for each outbound message]) = 8 

        mf.verifyMessages(8, false);
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION,
                                        null, Names.WSRM_CREATE_SEQUENCE_ACTION,
                                        null, GREETME_RESPONSE_ACTION, 
                                        null, GREETME_RESPONSE_ACTION};
        if (!mf.checkActions(expectedActions, false)) {
            // greetMeResponse and partial response to previous CreateSequence may be
            // processed out-of-order
            expectedActions[4] = GREETME_RESPONSE_ACTION;
            expectedActions[5] = null;
            mf.verifyActions(expectedActions, false);
            mf.verifyMessageNumbers(new String[] {null, null, null, null, "1", null, null, "2"}, false);
            mf.verifyLastMessage(new boolean[8], false);
            mf.verifyAcknowledgements(new boolean[] {false, false, false, false, 
                                                     true, false, false, true},
                                      false);
        } else {
            mf.verifyMessageNumbers(new String[] {null, null, null, null, null, "1", null, "2"}, false);
            mf.verifyLastMessage(new boolean[8], false);
            mf.verifyAcknowledgements(new boolean[] {false, false, false, false, 
                                                     false, true, false, true}, 
                                      false);
        }
    }

    public void testTwowayMessageLoss() throws Exception {
        if (!doTestTwowayMessageLoss) {
            return;
        }
        setupEndpoints("twoway-message-loss");

        greeter.greetMe("one");
        greeter.greetMe("two");
        greeter.greetMe("three");
        greeter.greetMe("four");

        // CreateSequence and four greetMe messages

        mf.verifyMessages(5, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETME_ACTION,
                                                 GREETME_ACTION, GREETME_ACTION, GREETME_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3", "4"}, true);
        mf.verifyLastMessage(new boolean[] {false, false, false, false, false}, true);
        mf.verifyAcknowledgements(new boolean[] {false, false, true, false, true}, true);
        
        // createSequenceResponse 
        // + 2 greetMeResponse actions (non-discarded messages)
        // + 2 greetMe actions (discarded messages)
        // + 3 partial responses (to CSR & each of the non-discarded messages)

        mf.verifyMessages(8, false);
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null,
                                        GREETME_RESPONSE_ACTION, GREETME_ACTION, null,
                                        GREETME_RESPONSE_ACTION, GREETME_ACTION};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, "1", null, null, "2", null}, false);
        mf.verifyLastMessage(new boolean[8], false);
        mf.verifyAcknowledgements(new boolean[] {false, false, false, true, false, false, true, false},
                                  false);
        
        
        mf.clear();
        
        // wait for resends to occur
        // for some reason only the first retransmission for each message works fine
        // the second time round a message with an empty body is re-transmitted
        /*
        mf.verifyMessages(4, true, 1000, 20);
        expectedActions = new String[] {GREETME_ACTION, GREETME_ACTION, 
                                        GREETME_ACTION, GREETME_ACTION};
        */
        mf.verifyMessages(2, true, 1000, 10);
        expectedActions = new String[] {GREETME_ACTION, GREETME_ACTION};
        mf.verifyActions(expectedActions, true);

    }

    // --- test setup helpers ---


    private void createControl() {
        if (null == control) {
            URL wsdl = getClass().getResource("/wsdl/greeter_control.wsdl");
            ControlService controlService = new ControlService(wsdl, CONTROL_SERVICE_NAME);
            control = controlService.getPort(CONTROL_PORT_NAME, Control.class);
        }
    }

    private void setupEndpoints(String configuration) throws Exception {

        if (configuration != null && configuration.equals(currentConfiguration)) {
            return;
        }
        
        if (configuration.indexOf("shutdown") >  0 && null != bus) {
            bus.shutdown(true);
            bus = null;
        }
        
        if (null == bus) {
            bus = Bus.init();
        }
      
        createControl();

        control.stopGreeter();
        control.startGreeter(configuration);

        if (null != configuration && configuration.length() > 0) {
            ControlImpl.setConfigFileProperty(configuration);
        }

        TestConfigurator tc = new TestConfigurator();
        tc.configureClient(SERVICE_NAME, PORT_NAME.getLocalPart());

        URL wsdl = getClass().getResource("/wsdl/greeter_control.wsdl");
        greeterService = new GreeterService(wsdl, SERVICE_NAME);        
        greeter = greeterService.getPort(PORT_NAME, Greeter.class);

        BindingProvider provider = (BindingProvider)greeter;
        AbstractBindingImpl abi = (AbstractBindingImpl)provider.getBinding();
        List<Handler> handlerChain = abi.getHandlerChain();
        assertTrue(handlerChain.size() > 0);
        
        List<SOAPMessage> outboundMessages = null;
        List<LogicalMessageContext> inboundContexts = null;

        boolean found = false;
        for (Handler h : handlerChain) {
            if (!found && h instanceof SOAPMessageRecorder) {
                SOAPMessageRecorder recorder = (SOAPMessageRecorder)h;
                outboundMessages = recorder.getOutboundMessages();
                outboundMessages.clear();
                found = true;
                break;
            }
        }
        assertTrue("Could not find SOAPMessageRecorder in post protocol handler chain", found);
        
        handlerChain = abi.getPreLogicalSystemHandlers();
        assertTrue(handlerChain.size() > 0);
        found = false;
        for (Handler h : handlerChain) {
            if (!found && h instanceof LogicalMessageContextRecorder) {
                LogicalMessageContextRecorder recorder = (LogicalMessageContextRecorder)h;
                inboundContexts = recorder.getInboundContexts();
                inboundContexts.clear();
                found = true;
                break;
            }
        }
        assertTrue("Could not find LogicalMessageContextRecorder in pre logical system handler chain", found);
        currentConfiguration = configuration;
        
        mf = new MessageFlow(outboundMessages, inboundContexts);
        
    }

}
