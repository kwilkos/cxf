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
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.greeter_control.Greeter;
import org.objectweb.celtix.greeter_control.GreeterService;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.celtix.ws.rm.Names;

/**
 * Tests Reliable Messaging.
 */
public class ShutdownTest extends ClientServerTestBase {

    private static final String APP_NAMESPACE = "http://celtix.objectweb.org/greeter_control";
    private static final String GREETMEONEWAY_ACTION = APP_NAMESPACE + "/types/Greeter/greetMeOneWay";

    private static final QName SERVICE_NAME = new QName(APP_NAMESPACE, "GreeterService");
    private static final QName PORT_NAME = new QName(APP_NAMESPACE, "GreeterPort");

    private Bus bus;
    private MessageFlow mf;
    private GreeterService greeterService;
    private Greeter greeter;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ShutdownTest.class);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ShutdownTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                // special case handling for WS-Addressing system test to avoid
                // UUID related issue when server is run as separate process
                // via maven on Win2k
                assertTrue("server did not launch correctly", launchServer(ShutdownTestServer.class, 
                    "Windows 2000".equals(System.getProperty("os.name"))));
            }

            public void setUp() throws Exception {
                
                URL url = getClass().getResource("oneway-terminate-on-shutdown.xml"); 
                assertNotNull("cannot find test resource", url);
                configFileName = url.toString(); 

                super.setUp();

            }
        };
    }
    
    public void setUp() throws BusException {
        
        bus = Bus.init();

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
        
        mf = new MessageFlow(outboundMessages, inboundContexts);
        
    }
    
    public void tearDown() {
    }
    
    public void testOnewayTerminateOnShutdown() throws Exception {

        try {
            greeter.greetMeOneWay("once");
            greeter.greetMeOneWay("twice");
            greeter.greetMeOneWay("thrice");
        } finally {
            bus.shutdown(true);
        }

        mf.verifyMessages(6, true, 1000, 3);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, 
                                                 GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, 
                                                 GREETMEONEWAY_ACTION,
                                                 Names.WSRM_LAST_MESSAGE_ACTION,
                                                 Names.WSRM_TERMINATE_SEQUENCE_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3", "4", null}, true);


        mf.verifyMessages(7, false);
        expectedActions = new String[] {null,
                                        Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, 
                                        null, null, null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null, null, null, null}, false);
        boolean[] expectedAcks = new boolean[7];
        expectedAcks[5] = true;
        mf.verifyAcknowledgements(expectedAcks, false);
    }
}
