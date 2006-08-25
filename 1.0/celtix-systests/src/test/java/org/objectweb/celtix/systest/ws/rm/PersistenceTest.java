package org.objectweb.celtix.systest.ws.rm;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
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
import org.objectweb.celtix.bus.configuration.wsrm.StoreInitParamType;
import org.objectweb.celtix.bus.configuration.wsrm.StoreType;
import org.objectweb.celtix.bus.ws.rm.Names;
import org.objectweb.celtix.bus.ws.rm.RMHandler;
import org.objectweb.celtix.bus.ws.rm.persistence.jdbc.RMTxStore;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.greeter_control.Greeter;
import org.objectweb.celtix.greeter_control.GreeterService;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.celtix.systest.common.TestServerBase;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;

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
    private String cfgFileProperty;
    

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
                
                RMTxStore.deleteDatabaseFiles("rmpersist", true);
                
                URL url = getClass().getResource("oneway-client-crash.xml"); 
                assertNotNull("cannot find test resource", url);
                configFileName = url.toString(); 
                ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
                builder.buildConfiguration(BusConfigurationBuilder.BUS_CONFIGURATION_URI, "celtix");

                super.setUp();

            }
            
            public void tearDown() {
                RMTxStore.deleteDatabaseFiles("rmpersist", false);
            }
        };
    }
    
    public void setUp() throws BusException {
        
        cfgFileProperty = System.getProperty("celtix.config.file");
        URL url = getClass().getResource("oneway-client-crash.xml"); 
        assertNotNull("cannot find test resource", url);
        String configFileName = url.toString();
        System.setProperty("celtix.config.file", configFileName);
        
        bus = Bus.init();

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
        assertTrue("Could not find SOAPMessageRecorder in handler chain", found);
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
        
        mf = new MessageFlow(outboundMessages, inboundContexts);
        
    }
    
    public void tearDown() throws BusException {
        RMHandler rmh = getRMHandler();
        rmh.destroy();
        
        greeter = null;
        bus.shutdown(true);
        
        if (null == cfgFileProperty) {
            System.clearProperty("celtix.config.file");
        } else {
            System.setProperty("celtix.config.file", cfgFileProperty);
        }
    }
    
    public void testPopulateStore() throws Exception {
        
        greeter.greetMeOneWay("one");
        greeter.greetMeOneWay("two");
        greeter.greetMeOneWay("three");
    
        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {Names.WSRM_CREATE_SEQUENCE_ACTION, 
                                                 GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, 
                                                 GREETMEONEWAY_ACTION};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);


        mf.verifyMessages(5, false);
        expectedActions = new String[] {null, Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION, 
                                        null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[5], false);
        boolean[] expectedAcks = new boolean[5];
        mf.verifyAcknowledgements(expectedAcks, false);
        
        RMHandler rmh = getRMHandler();
        assertNotNull(rmh);
        
        RMTxStore store = getStore(rmh);
        assertNotNull(store);
        
        Collection<RMDestinationSequence> dss =
            store.getDestinationSequences(rmh.getConfiguration().getParent().getId().toString());
        assertEquals(1, dss.size());
        
        Collection<RMSourceSequence> sss =
            store.getSourceSequences(rmh.getConfiguration().getParent().getId().toString());
        assertEquals(1, sss.size());
        
        Collection<RMMessage> msgs = 
            store.getMessages(sss.iterator().next().getIdentifier(), true);
        assertEquals(3, msgs.size());    
    }
    
    public void testRecover() throws Exception {
        
        // do nothing - resends should happen in the background
        
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException ex) {
            // ignore
        }
    
        int n = mf.getOutboundMessages().size();
        assertTrue(n > 0);        
        String[] expectedActions = new String[n];
        for (int i = 0; i < n; i++) {
            expectedActions[i] = GREETMEONEWAY_ACTION;
        }
        mf.verifyActions(expectedActions, true);
        for (int i = 0; i < n; i++) {
            SOAPElement elem = mf.getSequence(mf.getOutboundMessages().get(i));
            int mn = Integer.parseInt(mf.getMessageNumber(elem));
            assertTrue(mn >= 1 && mn <= 3);
        }

    }
    
    private RMHandler getRMHandler() {
        BindingProvider provider = (BindingProvider)greeter;
        AbstractBindingImpl abi = (AbstractBindingImpl)provider.getBinding();
        List<Handler> handlerChain = abi.getPreLogicalSystemHandlers();
        assertTrue(handlerChain.size() > 0);
        for (Handler h : handlerChain) {
            if (h instanceof RMHandler) {
                return (RMHandler)h;
            }
        }
        return null;
    }
    
    private RMTxStore getStore(RMHandler rmh) {
        Configuration cfg = rmh.getConfiguration();
        StoreType s = cfg.getObject(StoreType.class, "store");
        Map<String, String> params = new HashMap<String, String>();
        for (StoreInitParamType p : s.getInitParam()) {
            String name = p.getParamName();
            String value = p.getParamValue();
            params.put(name, value);
        }
        RMTxStore store = new RMTxStore();
        store.init(params);
        return store;
    }
     
}
