package org.objectweb.celtix.systest.ws.rm;

import java.net.URL;
import java.util.ArrayList;
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

import junit.framework.Test;
import junit.framework.TestSuite;

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

    private static final QName CONTROL_SERVICE_NAME = new QName(APP_NAMESPACE, "ControlService");
    private static final QName SERVICE_NAME = new QName(APP_NAMESPACE, "GreeterService");
    private static final QName CONTROL_PORT_NAME = new QName(APP_NAMESPACE, "ControlPort");
    private static final QName PORT_NAME = new QName(APP_NAMESPACE, "GreeterPort");

    private GreeterService greeterService;
    private Greeter greeter;
    private Control control;
    private String currentConfiguration;
    private List<SOAPMessage> outboundMessages;
    private List<SOAPMessage> inboundMessages;

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
                // avoid re-using a previously created configuration for a bus with id "celtix"
                ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
                builder.buildConfiguration(BusConfigurationBuilder.BUS_CONFIGURATION_URI, "celtix");
                
                super.setUp();
                
            }
        };
    }

    // --- tests ---

    public void testOnewayAnonymousAcks() throws Exception {

        setupEndpoints("");
        
        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice"); 
        greeter.greetMeOneWay("thrice");

        // three application messages plus createSequence
        assertEquals(4, outboundMessages.size());
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION,
                                                 GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION};
        verifyActions(expectedActions, true);
        verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse plus three partial responses
        assertEquals(4, inboundMessages.size());
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null, null};
        verifyActions(expectedActions, false);
        verifyMessageNumbers(new String[] {null, null, null, null}, false);
        assertNull(getAcknowledgment(inboundMessages.get(0)));
        for (int i = 1; i < 4; i++) {
            assertNotNull(getAcknowledgment(inboundMessages.get(i)));
        }
    }
    
    public void testOnewayDeferredAnonymousAcks() throws Exception {

        setupEndpoints("anonymous-deferred");
        
        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");
        
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException ex) {
            // ignore
        }
        
        greeter.greetMeOneWay("thrice");
        

        // three application messages plus createSequence
        assertEquals(4, outboundMessages.size());
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION,
                                                 GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION};
        verifyActions(expectedActions, true);
        verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse plus three partial responses, only the last 
        // one should include a sequence acknowledgment
        
        assertEquals(4, inboundMessages.size());
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, null, null, null};
        verifyActions(expectedActions, false);
        verifyMessageNumbers(new String[] {null, null, null, null}, false);
        for (int i = 0; i < 3; i++) {
            assertNull("Inbound message " + (i + 1) + " contains sequence acknowledgment header",
                       getAcknowledgment(inboundMessages.get(i)));
        }
        assertNotNull("Inbound message 4 does not contain sequence acknowledgment header ",
                      getAcknowledgment(inboundMessages.get(3)));
    }
    
    public void testOnewayAnonymousAcksSequenceLength1() throws Exception {

        setupEndpoints("anonymous-seqlength1");
        
        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice"); 

        // two application messages plus two createSequence

        assertEquals(4, outboundMessages.size());
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION,
                                                 GREETMEONEWAY_ACTION,
                                                 Names.WSRM_CREATE_SEQUENCE_ACTION,
                                                 GREETMEONEWAY_ACTION};
        verifyActions(expectedActions, true);
        verifyMessageNumbers(new String[] {null, "1", null, "1"}, true);
        verifyLastMessage(new boolean[] {false, true, false, true}, true);

        
        // createSequenceResponse plus partial response ||: 2

        assertEquals(4, inboundMessages.size());
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, 
                                        null, 
                                        Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, 
                                        null};
        verifyActions(expectedActions, false);
        verifyMessageNumbers(new String[] {null, null, null, null}, false);
        verifyLastMessage(new boolean[] {false, false, false, false}, false);
        
        int i = 0;
        while (i < 4) {
            assertNull(getAcknowledgment(inboundMessages.get(i++)));
            assertNotNull(getAcknowledgment(inboundMessages.get(i++)));
        }
    }
    
    public void xtestOnewayNonAnonymous() throws Exception {

        
    }
    
    public void xtestTwowayNonAnonymous() throws Exception {
        
    }
    
    public void xtestTwowayNonAnonymousDeferred() throws Exception {
        
    }
    
    // --- test helpers ---


    private void verifyActions(String[] expectedActions, boolean outbound) throws Exception {
        List<SOAPMessage> messages = outbound ? outboundMessages : inboundMessages;
        assertEquals(expectedActions.length, messages.size());
        for (int i = 0; i < expectedActions.length; i++) {
            String action = getAction(messages.get(i));
            if (null == expectedActions[i]) {
                assertNull(action);
            } else {
                assertEquals((outbound ? "Outbound " : "Inbound")
                             + " message " + (i + 1) + " does not contain expected action header"
                             + System.getProperty("line.separator"),
                             expectedActions[i], action);
            }
        }
    }

    private void verifyMessageNumbers(String[] expectedMessageNumbers, boolean outbound)
        throws Exception {
        List<SOAPMessage> messages = outbound ? outboundMessages : inboundMessages;
        assertEquals(expectedMessageNumbers.length, messages.size());
        for (int i = 0; i < expectedMessageNumbers.length; i++) {
            SOAPElement se = getSequence(messages.get(i));
            if (null == expectedMessageNumbers[i]) {
                assertNull(se);
            } else {
                assertEquals((outbound ? "Outbound " : "Inbound")
                             + " message " + (i + 1) + " does not contain expected message number "
                         + expectedMessageNumbers[i],
                         expectedMessageNumbers[i], getMessageNumber(se));
            }
        }
    }
    
    private void verifyLastMessage(boolean[] expectedLastMessages, boolean outbound) throws Exception {
        List<SOAPMessage> messages = outbound ? outboundMessages : inboundMessages;
        assertEquals(expectedLastMessages.length, messages.size());
        for (int i = 0; i < expectedLastMessages.length; i++) {
            SOAPElement se = getSequence(messages.get(i));
            if (null == se) {
                if (expectedLastMessages[i]) {
                    fail((outbound ? "Outbound " : "Inbound") + " message " + (i + 1)
                         + " contains last message element.");
                }
            } else {
                assertEquals((outbound ? "Outbound " : "Inbound") + " message " + (i + 1)
                             + " does not contain expected last message element", expectedLastMessages[i],
                             getLastMessage(se));
            }
        }
    }

    private String getAction(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement =
                (SOAPHeaderElement)headerElements.next();
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

    private SOAPElement getSequence(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement =
                (SOAPHeaderElement)headerElements.next();
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
        SOAPElement se = (SOAPElement)elem.getChildElements(new QName(Names.WSRM_NAMESPACE_NAME,
                                                                      "MessageNumber")).next();
        return se.getTextContent();
    }
    
    private boolean getLastMessage(SOAPElement elem) throws Exception {
        SOAPElement se = (SOAPElement)elem.getChildElements(new QName(Names.WSRM_NAMESPACE_NAME,
                                                                      "LastMessage")).next();
        return null != se;
    }

    private SOAPElement getAcknowledgment(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement =
                (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName();
            if (headerName.getURI().equals(Names.WSRM_NAMESPACE_NAME)
                && localName.equals(Names.WSRM_SEQUENCE_ACK_NAME)) {
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
            if (h instanceof SOAPMessageRecorder) {
                SOAPMessageRecorder recorder = (SOAPMessageRecorder)h;
                outboundMessages = new ArrayList<SOAPMessage>();
                recorder.setOutboundMessages(outboundMessages);
                inboundMessages = new ArrayList<SOAPMessage>();
                recorder.setInboundMessages(inboundMessages);
                found = true;
                break;
            }
        }
        assertTrue("Could not find SOAPMessageRecorder in post protocol handler chain", found);
        currentConfiguration = configuration;
    }


}
