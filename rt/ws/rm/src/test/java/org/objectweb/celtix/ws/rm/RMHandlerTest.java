package org.objectweb.celtix.ws.rm;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Future;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Response;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalMessageContext;

import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bus.configuration.spring.ConfigurationProviderImpl;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.configuration.impl.TypeSchemaHelper;
import org.objectweb.celtix.greeter_control.Greeter;
import org.objectweb.celtix.greeter_control.GreeterService;
import org.objectweb.celtix.greeter_control.PingMeFault;
import org.objectweb.celtix.greeter_control.types.GreetMeResponse;
import org.objectweb.celtix.greeter_control.types.PingMeResponse;
import org.objectweb.celtix.greeter_control.types.SayHiResponse;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.expectLastCall;

public class RMHandlerTest extends TestCase {
    private static final String APP_NAMESPACE = "http://celtix.objectweb.org/greeter_control";
    private static final String CFG_FILE_PROPERTY = "celtix.config.file";
    private static final QName SERVICE_NAME = new QName(APP_NAMESPACE, "GreeterService");
    private static final QName PORT_NAME = new QName(APP_NAMESPACE, "GreeterPort");

    private String celtixCfgFile;
    private Bus bus;

    private IMocksControl control;
    private ConfigurationHelper configurationHelper;
    private RMSource source;
    private RMDestination destination;
    private Timer timer;
    private RMHandler handler;

    public void setUp() {
        control = EasyMock.createNiceControl();
        configurationHelper = control.createMock(ConfigurationHelper.class);
        source = control.createMock(RMSource.class);
        destination = control.createMock(RMDestination.class);
        timer = control.createMock(Timer.class);
        
        handler = new RMHandler();
    }

    public void tearDown() {
        configurationHelper = null;
        source = null;
        destination = null;
        timer = null;
        handler = null;
        if (1 < 0) {
            System.out.println(configurationHelper);
            System.out.println(source);
            System.out.println(destination);
            System.out.println(timer);
            System.out.println(handler);
        }
    }

    public void setUpReal() throws Exception {
        ConfigurationProviderImpl.clearBeanFactoriesMap();
        TypeSchemaHelper.clearCache();
        ConfigurationBuilderFactory.clearBuilder();

        celtixCfgFile = System.getProperty(CFG_FILE_PROPERTY);
        URL url = RMHandlerTest.class.getResource("resources/RMHandlerTest.xml");
        assert null != url;
        String configFileName = url.toString();
        System.setProperty(CFG_FILE_PROPERTY, configFileName);
        bus = Bus.init();
    }

    public void tearDownReal() throws Exception {
        bus.shutdown(true);
        if (null != celtixCfgFile) {
            System.setProperty(CFG_FILE_PROPERTY, celtixCfgFile);
        } else {
            System.clearProperty(CFG_FILE_PROPERTY);
        }

        ConfigurationBuilderFactory.clearBuilder();
        TypeSchemaHelper.clearCache();
        ConfigurationProviderImpl.clearBeanFactoriesMap();
    }

    public void xtestInitialisationClientSide() throws Exception {
        setUpReal();
        try {
            URL wsdl = getClass().getResource("/wsdl/greeter_control.wsdl");
            GreeterService greeterService = new GreeterService(wsdl, SERVICE_NAME);
            Greeter greeter = greeterService.getPort(PORT_NAME, Greeter.class);

            BindingProvider provider = (BindingProvider)greeter;
            AbstractBindingImpl abi = (AbstractBindingImpl)provider.getBinding();
            List<Handler> handlerChain = abi.getPreLogicalSystemHandlers();

            assertEquals(1, handlerChain.size());
            handler = (RMHandler)handlerChain.get(0);

            assertEquals(bus, handler.getBus());
            assertNotNull(handler.getClientBinding());
            assertNull(handler.getServerBinding());
            assertNotNull(handler.getBinding());

            assertNotNull(handler.getClientTransport());
            assertNull(handler.getServerTransport());
            assertNotNull(handler.getTransport());
        } finally {
            tearDownReal();
        }
    }

    public void xtestInitialisationServerSide() throws Exception {
        setUpReal();
        try {
            String address = "http://localhost:9020/SoapContext/GreeterPort";
            Endpoint ep = Endpoint.publish(address, new GreeterImpl());

            AbstractBindingImpl abi = (AbstractBindingImpl)ep.getBinding();
            List<Handler> handlerChain = abi.getPreLogicalSystemHandlers();

            assertEquals(1, handlerChain.size());
            handler = (RMHandler)handlerChain.get(0);

            assertEquals(bus, handler.getBus());
            assertNull(handler.getClientBinding());
            assertNotNull(handler.getServerBinding());
            assertNotNull(handler.getBinding());

            assertNull(handler.getClientTransport());
            assertNotNull(handler.getServerTransport());
            assertNotNull(handler.getTransport());
        } finally {
            tearDownReal();
        }
    }
    
    public void testConstructor() {
        assertNotNull(handler.getProxy());
        assertNotNull(handler.getServant());
        
        assertNull(handler.getBinding());
        assertNull(handler.getBus());
        assertNull(handler.getClientBinding());
        assertNull(handler.getClientTransport());
        assertNull(handler.getConfigurationHelper());
        assertNull(handler.getDestination());
        assertNull(handler.getPersistenceManager());
        assertNull(handler.getServerBinding());
        assertNull(handler.getServerTransport());
        assertNull(handler.getSource());
        assertNull(handler.getStore());
    }

    public void testClose() {
        LogicalMessageContext ctx = control.createMock(LogicalMessageContext.class);
        handler.close(ctx);      
    }
    
    public void testHandleMessage() throws NoSuchMethodException {
        Method m = RMHandler.class.getDeclaredMethod("handle", new Class[] {LogicalMessageContext.class});
        handler = control.createMock(RMHandler.class, new Method[] {m});
        LogicalMessageContext ctx = control.createMock(LogicalMessageContext.class);
        expect(handler.handle(ctx)).andReturn(true);
        
        control.replay();
        handler.handleMessage(ctx);
        control.verify();
    }
    
    public void testHandleFault() throws NoSuchMethodException {
        Method m = RMHandler.class.getDeclaredMethod("handle", new Class[] {LogicalMessageContext.class});
        handler = control.createMock(RMHandler.class, new Method[] {m});
        LogicalMessageContext ctx = control.createMock(LogicalMessageContext.class);
        expect(handler.handle(ctx)).andReturn(true);
        
        control.replay();
        handler.handleMessage(ctx);
        control.verify();
    }
    
    
    public void testHandleOutbound() throws Exception {
        doTestHandle(true, false); 
    }
    
    public void testHandleInbound() throws Exception {
        doTestHandle(false, true); 
    }
    
    
    private void doTestHandle(boolean outbound, boolean throwSequenceFault) throws SequenceFault, 
        NoSuchMethodException {
        Method m = RMHandler.class.getDeclaredMethod(outbound ? "handleOutbound" : "handleInbound",
            new Class[] {LogicalMessageContext.class});
        handler = control.createMock(RMHandler.class, new Method[] {m});
        LogicalMessageContext ctx = control.createMock(LogicalMessageContext.class);
        expect(ctx.get(MESSAGE_OUTBOUND_PROPERTY)).andReturn(outbound ? Boolean.TRUE : Boolean.FALSE);
        if (outbound) {
            handler.handleOutbound(ctx);
        } else {
            handler.handleInbound(ctx);
        }
        SequenceFault sf = null;        
        
        if (throwSequenceFault) {
            sf = new SequenceFault("no such sequence");
            expectLastCall().andThrow(sf);
        } else {
            expectLastCall();
        }
        
        control.replay();
        handler.handle(ctx);
        control.verify();
    }
    
    public void testProcessAcknowledgments() {
        
    }
    
    public void testProcessSequence() throws SequenceFault {
        RMProperties rmps = control.createMock(RMProperties.class);
        expect(rmps.getSequence()).andReturn(null);
        control.replay();
        handler.processSequence(rmps, null);
        control.verify();
        
        control.reset();
        handler.setInitialised(configurationHelper, source, destination, timer, true);
        AddressingProperties maps = control.createMock(AddressingProperties.class);
        SequenceType s = control.createMock(SequenceType.class); 
        expect(rmps.getSequence()).andReturn(s);
        expect(maps.getReplyTo()).andReturn(null);
        destination.acknowledge(s, null);
        expectLastCall();
        control.replay();
        handler.processSequence(rmps, maps);
        control.verify();
        
        control.reset();
        expect(rmps.getSequence()).andReturn(s);
        EndpointReferenceType epr = control.createMock(EndpointReferenceType.class);
        expect(maps.getReplyTo()).andReturn(epr).times(2);
        AttributedURIType uri = control.createMock(AttributedURIType.class);
        expect(epr.getAddress()).andReturn(uri);
        String address = Names.WSA_ANONYMOUS_ADDRESS;
        expect(uri.getValue()).andReturn(address);
        destination.acknowledge(s, address);
        expectLastCall();
        control.replay();
        handler.processSequence(rmps, maps);
        control.verify();
    }
    
    public void testProcessAcknowledgmentRequests() {
        
    }
    
    public void testProcessDeliveryAssurance() {
        RMProperties rmps = control.createMock(RMProperties.class);
        expect(rmps.getSequence()).andReturn(null);
        control.replay();
        handler.processDeliveryAssurance(rmps);
        control.verify();
        
        control.reset();
        handler.setInitialised(configurationHelper, source, destination, timer, true);
        SequenceType s = control.createMock(SequenceType.class); 
        expect(rmps.getSequence()).andReturn(s);
        Identifier id = control.createMock(Identifier.class);
        expect(s.getIdentifier()).andReturn(id); 
        DestinationSequence ds = control.createMock(DestinationSequence.class);
        expect(destination.getSequence(id)).andReturn(ds);
        BigInteger mn = BigInteger.TEN;
        expect(s.getMessageNumber()).andReturn(mn);
        expect(ds.applyDeliveryAssurance(mn)).andReturn(true);
        control.replay();
        handler.processDeliveryAssurance(rmps);
        control.verify();
    }
    
    public void testAddAcknowledgements() {
        
    }
    
    
    
    

    @WebService(serviceName = "GreeterService", 
                portName = "GreeterPort", 
                endpointInterface = "org.objectweb.celtix.greeter_control.Greeter", 
                targetNamespace = "http://celtix.objectweb.org/greeter_control")
    class GreeterImpl implements Greeter {

        public String greetMe(String requestType) {
            return null;
        }

        public Future<?> greetMeAsync(String requestType, AsyncHandler<GreetMeResponse> asyncHandler) {
            return null;
        }

        public Response<GreetMeResponse> greetMeAsync(String requestType) {
            return null;
        }

        public void greetMeOneWay(String requestType) {
        }

        public void pingMe() throws PingMeFault {
        }

        public Response<PingMeResponse> pingMeAsync() {
            return null;
        }

        public Future<?> pingMeAsync(AsyncHandler<PingMeResponse> asyncHandler) {
            return null;
        }

        public String sayHi() {
            return null;
        }

        public Response<SayHiResponse> sayHiAsync() {
            return null;
        }

        public Future<?> sayHiAsync(AsyncHandler<SayHiResponse> asyncHandler) {
            return null;
        }
    }
}
