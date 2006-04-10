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
    private boolean no;
    
    private boolean doTestOnewayAnonymousAcks = yes;
    private boolean doTestOnewayDeferredAnonymousAcks = yes;    
    private boolean doTestOnewayAnonymousAcksSequenceLength1 = yes; 
    private boolean doTestOnewayAnonymousAcksSupressed = yes;
    private boolean doTestTwowayNonAnonymous = yes;
    private boolean doTestTwowayNonAnonymousMaximumSequenceLength2 = yes;    
    private boolean doTestTwowayNonAnonymousNoOffer = no;
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

    // --- tests ---

    public void testOnewayAnonymousAcks() throws Exception {
        if (!doTestOnewayAnonymousAcks) {
            return;
        }
        setupEndpoints("");

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");
        greeter.greetMeOneWay("thrice");

        // three application messages plus createSequence

        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, GREETMEONEWAY_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse plus three partial responses
        mf.verifyMessages(4, false);
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null}, false);
        mf.verifyAcknowledgements(new boolean[] {false, true, true, true}, false);
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

        // createSequenceResponse message plus three partial responses, only the
        // last one should include a sequence acknowledgment

        mf.verifyMessages(4, false);
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null}, false);
        mf.verifyAcknowledgements(new boolean[] {false, false, false, true}, false);
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

        mf.verifyInboundMessages(6);
       
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null,
                                        Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null, null, null}, false);
        mf.verifyLastMessage(new boolean[] {false, false, false, false, false, false}, false);
        mf.verifyAcknowledgements(new boolean[] {false, true, false, false, true, false}, false);
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

        // createSequenceResponse plus three partial responses, none of which
        // contain an acknowledgment

        mf.verifyMessages(4, false);
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null}, false);
        mf.verifyAcknowledgements(new boolean[] {false, false, false, false}, false);
        
        mf.getOutboundMessages().clear();
        mf.getInboundContexts().clear();

        // allow resends to kick in
        Thread.sleep(10 * 1000);

        // between 1 and 3 resends (the actual number depends on whether
        // the ACK in response to the AckRequested header in the first resend
        // is processed before the other (async) resends occur
        
        int nOutbound = mf.getOutboundMessages().size();
        assertTrue("unexpected number of resends: " + nOutbound,
                   nOutbound >= 1 && nOutbound <= 3);
        mf.verifyAckRequestedOutbound();
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
        // SequenceAcknowledgement messages
        
        mf.verifyInboundMessages(11);
        
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, 
                                        null, GREETME_RESPONSE_ACTION, 
                                        null, GREETME_RESPONSE_ACTION, 
                                        null,
                                        null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION,
                                        null, GREETME_RESPONSE_ACTION};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(
            new String[] {null, null, null, "1", null, "2", null, null, null, null, "1"}, false);
        boolean[] expected = new boolean[11];
        expected[5] = true;
        mf.verifyLastMessage(expected, false);
        expected[3] = true;
        expected[10] = true;
        mf.verifyAcknowledgements(expected, false);
    }

    // enable after server transport api has changed to support
    // requests being send from a server side

    public void testTwowayNonAnonymousNoOffer() throws Exception {
        if (!doTestTwowayNonAnonymousNoOffer) {
            return;
        }
        setupEndpoints("twoway-no-offer");

        greeter.greetMe("one");
        greeter.greetMe("two");

        // CreateSequence and two greetMe messages
        // plus a CreateSequenceResponse
        // TODO there should be partial responses to the decoupled responses!

        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETME_ACTION,
                                                 Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, GREETME_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", null, "2"}, true);
        mf.verifyLastMessage(new boolean[] {false, false, false, false}, true);
        mf.verifyAcknowledgements(new boolean[] {false, false, false, true}, true);

        // createSequenceResponse plus 2 greetMeResponse messages plus
        // one partial response for each of the four messages plus
        // CreateSequence request

        mf.verifyMessages(7, false);
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null,
                                        Names.WSRM_CREATE_SEQUENCE_ACTION, GREETME_RESPONSE_ACTION, null,
                                        GREETME_RESPONSE_ACTION};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null, null, "1", null, "2"}, false);
        mf.verifyLastMessage(new boolean[7], false);
        mf.verifyAcknowledgements(new boolean[] {false, false, false, false, true, false, true, false}, 
                                  false);
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
        
        // wait for resends to occur
        mf.clear();
        Thread.sleep(20 * 1000);
        
        mf.verifyMessages(4, true);
        expectedActions = new String[] {GREETME_ACTION, GREETME_ACTION, 
                                        GREETME_ACTION, GREETME_ACTION};
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
        List<Handler> handlerChain = abi.getPostProtocolSystemHandlers();
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
        assertTrue("Could not find LogicalMessageContextRecorder in pre logical handler chain", found);
        currentConfiguration = configuration;
        
        mf = new MessageFlow(outboundMessages, inboundContexts);
        
    }

}
