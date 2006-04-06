package org.objectweb.celtix.systest.ws.rm;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalMessageContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bus.busimpl.BusConfigurationBuilder;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.rm.Names;
import org.objectweb.celtix.bus.ws.rm.RMContextUtils;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.greeter_control.Control;
import org.objectweb.celtix.greeter_control.ControlService;
import org.objectweb.celtix.greeter_control.Greeter;
import org.objectweb.celtix.greeter_control.GreeterService;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.rm.RMProperties;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceType;

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

    private GreeterService greeterService;
    private Greeter greeter;
    private Control control;
    private String currentConfiguration;
    private List<SOAPMessage> outboundMessages;
    private List<LogicalMessageContext> inboundContexts;
    
    private boolean yes = true;
    private boolean no;
    
    private boolean doTestOnewayAnonymousAcks = yes;
    private boolean doTestOnewayDeferredAnonymousAcks = yes;
    private boolean doTestOnewayAnonymousAcksSupressed = yes;
    private boolean doTestOnewayAnonymousAcksSequenceLength1 = yes;    
    private boolean doTestTwowayNonAnonymous = yes;
    private boolean doTestTwowayNonAnonymousMaximumSequenceLength2 = yes;    
    private boolean doTestTwowayNonAnonymousNoOffer = no;

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

        assertEquals(4, outboundMessages.size());
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, GREETMEONEWAY_ACTION};
        verifyActions(expectedActions, true);
        verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse plus three partial responses
        assertEquals(4, inboundContexts.size());
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null, null};
        verifyActions(expectedActions, false);
        verifyMessageNumbers(new String[] {null, null, null, null}, false);
        assertNull(getAcknowledgment(inboundContexts.get(0)));
        verifyAcknowledgements(new boolean[] {false, true, true, true}, false);
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
        assertEquals(4, outboundMessages.size());
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, GREETMEONEWAY_ACTION};
        verifyActions(expectedActions, true);
        verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse message plus three partial responses, only the
        // last one should include a sequence acknowledgment

        assertEquals(4, inboundContexts.size());
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null, null};
        verifyActions(expectedActions, false);
        verifyMessageNumbers(new String[] {null, null, null, null}, false);
        verifyAcknowledgements(new boolean[] {false, false, false, true}, false);
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

        assertEquals(6, outboundMessages.size());
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 Names.WSRM_TERMINATE_SEQUENCE_ACTION,
                                                 Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 Names.WSRM_TERMINATE_SEQUENCE_ACTION};
        verifyActions(expectedActions, true);
        verifyMessageNumbers(new String[] {null, "1", null, null, "1", null}, true);
        verifyLastMessage(new boolean[] {false, true, false, false, true, false}, true);

        // createSequenceResponse message plus partial responses to
        // greetMeOneWay and terminateSequence ||: 2

        verifyInboundMessages(6);
       
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null,
                                        Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null};
        verifyActions(expectedActions, false);
        verifyMessageNumbers(new String[] {null, null, null, null, null, null}, false);
        verifyLastMessage(new boolean[] {false, false, false, false, false, false}, false);
        verifyAcknowledgements(new boolean[] {false, true, false, false, true, false}, false);
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
        assertEquals(4, outboundMessages.size());
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, GREETMEONEWAY_ACTION};
        verifyActions(expectedActions, true);
        verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse plus three partial responses, none of which
        // contain an acknowledgment

        assertEquals(4, inboundContexts.size());
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null, null};
        verifyActions(expectedActions, false);
        verifyMessageNumbers(new String[] {null, null, null, null}, false);
        verifyAcknowledgements(new boolean[] {false, false, false, false}, false);
        
        outboundMessages.clear();
        inboundContexts.clear();

        // allow resends to kick in
        Thread.sleep(10 * 1000);

        // between 1 and 3 resends (the actual number depends on whether
        // the ACK in response to the AckRequested header in the first resend
        // is processed before the other (async) resends occur
        assertTrue("unexpected number of resends: " + outboundMessages.size(),
                   outboundMessages.size() >= 1 && outboundMessages.size() <= 3);
        verifyAckRequestedOutbound(outboundMessages);
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

        assertEquals(4, outboundMessages.size());
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETME_ACTION,
                                                 GREETME_ACTION, GREETME_ACTION};
        verifyActions(expectedActions, true);
        verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);
        verifyLastMessage(new boolean[] {false, false, false, false}, true);
        verifyAcknowledgements(new boolean[] {false, false, true, true}, true);
        
        // createSequenceResponse plus 3 greetMeResponse messages plus
        // one partial response for each of the four messages

        assertEquals(8, inboundContexts.size());
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null,
                                        GREETME_RESPONSE_ACTION, null, GREETME_RESPONSE_ACTION, null,
                                        GREETME_RESPONSE_ACTION};
        verifyActions(expectedActions, false);
        verifyMessageNumbers(new String[] {null, null, null, "1", null, "2", null, "3"}, false);
        verifyLastMessage(new boolean[8], false);
        verifyAcknowledgements(new boolean[] {false, false, false, true, false, true, false, true}, false);
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


        assertEquals(7, outboundMessages.size());
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, 
                                                 GREETME_ACTION,
                                                 GREETME_ACTION,
                                                 Names.WSRM_TERMINATE_SEQUENCE_ACTION,
                                                 Names.WSRM_SEQUENCE_ACKNOWLEDGMENT_ACTION,
                                                 Names.WSRM_CREATE_SEQUENCE_ACTION,
                                                 GREETME_ACTION};
        verifyActions(expectedActions, true);
        verifyMessageNumbers(new String[] {null, "1", "2", null, null, null, "1"}, true);
        verifyLastMessage(new boolean[] {false, false, true, false, false, false, false}, true);
        verifyAcknowledgements(new boolean[] {false, false, true, false, true, false, false}, true);

        // Note that we don't expect a partial response to standalone LastMessage or 
        // SequenceAcknowledgement messages
        
        verifyInboundMessages(11);
        
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, 
                                        null, GREETME_RESPONSE_ACTION, 
                                        null, GREETME_RESPONSE_ACTION, 
                                        null,
                                        null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION,
                                        null, GREETME_RESPONSE_ACTION};
        verifyActions(expectedActions, false);
        verifyMessageNumbers(
            new String[] {null, null, null, "1", null, "2", null, null, null, null, "1"}, false);
        boolean[] expected = new boolean[11];
        expected[5] = true;
        verifyLastMessage(expected, false);
        expected[3] = true;
        expected[10] = true;
        verifyAcknowledgements(expected, false);
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

        assertEquals(4, outboundMessages.size());
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, GREETME_ACTION,
                                                 Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, GREETME_ACTION};
        verifyActions(expectedActions, true);
        verifyMessageNumbers(new String[] {null, "1", null, "2"}, true);
        verifyLastMessage(new boolean[] {false, false, false, false}, true);
        for (int i = 0; i < 3; i++) {
            assertNull(getAcknowledgment(outboundMessages.get(i)));
        }
        assertNotNull(getAcknowledgment(outboundMessages.get(3)));

        // createSequenceResponse plus 2 greetMeResponse messages plus
        // one partial response for each of the four messages plus
        // CreateSequence request

        assertEquals(7, inboundContexts.size());
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null,
                                        Names.WSRM_CREATE_SEQUENCE_ACTION, GREETME_RESPONSE_ACTION, null,
                                        GREETME_RESPONSE_ACTION};
        verifyActions(expectedActions, false);
        verifyMessageNumbers(new String[] {null, null, null, null, null, "1", null, "2"}, false);
        verifyLastMessage(new boolean[7], false);
        for (int i = 0; i < 8; i++) {
            if (i == 4 || i == 6) {
                assertNotNull("Inbound message " + i + " does not contain expected acknowledgement",
                              getAcknowledgment(inboundContexts.get(i)));
            } else {
                assertNull("Inbound message " + i + " contains unexpected acknowledgement",
                           getAcknowledgment(inboundContexts.get(i)));
            }
        }
    }

    // --- test helpers ---

    private void verifyActions(String[] expectedActions, boolean outbound) throws Exception {

        assertEquals(expectedActions.length, outbound ? outboundMessages.size() : inboundContexts.size());

        for (int i = 0; i < expectedActions.length; i++) {
            String action = outbound ? getAction(outboundMessages.get(i)) : getAction(inboundContexts.get(i));
            if (null == expectedActions[i]) {
                assertNull((outbound ? "Outbound " : "Inbound") + " message " + i
                           + " has unexpected action: " + action, action);
            } else {
                assertEquals((outbound ? "Outbound " : "Inbound") + " message " + i
                             + " does not contain expected action header"
                             + System.getProperty("line.separator"), expectedActions[i], action);
            }
        }
    }

    private void verifyMessageNumbers(String[] expectedMessageNumbers, boolean outbound) throws Exception {

        assertEquals(expectedMessageNumbers.length, outbound ? outboundMessages.size()
            : inboundContexts.size());

        for (int i = 0; i < expectedMessageNumbers.length; i++) {
            if (outbound) {
                SOAPElement se = getSequence(outboundMessages.get(i));
                if (null == expectedMessageNumbers[i]) {
                    assertNull(se);
                } else {
                    assertEquals("Outbound message " + i + " does not contain expected message number "
                                 + expectedMessageNumbers[i], expectedMessageNumbers[i], 
                                 getMessageNumber(se));
                }
            } else {
                SequenceType s = getSequence(inboundContexts.get(i));
                String messageNumber = null == s ? null : s.getMessageNumber().toString();
                if (null == expectedMessageNumbers[i]) {
                    assertNull(messageNumber);
                } else {
                    assertEquals("Inbound message " + i + " does not contain expected message number "
                                 + expectedMessageNumbers[i], expectedMessageNumbers[i], messageNumber);
                }
            }
        }
    }

    private void verifyLastMessage(boolean[] expectedLastMessages, boolean outbound) throws Exception {
        
        assertEquals(expectedLastMessages.length, outbound ? outboundMessages.size()
            : inboundContexts.size());
        
        for (int i = 0; i < expectedLastMessages.length; i++) { 
            boolean lastMessage;
            if (outbound) {
                SOAPElement se = getSequence(outboundMessages.get(i));
                lastMessage = null == se ? false : getLastMessage(se);
            } else {
                SequenceType s = getSequence(inboundContexts.get(i));
                lastMessage = null == s ? false : null != s.getLastMessage();
            }
            assertEquals("Outbound message " + i 
                         + (expectedLastMessages[i] ? " does not contain expected last message element."
                             : " contains last message element."),
                         expectedLastMessages[i], lastMessage);  
        
        }
    }
    
    private void verifyAcknowledgements(boolean[] expectedAcks, boolean outbound) throws Exception {
        assertEquals(expectedAcks.length, outbound ? outboundMessages.size()
            : inboundContexts.size());
        
        for (int i = 0; i < expectedAcks.length; i++) {
            boolean ack = outbound ? (null != getAcknowledgment(outboundMessages.get(i))) 
                : (null != getAcknowledgment(inboundContexts.get(i)));
            
            if (expectedAcks[i]) {
                assertTrue((outbound ? "Outbound" : "Inbound") + " message " + i 
                           + " does not contain expected acknowledgement", ack);
            } else {
                assertFalse((outbound ? "Outbound" : "Inbound") + " message " + i 
                           + " contains unexpected acknowledgement", ack);
            }
        }
    }

    private void verifyAckRequestedOutbound(List<SOAPMessage> messages) throws Exception {
        boolean found = false;
        for (SOAPMessage m : messages) {
            SOAPElement se = getAckRequested(m);
            if (se != null) {
                found = true;
                break;
            }
        }
        assertTrue("expected AckRequested", found);
    }
    
    protected void verifyAckRequestedInbound(List<LogicalMessageContext> contexts) throws Exception {
        boolean found = false;
        for (LogicalMessageContext context : contexts) {
            RMProperties rmps = RMContextUtils.retrieveRMProperties(context, false);
            if (null != rmps 
                && rmps.getAcksRequested() != null 
                && rmps.getAcksRequested().size() > 0) {
                found = true;
                break;
            }
        }
        assertTrue("expected AckRequested", found);
    }

    protected String getAction(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName();
            if ((headerName.getURI().equals(org.objectweb.celtix.bus.ws.addressing.Names.WSA_NAMESPACE_NAME) 
                || headerName.getURI().equals(org.objectweb.celtix.bus.ws.addressing.VersionTransformer
                                              .Names200408.WSA_NAMESPACE_NAME))
                && localName.equals(org.objectweb.celtix.bus.ws.addressing.Names.WSA_ACTION_NAME)) {
                return headerElement.getTextContent();
            }
        }
        return null;
    }

    protected String getAction(LogicalMessageContext context) {
        AddressingProperties maps = ContextUtils.retrieveMAPs(context, false, false);
        if (null != maps && null != maps.getAction()) {
            return maps.getAction().getValue();
        }
        return null;
    }

    protected SOAPElement getSequence(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName();
            if (headerName.getURI().equals(Names.WSRM_NAMESPACE_NAME)
                && localName.equals(Names.WSRM_SEQUENCE_NAME)) {
                return (SOAPElement)header.getChildElements().next();
            }
        }
        return null;
    }

    private String getMessageNumber(SOAPElement elem) throws Exception {
        SOAPElement se = (SOAPElement)elem.getChildElements(
                                                            new QName(Names.WSRM_NAMESPACE_NAME,
                                                                      "MessageNumber")).next();
        return se.getTextContent();
    }

    protected SequenceType getSequence(LogicalMessageContext context) {
        RMProperties rmps = RMContextUtils.retrieveRMProperties(context, false);
        return rmps == null ? null : rmps.getSequence();
    }

    private boolean getLastMessage(SOAPElement elem) throws Exception {
        return elem.getChildElements(new QName(Names.WSRM_NAMESPACE_NAME, "LastMessage")).hasNext();
    }

    protected SOAPElement getAcknowledgment(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName();
            if (headerName.getURI().equals(Names.WSRM_NAMESPACE_NAME)
                && localName.equals(Names.WSRM_SEQUENCE_ACK_NAME)) {
                return (SOAPElement)header.getChildElements().next();
            }
        }
        return null;
    }
    
    protected SequenceAcknowledgement getAcknowledgment(LogicalMessageContext context) {
        RMProperties rmps = RMContextUtils.retrieveRMProperties(context, false);
        if (null != rmps && null != rmps.getAcks() && rmps.getAcks().size() > 0) {
            return rmps.getAcks().iterator().next();
        } 
        return null;
    }

    private SOAPElement getAckRequested(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName();
            if (headerName.getURI().equals(Names.WSRM_NAMESPACE_NAME)
                && localName.equals(Names.WSRM_ACK_REQUESTED_NAME)) {
                return (SOAPElement)header.getChildElements().next();
            }
        }
        return null;
    }

    private void createControl() {
        if (null == control) {
            URL wsdl = getClass().getResource("/wsdl/greeter_control.wsdl");
            ControlService controlService = new ControlService(wsdl, CONTROL_SERVICE_NAME);
            control = controlService.getPort(CONTROL_PORT_NAME, Control.class);

        }
    }

    private void setupEndpoints(String configuration) {

        if (configuration != null && configuration.equals(currentConfiguration)) {
            return;
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
    }
    
    private void verifyInboundMessages(int nExpected) {
        for (int i = 0; i < 10; i++) {
            if (inboundContexts.size() < nExpected) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    // ignore
                }
            } else {
                break;
            }
        }
        assertEquals("Did not receive the expected number of messages.", nExpected, inboundContexts.size());
    }

}
