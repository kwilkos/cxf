package org.objectweb.celtix.bus.ws.rm.soap;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.bus.busimpl.BusConfigurationBuilder;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.bus.jaxws.ServiceImpl;
import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.VersionTransformer;
import org.objectweb.celtix.bus.ws.rm.ConfigurationHelper;
import org.objectweb.celtix.bus.ws.rm.DestinationSequence;
import org.objectweb.celtix.bus.ws.rm.RMDestination;
import org.objectweb.celtix.bus.ws.rm.RMHandler;
import org.objectweb.celtix.bus.ws.rm.RMPolicyProvider;
import org.objectweb.celtix.bus.ws.rm.RMPropertiesImpl;
import org.objectweb.celtix.bus.ws.rm.RMSource;
import org.objectweb.celtix.bus.ws.rm.RMUtils;
import org.objectweb.celtix.bus.ws.rm.RetransmissionQueue;
import org.objectweb.celtix.bus.ws.rm.SourceSequence;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.workqueue.AutomaticWorkQueue;
import org.objectweb.celtix.workqueue.WorkQueueManager;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceType;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;
import org.objectweb.celtix.ws.rm.persistence.RMStore;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;


import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;
import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.RM_PROPERTIES_OUTBOUND;

public class PersistenceHandlerTest extends TestCase {
    private static final QName SERVICE_NAME = 
        new QName("http://celtix.objectweb.org/greeter_control", "GreeterService");
    private static final String PORT_NAME = "GreeterPort";
    private static final String APP_MSG_ACTION = 
        "http://celtix.objectweb.org/greeter_control/types/Greeter/greetMe";
    
    private IMocksControl control;
    private PersistenceHandler handler;
    private AbstractClientBinding clientBinding;
    private AbstractServerBinding serverBinding;
    private ConfigurationHelper ch;   
    private RMStore store; 
    private RetransmissionQueue queue;
    private Bus bus;
    private WorkQueueManager wqm;
    private AutomaticWorkQueue wq;
    
    
    public void setUp() {
        control = EasyMock.createNiceControl();  
        handler = new PersistenceHandler();  
    }  
    
    public void tearDown() {        
        ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
        builder.clearConfigurations();
        RMHandler.getHandlerMap().clear();
    }
    
    public void testInitialiseClient() {
        clientBinding = control.createMock(AbstractClientBinding.class);
        handler.clientBinding = clientBinding;
        RMHandler rmh = control.createMock(RMHandler.class);
        RMHandler.getHandlerMap().put(clientBinding, rmh);
        rmh.setPersistenceManager(handler);
        expectLastCall();
        setupConfigurationBuilder(false);
        
        control.replay();    
        handler.initialise();
        control.verify();
    }
    
    public void testInitialiseServer() { 
        serverBinding = control.createMock(AbstractServerBinding.class);
        handler.serverBinding = serverBinding;
        RMHandler rmh = control.createMock(RMHandler.class);
        RMHandler.getHandlerMap().put(serverBinding, rmh);
        rmh.setPersistenceManager(handler);
        expectLastCall();
        setupConfigurationBuilder(true);
        
        control.replay();        
        handler.initialise();
        control.verify();
    }
    
    public void testGetConfigurationHelper() {       
        ch = control.createMock(ConfigurationHelper.class);   
        store = control.createMock(RMStore.class); 
        queue = control.createMock(RetransmissionQueue.class);
        handler.initialise(ch, store, queue);
        assertSame("did not return expected configuration helper", ch, handler.getConfigurationHelper());
    }
    
    public void testGetStore() {       
        ch = control.createMock(ConfigurationHelper.class);   
        store = control.createMock(RMStore.class); 
        queue = control.createMock(RetransmissionQueue.class);
        handler.initialise(ch, store, queue);
        assertSame("did not return expected store", store, handler.getStore());
    }
    
    public void testGetQueue() {       
        ch = control.createMock(ConfigurationHelper.class);   
        store = control.createMock(RMStore.class); 
        queue = control.createMock(RetransmissionQueue.class);
        handler.initialise(ch, store, queue);
        assertSame("did not return expected retransmission queue", queue, handler.getQueue());
    }
    
    public void testGetBinding() {       
        clientBinding = control.createMock(AbstractClientBinding.class);
        handler.clientBinding = clientBinding;
        assertSame("did not return expected binding", clientBinding, handler.getBinding());
        serverBinding = control.createMock(AbstractServerBinding.class);
        handler.serverBinding = serverBinding;
        handler.clientBinding = null;
        assertSame("did not return expected binding", serverBinding, handler.getBinding());
    }
    
    public void testGetTransport() {
        ClientTransport clientTransport = control.createMock(ClientTransport.class);
        handler.clientTransport = clientTransport;
        assertSame("did not return expected transport", clientTransport, handler.getClientTransport());
        ServerTransport serverTransport = control.createMock(ServerTransport.class);
        handler.serverTransport = serverTransport;
        assertSame("did not return expected transport", serverTransport, handler.getServerTransport());
    }
    
    public void testShutdown() {       
        ch = control.createMock(ConfigurationHelper.class);   
        store = control.createMock(RMStore.class); 
        queue = control.createMock(RetransmissionQueue.class);
        handler.initialise(ch, store, queue);
        queue.stop();
        expectLastCall();
        
        control.replay();
        handler.shutdown();
        control.verify();
    }
    
    public void testClose() {
        SOAPMessageContext ctx = control.createMock(SOAPMessageContext.class);
        handler.close(ctx);
    }
    
    public void testGetHeaders() {
        assertEquals("unexpected number of headers", 3, handler.getHeaders().size());
    }
    
    public void testHandleMessageOutbound() {
        doTestHandleMessage(true);
    } 

    public void testHandleMessageInbound() {
        doTestHandleMessage(false);
    } 
    
    public void doTestHandleMessage(boolean outbound) {
        setupQueue(); 
        SOAPMessageContext ctx = control.createMock(SOAPMessageContext.class);
        expect(ctx.get(MESSAGE_OUTBOUND_PROPERTY)).andReturn(outbound ? Boolean.TRUE : Boolean.FALSE);
        
        control.replay();
        handler.handleMessage(ctx);
        control.verify();
    }
   
    public void testHandleFaultOutbound() {
        doTestHandleFault(true);
    } 
    
    public void testHandlerFaultInbound() {
        doTestHandleFault(false);
    }
    
    private void doTestHandleFault(boolean outbound) {
        setupQueue(); 
        SOAPMessageContext ctx = control.createMock(SOAPMessageContext.class);
        expect(ctx.get(MESSAGE_OUTBOUND_PROPERTY)).andReturn(outbound ? Boolean.TRUE : Boolean.FALSE);
        
        control.replay();
        handler.handleFault(ctx);
        control.verify();
    }
    
    
    public void testHandleOutboundRMControlMessage() {
        SOAPMessageContext ctx = setupContext(RMUtils.getRMConstants().getSequenceInfoAction());
        
        control.replay();
        handler.handleOutbound(ctx);
        control.verify();      
    }
    
    public void testHandleOutboundApplicationMessageNoRMPs() {
        SOAPMessageContext ctx = setupContext(APP_MSG_ACTION);
        
        expect(ctx.get(RM_PROPERTIES_OUTBOUND)).andReturn(null);
        
        control.replay();
        handler.handleOutbound(ctx);
        control.verify();      
    }
    
    public void testHandleOutboundApplicationMessageRMPsNoSequence() {
        SOAPMessageContext ctx = setupContext(APP_MSG_ACTION);
        RMPropertiesImpl rmps = control.createMock(RMPropertiesImpl.class);
        expect(ctx.get(RM_PROPERTIES_OUTBOUND)).andReturn(rmps);
        expect(rmps.getSequence()).andReturn(null);
        
        control.replay();
        handler.handleOutbound(ctx);
        control.verify();      
    }
    
    public void testCacheUnacknowledged() {
        clientBinding = control.createMock(AbstractClientBinding.class);
        handler.clientBinding = clientBinding;
        SourceSequence ss = control.createMock(SourceSequence.class);
        RMMessage msg = control.createMock(RMMessage.class);
        ObjectMessageContext objCtx = control.createMock(ObjectMessageContext.class);
        expect(clientBinding.createObjectContext()).andReturn(objCtx);
        MessageContext ctx = control.createMock(MessageContext.class);
        expect(msg.getContext()).andReturn(ctx);
        objCtx.putAll(ctx);
        expectLastCall();
        ch = control.createMock(ConfigurationHelper.class);   
        store = control.createMock(RMStore.class); 
        queue = control.createMock(RetransmissionQueue.class);
        handler.initialise(ch, store, queue);
        queue.addUnacknowledged(objCtx);
        expectLastCall();
        store.persistOutgoing(ss, msg);
        expectLastCall();
        
        control.replay();
        handler.cacheUnacknowledged(ss, msg);
        control.verify();     
    }
    
    public void testHandleOutboundApplicationMessage() throws NoSuchMethodException {
        Method m = PersistenceHandler.class.getMethod("cacheUnacknowledged",
            new Class[] {SourceSequence.class, RMMessage.class});
        handler = control.createMock(PersistenceHandler.class, new Method[] {m});
        SOAPMessageContext ctx = setupContext(APP_MSG_ACTION);
        RMPropertiesImpl rmps = control.createMock(RMPropertiesImpl.class);
        expect(ctx.get(RM_PROPERTIES_OUTBOUND)).andReturn(rmps);
        SequenceType st = control.createMock(SequenceType.class);
        expect(rmps.getSequence()).andReturn(st).times(4);
        expect(st.getMessageNumber()).andReturn(BigInteger.TEN);
        expect(st.getLastMessage()).andReturn(null);
        Identifier id = control.createMock(Identifier.class);
        expect(st.getIdentifier()).andReturn(id);
        handler.cacheUnacknowledged(isA(SourceSequence.class), isA(RMMessage.class));
        expectLastCall();

        control.replay();
        handler.handleOutbound(ctx);
        control.verify();
    }
    
    public void testRestore() {  
        clientBinding = control.createMock(AbstractClientBinding.class);
        handler.clientBinding = clientBinding;
        RMHandler rmh = control.createMock(RMHandler.class);
        RMHandler.getHandlerMap().put(clientBinding, rmh);
        
        ch = control.createMock(ConfigurationHelper.class);   
        store = control.createMock(RMStore.class); 
        queue = control.createMock(RetransmissionQueue.class);
        handler.initialise(ch, store, queue);
       
        expect(ch.getEndpointId()).andReturn("endpoint");
        Collection<RMSourceSequence> sss = new ArrayList<RMSourceSequence>();
        SourceSequence ss = control.createMock(SourceSequence.class);
        sss.add(ss);
        expect(store.getSourceSequences("endpoint")).andReturn(sss);
        RMSource source = control.createMock(RMSource.class);
        expect(rmh.getSource()).andReturn(source);
        source.addSequence(ss, false);
        expectLastCall();
        Collection<SourceSequence> allSequences = new ArrayList<SourceSequence>();
        allSequences.add(ss);
        expect(source.getAllSequences()).andReturn(allSequences);
        queue.populate(allSequences);
        expectLastCall();
        expect(queue.isEmpty()).andReturn(false);
        bus = control.createMock(Bus.class);
        expect(clientBinding.getBus()).andReturn(bus);
        wqm = control.createMock(WorkQueueManager.class);
        expect(bus.getWorkQueueManager()).andReturn(wqm);
        wq = control.createMock(AutomaticWorkQueue.class);
        expect(wqm.getAutomaticWorkQueue()).andReturn(wq);
        queue.start(wq);
        expectLastCall();
        RMDestination destination = control.createMock(RMDestination.class);
        expect(rmh.getDestination()).andReturn(destination);
        Collection<RMDestinationSequence> dss = new ArrayList<RMDestinationSequence>();
        DestinationSequence ds = control.createMock(DestinationSequence.class);
        dss.add(ds);
        expect(store.getDestinationSequences("endpoint")).andReturn(dss);
        destination.addSequence(ds, false);
        expectLastCall();
        
        control.replay();
        handler.restore();
        control.verify();
    }
    
    private void setupConfigurationBuilder(boolean server) {
        Configuration configuration;
        ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
        Configuration busCfg = builder.buildConfiguration(
            BusConfigurationBuilder.BUS_CONFIGURATION_URI, "PersistenceHandlerTest");
        Configuration parent = null;
        if (server) {
            parent = builder.buildConfiguration(EndpointImpl.ENDPOINT_CONFIGURATION_URI, 
                                                SERVICE_NAME.toString(), busCfg);
        } else {
            String id = SERVICE_NAME.toString() + "/" + PORT_NAME;
            parent = builder.buildConfiguration(ServiceImpl.PORT_CONFIGURATION_URI, id, busCfg);
        }
        configuration = builder.buildConfiguration(ConfigurationHelper.RM_CONFIGURATION_URI, 
                                                   ConfigurationHelper.RM_CONFIGURATION_ID, parent);
        
        bus = control.createMock(Bus.class);
        if (!server) {
            expect(clientBinding.getBus()).andReturn(bus);
        } else {
            expect(serverBinding.getBus()).andReturn(bus);
        }
        expect(bus.getConfiguration()).andReturn(busCfg);
        EndpointReferenceType epr = EndpointReferenceUtils.getEndpointReference(
            "http://localhost:9000/SoapContext/GreeterPort");
        EndpointReferenceUtils.setServiceAndPortName(epr, SERVICE_NAME, PORT_NAME);
        if (!server) {
            expect(clientBinding.getEndpointReference()).andReturn(epr);
        } else {
            expect(serverBinding.getEndpointReference()).andReturn(epr);
        }
        RMPolicyProvider pp = control.createMock(RMPolicyProvider.class);
        configuration.getProviders().add(pp);
    }
    
    private SOAPMessageContext setupContext(String action) {
        SOAPMessageContext ctx = control.createMock(SOAPMessageContext.class);
        AddressingPropertiesImpl maps = control.createMock(AddressingPropertiesImpl.class);
        expect(ctx.get(CLIENT_ADDRESSING_PROPERTIES_OUTBOUND)).andReturn(maps);
        maps.exposeAs(VersionTransformer.Names200408.WSA_NAMESPACE_NAME);
        expectLastCall();
        AttributedURIType uri = control.createMock(AttributedURIType.class);
        expect(maps.getAction()).andReturn(uri).times(2);
        expect(uri.getValue()).andReturn(action);
        return ctx;
    }
    
    private void setupQueue() {
        clientBinding = control.createMock(AbstractClientBinding.class);
        handler.clientBinding = clientBinding;
        ch = control.createMock(ConfigurationHelper.class);   
        store = control.createMock(RMStore.class); 
        queue = control.createMock(RetransmissionQueue.class);
        handler.initialise(ch, store, queue);
        bus = control.createMock(Bus.class);
        expect(clientBinding.getBus()).andReturn(bus);
        wqm = control.createMock(WorkQueueManager.class);
        expect(bus.getWorkQueueManager()).andReturn(wqm);
        wq = control.createMock(AutomaticWorkQueue.class);
        expect(wqm.getAutomaticWorkQueue()).andReturn(wq);
        queue.start(wq);
        expectLastCall();
    }
}
