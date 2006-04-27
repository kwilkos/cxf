package org.objectweb.celtix.systest.ws.rm;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalMessageContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bus.busimpl.BusConfigurationBuilder;
import org.objectweb.celtix.bus.ws.rm.Names;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.greeter_control.Greeter;
import org.objectweb.celtix.greeter_control.GreeterService;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.celtix.systest.common.TestServerBase;

/**
 * Tests Reliable Messaging.
 */
public class PersistenceTest extends ClientServerTestBase {

    private static final String APP_NAMESPACE = "http://celtix.objectweb.org/greeter_control";
    private static final String GREETMEONEWAY_ACTION = APP_NAMESPACE + "/types/Greeter/greetMeOneWay";

    private static final QName SERVICE_NAME = new QName(APP_NAMESPACE, "GreeterService");
    private static final QName PORT_NAME = new QName(APP_NAMESPACE, "GreeterPort");

    private Bus bus;
    private MessageFlow mf;
    private GreeterService greeterService;
    private Greeter greeter;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistenceTest.class);
    }
    
    static class PersistenceTestServer extends TestServerBase {

        protected void run() {

            ControlImpl.setConfigFileProperty("oneway-client-crash");
            ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
            builder.buildConfiguration(BusConfigurationBuilder.BUS_CONFIGURATION_URI, "celtix");
            
            GreeterImpl implementor = new GreeterImpl();
            String address = "http://localhost:9000/SoapContext/GreeterPort";
            Endpoint.publish(address, implementor);

        }

        public static void main(String[] args) {
            try {
                PersistenceTestServer s = new PersistenceTestServer();
                s.start();            
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            } finally {
                System.out.println("done!");
            }
        }
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(PersistenceTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                // special case handling for WS-Addressing system test to avoid
                // UUID related issue when server is run as separate process
                // via maven on Win2k
                assertTrue("server did not launch correctly", launchServer(PersistenceTestServer.class, 
                    "Windows 2000".equals(System.getProperty("os.name"))));
            }

            public void setUp() throws Exception {
                
                URL url = getClass().getResource("oneway-client-crash.xml"); 
                assertNotNull("cannot find test resource", url);
                configFileName = url.toString(); 
                ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
                builder.buildConfiguration(BusConfigurationBuilder.BUS_CONFIGURATION_URI, "celtix");

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
    
    public void testOnewayTerminateOnShutdown() throws Exception {

        try {
            greeter.greetMeOneWay("once");
            greeter.greetMeOneWay("twice");
            greeter.greetMeOneWay("thrice");
        } finally {
            bus.shutdown(true);
        }

        mf.verifyMessages(6, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, 
                                                 GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, 
                                                 GREETMEONEWAY_ACTION,
                                                 Names.WSRM_LAST_MESSAGE_ACTION,
                                                 Names.WSRM_TERMINATE_SEQUENCE_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3", "4", null}, true);


        mf.verifyMessages(6, false);
        expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, 
                                        null, null, null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null, null, null}, false);
        boolean[] expectedAcks = new boolean[6];
        expectedAcks[4] = true;
        mf.verifyAcknowledgements(expectedAcks, false);
    }
}
