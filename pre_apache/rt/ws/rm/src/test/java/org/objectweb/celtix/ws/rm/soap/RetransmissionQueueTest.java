package org.objectweb.celtix.ws.rm.soap;

import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.ServerDataBindingCallback;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.workqueue.WorkQueue;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.ws.addressing.soap.MAPCodec;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.Names;
import org.objectweb.celtix.ws.rm.RMProperties;
import org.objectweb.celtix.ws.rm.SequenceType;
import org.objectweb.celtix.ws.rm.SourceSequence;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMStore;

import static org.objectweb.celtix.bindings.JAXWSConstants.DATABINDING_CALLBACK_PROPERTY;
import static org.objectweb.celtix.context.ObjectMessageContext.REQUESTOR_ROLE_PROPERTY;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;
import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.RM_PROPERTIES_OUTBOUND;


/**
 * Test resend logic.
 */
public class RetransmissionQueueTest extends TestCase {

    private IMocksControl control;
    private PersistenceHandler handler;
    private WorkQueue workQueue;
    private RetransmissionQueueImpl queue;
    private TestResender resender;
    private List<ObjectMessageContext> contexts =
        new ArrayList<ObjectMessageContext>();
    private List<RMProperties> properties =
        new ArrayList<RMProperties>();
    private List<SequenceType> sequences =
        new ArrayList<SequenceType>();
    private List<Identifier> identifiers =
        new ArrayList<Identifier>();
    private List<Object> mocks =
        new ArrayList<Object>();
    
    public void setUp() {
        control = EasyMock.createNiceControl();
        handler = createMock(PersistenceHandler.class);
        queue = new RetransmissionQueueImpl(handler);
        resender = new TestResender();
        queue.replaceResender(resender);
        workQueue = createMock(WorkQueue.class);
    }
    
    public void tearDown() {
        control.verify();
        contexts.clear();
        properties.clear();
        sequences.clear();
        mocks.clear();
        control.reset();
    }
    
    public void testCtor() {
        ready();
        assertNotNull("expected unacked map", queue.getUnacknowledged());
        assertEquals("expected empty unacked map", 
                     0,
                     queue.getUnacknowledged().size());
        assertEquals("unexpected base retransmission interval",
                     3000L,
                     queue.getBaseRetransmissionInterval());
        assertEquals("unexpected exponential backoff",
                     2,
                     queue.getExponentialBackoff());
    }
    
    public void testCacheUnacknowledged() {
        ObjectMessageContext context1 = setUpContext("sequence1");
        ObjectMessageContext context2 = setUpContext("sequence2");
        ObjectMessageContext context3 = setUpContext("sequence1");
        
        setupContextMAPs(context1);
        setupContextMAPs(context2);
        setupContextMAPs(context3);
        
        ready();
        
        assertNotNull("expected resend candidate",
                      queue.cacheUnacknowledged(context1));
        assertEquals("expected non-empty unacked map", 
                     1,
                     queue.getUnacknowledged().size());
        List<RetransmissionQueueImpl.ResendCandidate> sequence1List = 
            queue.getUnacknowledged().get("sequence1");
        assertNotNull("expected non-null context list", sequence1List);
        assertSame("expected context list entry",
                   context1,
                   sequence1List.get(0).getContext());

        assertNotNull("expected resend candidate",
                      queue.cacheUnacknowledged(context2));
        assertEquals("unexpected unacked map size", 
                     2,
                     queue.getUnacknowledged().size());
        List<RetransmissionQueueImpl.ResendCandidate> sequence2List = 
            queue.getUnacknowledged().get("sequence2");
        assertNotNull("expected non-null context list", sequence2List);
        assertSame("expected context list entry",
                   context2,
                   sequence2List.get(0).getContext());
        
        assertNotNull("expected resend candidate",
                      queue.cacheUnacknowledged(context3));
        assertEquals("un expected unacked map size", 
                     2,
                     queue.getUnacknowledged().size());
        sequence1List = 
            queue.getUnacknowledged().get("sequence1");
        assertNotNull("expected non-null context list", sequence1List);
        assertSame("expected context list entry",
                   context3,
                   sequence1List.get(1).getContext());
    }
    
    public void testPurgeAcknowledgedSome() {
        BigInteger[] messageNumbers = {BigInteger.TEN, BigInteger.ONE};
        SourceSequence sequence = setUpSequence("sequence1",
                                          messageNumbers, 
                                          new boolean[] {true, false});
        List<RetransmissionQueueImpl.ResendCandidate> sequenceList =
            new ArrayList<RetransmissionQueueImpl.ResendCandidate>();
        queue.getUnacknowledged().put("sequence1", sequenceList);
        ObjectMessageContext context1 =
            setUpContext("sequence1", messageNumbers[0]);
        sequenceList.add(queue.createResendCandidate(context1));
        ObjectMessageContext context2 =
            setUpContext("sequence1", messageNumbers[1]);
        sequenceList.add(queue.createResendCandidate(context2));
        ready();

        queue.purgeAcknowledged(sequence);
        assertEquals("unexpected unacked map size", 
                     1,
                     queue.getUnacknowledged().size());
        assertEquals("unexpected unacked list size", 
                     1,
                     sequenceList.size());
    }
    
    public void testPurgeAcknowledgedNone() {
        BigInteger[] messageNumbers = {BigInteger.TEN, BigInteger.ONE};
        SourceSequence sequence = setUpSequence("sequence1",
                                           messageNumbers, 
                                           new boolean[] {false, false});
        List<RetransmissionQueueImpl.ResendCandidate> sequenceList =
            new ArrayList<RetransmissionQueueImpl.ResendCandidate>();
        queue.getUnacknowledged().put("sequence1", sequenceList);
        ObjectMessageContext context1 =
            setUpContext("sequence1", messageNumbers[0]);
        sequenceList.add(queue.createResendCandidate(context1));
        ObjectMessageContext context2 =
            setUpContext("sequence1", messageNumbers[1]);
        sequenceList.add(queue.createResendCandidate(context2));
        ready();

        queue.purgeAcknowledged(sequence);
        assertEquals("unexpected unacked map size", 
                     1,
                     queue.getUnacknowledged().size());
        assertEquals("unexpected unacked list size", 
                     2,
                     sequenceList.size());
    }
    
    public void testIsEmpty() {
        ready();
        assertTrue("queue is not empty" , queue.isEmpty());
    }

    public void testCountUnacknowledged() {
        BigInteger[] messageNumbers = {BigInteger.TEN, BigInteger.ONE};
        SourceSequence sequence = setUpSequence("sequence1",
                                          messageNumbers, 
                                          null);
        List<RetransmissionQueueImpl.ResendCandidate> sequenceList =
            new ArrayList<RetransmissionQueueImpl.ResendCandidate>();
        
        queue.getUnacknowledged().put("sequence1", sequenceList);
        ObjectMessageContext context1 =
            setUpContext("sequence1", messageNumbers[0], false);
        sequenceList.add(queue.createResendCandidate(context1));
        ObjectMessageContext context2 =
            setUpContext("sequence1", messageNumbers[1], false);
        sequenceList.add(queue.createResendCandidate(context2));
        ready();

        assertEquals("unexpected unacked count", 
                     2,
                     queue.countUnacknowledged(sequence));
        assertTrue("queue is empty", !queue.isEmpty());
    }
    
    public void testCountUnacknowledgedUnknownSequence() {
        BigInteger[] messageNumbers = {BigInteger.TEN, BigInteger.ONE};
        SourceSequence sequence = setUpSequence("sequence1",
                                          messageNumbers, 
                                          null);
        ready();

        assertEquals("unexpected unacked count", 
                     0,
                     queue.countUnacknowledged(sequence));
    }
    
    public void testPopulate() {
  
        Collection<SourceSequence> sss = new ArrayList<SourceSequence>();
        Collection<RMMessage> msgs = new ArrayList<RMMessage>();
        // List<Handler> handlerChain = new ArrayList<Handler>();
            
        RMStore store = createMock(RMStore.class);
        handler.getStore();
        EasyMock.expectLastCall().andReturn(store);   
        SourceSequence ss = control.createMock(SourceSequence.class);
        sss.add(ss);
        Identifier id = control.createMock(Identifier.class);
        ss.getIdentifier();
        EasyMock.expectLastCall().andReturn(id); 
        RMMessage msg = control.createMock(RMMessage.class);
        msgs.add(msg);
        store.getMessages(id, true);
        EasyMock.expectLastCall().andReturn(msgs); 
        MessageContext context = control.createMock(MessageContext.class);
        msg.getContext();
        EasyMock.expectLastCall().andReturn(context);
        /*
        AbstractBindingBase binding = control.createMock(AbstractBindingBase.class);
        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding).times(2);
        AbstractBindingImpl abi = control.createMock(AbstractBindingImpl.class);
        binding.getBindingImpl();
        EasyMock.expectLastCall().andReturn(abi).times(2);
        */
        RMSoapHandler rmh = control.createMock(RMSoapHandler.class);
        MAPCodec wsah = control.createMock(MAPCodec.class);
        /*
        handlerChain.add(rmh);
        handlerChain.add(wsah);
        abi.getPostProtocolSystemHandlers();
        EasyMock.expectLastCall().andReturn(handlerChain).times(2);
        */
        handler.getWsaSOAPHandler();
        EasyMock.expectLastCall().andReturn(wsah);
        handler.getRMSoapHandler();
        EasyMock.expectLastCall().andReturn(rmh);
        RMProperties rmps = control.createMock(RMProperties.class);
        rmh.unmarshalRMProperties(null);
        EasyMock.expectLastCall().andReturn(rmps);
        AddressingProperties maps = control.createMock(AddressingProperties.class);
        wsah.unmarshalMAPs(null);
        EasyMock.expectLastCall().andReturn(maps);
        SequenceType st = control.createMock(SequenceType.class);
        rmps.getSequence();
        EasyMock.expectLastCall().andReturn(st);
        st.getIdentifier();
        EasyMock.expectLastCall().andReturn(id);
        id.getValue();
        EasyMock.expectLastCall().andReturn("sequence1");
        ready();
        
        queue.populate(sss);
        
        assertTrue("queue is empty", !queue.isEmpty());    
    }
    
    public void testResendInitiatorBackoffLogic() {
        ObjectMessageContext context1 = setUpContext("sequence1");
        ObjectMessageContext context2 = setUpContext("sequence2");
        ObjectMessageContext context3 = setUpContext("sequence1");
        setupContextMAPs(context1);
        setupContextMAPs(context2);
        setupContextMAPs(context3);
        ready();
        RetransmissionQueueImpl.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(context1);
        RetransmissionQueueImpl.ResendCandidate candidate2 =
            queue.cacheUnacknowledged(context2);
        RetransmissionQueueImpl.ResendCandidate candidate3 =
            queue.cacheUnacknowledged(context3);
        RetransmissionQueueImpl.ResendCandidate[] allCandidates = 
        {candidate1, candidate2, candidate3};
        boolean [] expectAckRequested = {true, true, false};

        // initial run => none due
        runInitiator();

        // all 3 candidates due
        runInitiator(allCandidates);
        runCandidates(allCandidates, expectAckRequested);        
                        
        // exponential backoff => none due
        runInitiator();
        
        // all 3 candidates due
        runInitiator(allCandidates);
        runCandidates(allCandidates, expectAckRequested);

        for (int i = 0; i < 3; i++) {
            // exponential backoff => none due
            runInitiator();
        }

        // all 3 candidates due
        runInitiator(allCandidates);
        runCandidates(allCandidates, expectAckRequested);
        
        for (int i = 0; i < 7; i++) {
            // exponential backoff => none due
            runInitiator();
        }
        
        // all 3 candidates due
        runInitiator(allCandidates);
        runCandidates(allCandidates, expectAckRequested);
    }


    public void testResendInitiatorDueLogic() {
        ObjectMessageContext context1 = setUpContext("sequence1");
        ObjectMessageContext context2 = setUpContext("sequence2");
        ObjectMessageContext context3 = setUpContext("sequence1");
        setupContextMAPs(context1);
        setupContextMAPs(context2);
        setupContextMAPs(context3);
        ready();
        RetransmissionQueueImpl.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(context1);
        RetransmissionQueueImpl.ResendCandidate candidate2 =
            queue.cacheUnacknowledged(context2);
        RetransmissionQueueImpl.ResendCandidate candidate3 =
            queue.cacheUnacknowledged(context3);
        RetransmissionQueueImpl.ResendCandidate[] allCandidates = 
        {candidate1, candidate2, candidate3};
        boolean [] expectAckRequested = {true, true, false};

        // initial run => none due
        runInitiator();

        // all 3 candidates due
        runInitiator(allCandidates);
                
        // all still pending => none due
        runInitiator();
        
        candidate1.run();
        candidate2.run();
        
        // exponential backoff => none due
        runInitiator();
        
        // candidates 1 & 2 run => only these due
        runInitiator(new RetransmissionQueueImpl.ResendCandidate[] {candidate1, candidate2});

        runCandidates(allCandidates, expectAckRequested);

        // exponential backoff => none due
        runInitiator();

        // candidates 3 run belatedly => now due
        runInitiator(new RetransmissionQueueImpl.ResendCandidate[] {candidate3});
        
        // exponential backoff => none due
        runInitiator();

        // candidates 1 & 2 now due
        runInitiator(new RetransmissionQueueImpl.ResendCandidate[] {candidate1, candidate2});
    }
    
    public void testResendInitiatorResolvedLogic() {
        ObjectMessageContext context1 = setUpContext("sequence1");
        ObjectMessageContext context2 = setUpContext("sequence2");
        ObjectMessageContext context3 = setUpContext("sequence1");
        setupContextMAPs(context1);
        setupContextMAPs(context2);
        setupContextMAPs(context3);
        ready();
        RetransmissionQueueImpl.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(context1);
        RetransmissionQueueImpl.ResendCandidate candidate2 =
            queue.cacheUnacknowledged(context2);
        RetransmissionQueueImpl.ResendCandidate candidate3 =
            queue.cacheUnacknowledged(context3);
        RetransmissionQueueImpl.ResendCandidate[] allCandidates = 
        {candidate1, candidate2, candidate3};
        boolean [] expectAckRequested = {true, true, false};
        
        // initial run => none due
        runInitiator();

        // all 3 candidates due
        runInitiator(allCandidates);
        runCandidates(allCandidates, expectAckRequested);

        // exponential backoff => none due
        runInitiator();
        
        candidate1.resolved();
        candidate3.resolved();
        
        // candidates 1 & 3 resolved => only candidate2 due
        runInitiator(new RetransmissionQueueImpl.ResendCandidate[] {candidate2});
    }
    
    public void testResenderInitiatorReschedule() {
        ready();
        
        runInitiator();
    }

    public void testResenderInitiatorNoRescheduleOnShutdown() {
        ready();
        
        queue.shutdown();
        queue.getResendInitiator().run();
    }
    
    public void testDefaultResenderClient() throws Exception {
        doTestDefaultResender(true);
    }
    
    public void testDefaultResenderServer() throws Exception {
        doTestDefaultResender(false);
    }

    private void doTestDefaultResender(boolean isRequestor) throws Exception {
        ObjectMessageContext context1 = setUpContext("sequence1");
        setupContextMAPs(context1);
        queue.replaceResender(queue.getDefaultResender());
        ready();
        RetransmissionQueueImpl.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(context1);
        RetransmissionQueueImpl.ResendCandidate[] allCandidates = {candidate1};
    
        // initial run => none due
        runInitiator();
    
        // single candidate due
        runInitiator(allCandidates);
        setUpDefaultResender(0, isRequestor, context1);
        allCandidates[0].run();
    }

    private ObjectMessageContext setUpContext(String sid) {
        return setUpContext(sid, null);
    }

    private ObjectMessageContext setUpContext(String sid,
                                        BigInteger messageNumber) {
        return setUpContext(sid, messageNumber, true);
    }

    private ObjectMessageContext setUpContext(String sid,
                                        BigInteger messageNumber,
                                        boolean storeSequence) {
        ObjectMessageContext context =
            createMock(ObjectMessageContext.class);
        if (storeSequence) {
            setUpSequenceType(context, sid, messageNumber);
        }
        contexts.add(context);
        
        return context;
    }
    
    private void setupContextMAPs(ObjectMessageContext context) {
        AddressingPropertiesImpl maps = createMock(AddressingPropertiesImpl.class);
        context.get(CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
        EasyMock.expectLastCall().andReturn(maps);
    }
    
    private void setupContextMessage(ObjectMessageContext context) throws Exception {
        SOAPMessage message = createMock(SOAPMessage.class);
        context.get("org.objectweb.celtix.bindings.soap.message");
        EasyMock.expectLastCall().andReturn(message);
        SOAPPart part = createMock(SOAPPart.class);
        message.getSOAPPart();
        EasyMock.expectLastCall().andReturn(part);
        SOAPEnvelope env = createMock(SOAPEnvelope.class);
        part.getEnvelope();
        EasyMock.expectLastCall().andReturn(env);
        SOAPHeader header = createMock(SOAPHeader.class);
        env.getHeader();
        EasyMock.expectLastCall().andReturn(header).times(2);
        Iterator headerElements = createMock(Iterator.class);
        header.examineAllHeaderElements();
        EasyMock.expectLastCall().andReturn(headerElements);
        
        // RM header element
        headerElements.hasNext();
        EasyMock.expectLastCall().andReturn(true);
        SOAPHeaderElement headerElement = createMock(SOAPHeaderElement.class);
        headerElements.next();
        EasyMock.expectLastCall().andReturn(headerElement);
        Name headerName = createMock(Name.class);
        headerElement.getElementName();
        EasyMock.expectLastCall().andReturn(headerName);
        headerName.getURI();
        EasyMock.expectLastCall().andReturn(Names.WSRM_NAMESPACE_NAME);
        headerElement.detachNode();
        EasyMock.expectLastCall();
        
        // non-RM header element
        headerElements.hasNext();
        EasyMock.expectLastCall().andReturn(true);
        headerElements.next();
        EasyMock.expectLastCall().andReturn(headerElement);
        headerElement.getElementName();
        EasyMock.expectLastCall().andReturn(headerName);
        headerName.getURI();
        EasyMock.expectLastCall().andReturn(Names.WSA_NAMESPACE_NAME);

        headerElements.hasNext();
        EasyMock.expectLastCall().andReturn(false);
    }

    private void ready() {
        control.replay();
        queue.start(workQueue);
    }
    
    private void setUpDefaultResender(int i,
                                      boolean isRequestor,
                                      ObjectMessageContext context) 
        throws Exception {
        assertTrue("too few contexts", i < contexts.size());
        assertTrue("too few properties", i < properties.size());
        assertTrue("too few sequences", i < sequences.size());
        control.verify();
        control.reset();
        
        contexts.get(i).get(RM_PROPERTIES_OUTBOUND);
        EasyMock.expectLastCall().andReturn(properties.get(i)).times(2);
        properties.get(i).getSequence();
        EasyMock.expectLastCall().andReturn(sequences.get(i)).times(2);
        
        setupContextMessage(context);
        
        contexts.get(i).get(REQUESTOR_ROLE_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(isRequestor));
        sequences.get(i).getIdentifier();
        EasyMock.expectLastCall().andReturn(identifiers.get(i));
        Transport transport = isRequestor
                              ? createMock(ClientTransport.class)
                              : createMock(ServerTransport.class);
        if (isRequestor) {
            handler.getClientTransport();
            EasyMock.expectLastCall().andReturn(transport).times(2);
        } else {
            handler.getServerTransport(); 
            EasyMock.expectLastCall().andReturn(transport).times(1);
        }
        AbstractBindingBase binding = 
            createMock(AbstractBindingBase.class);
        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding);
        HandlerInvoker handlerInvoker =
            createMock(HandlerInvoker.class);
        binding.createHandlerInvoker();
        EasyMock.expectLastCall().andReturn(handlerInvoker);
        AbstractBindingImpl bindingImpl = 
            createMock(AbstractBindingImpl.class);
        binding.getBindingImpl();
        EasyMock.expectLastCall().andReturn(bindingImpl).times(isRequestor
                                                               ? 6
                                                               : 5);
        bindingImpl.createBindingMessageContext(contexts.get(i));
        MessageContext bindingContext = 
            createMock(MessageContext.class);
        EasyMock.expectLastCall().andReturn(bindingContext);
        
        OutputStreamMessageContext outputStreamContext =
            createMock(OutputStreamMessageContext.class);
        transport.createOutputStreamContext(bindingContext);
        EasyMock.expectLastCall().andReturn(outputStreamContext);
        
        if (isRequestor) {
            
            setUpClientDispatch(handlerInvoker,
                                binding,
                                outputStreamContext,
                                bindingImpl,
                                transport);
        } else {
            setUpServerDispatch(bindingContext, outputStreamContext);
        }
        
        control.replay();
    }

    private void setUpClientDispatch(
                              HandlerInvoker handlerInvoker,
                              AbstractBindingBase binding,
                              OutputStreamMessageContext outputStreamContext,
                              AbstractBindingImpl bindingImpl,
                              Transport transport) throws Exception {
             
        InputStreamMessageContext inputStreamContext =
            createMock(InputStreamMessageContext.class);
        ((ClientTransport)transport).invoke(outputStreamContext);
        EasyMock.expectLastCall().andReturn(inputStreamContext);        
        binding.getBindingImpl();
        EasyMock.expectLastCall().andReturn(bindingImpl); 
        bindingImpl.createBindingMessageContext(inputStreamContext);
        MessageContext bindingContext = 
            control.createMock(MessageContext.class);
        EasyMock.expectLastCall().andReturn(bindingContext);        
        bindingImpl.read(inputStreamContext, bindingContext);
        EasyMock.expectLastCall();        
        handlerInvoker.invokeProtocolHandlers(true, bindingContext);
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);        
        ObjectMessageContext objectContext = control.createMock(ObjectMessageContext.class);
        binding.createObjectContext();
        EasyMock.expectLastCall().andReturn(objectContext);        
        bindingImpl.hasFault(bindingContext);
        EasyMock.expectLastCall().andReturn(false);        
        bindingImpl.unmarshal(bindingContext, objectContext, null);
        EasyMock.expectLastCall();
    }

    private void setUpServerDispatch(
                            MessageContext bindingContext,
                            OutputStreamMessageContext outputStreamContext) {
        DataBindingCallback callback =
            createMock(ServerDataBindingCallback.class);
        bindingContext.get(DATABINDING_CALLBACK_PROPERTY);
        EasyMock.expectLastCall().andReturn(callback);
        OutputStream outputStream = createMock(OutputStream.class);
        outputStreamContext.getOutputStream();
        EasyMock.expectLastCall().andReturn(outputStream);
    }

    private void runInitiator() {
        runInitiator(null);
    }
    
    private void runInitiator(
                       RetransmissionQueueImpl.ResendCandidate[] dueCandidates) {
        control.verify();
        control.reset();
        
        for (int i = 0; 
             dueCandidates != null && i < dueCandidates.length;
             i++) {
            workQueue.execute(dueCandidates[i]);
            EasyMock.expectLastCall();
        }
        /*
        workQueue.schedule(queue.getResendInitiator(), 
                           queue.getBaseRetransmissionInterval());         
        EasyMock.expectLastCall();
        */
        control.replay();
        queue.getResendInitiator().run();
    }
    
    private void runCandidates(
                          RetransmissionQueueImpl.ResendCandidate[] candidates,
                          boolean[] expectAckRequested) {
        for (int i = 0; i < candidates.length; i++) {
            candidates[i].run();
            assertEquals("unexpected request acknowledge",
                         expectAckRequested[i],
                         resender.includeAckRequested);
            assertSame("unexpected context",
                       candidates[i].getContext(),
                       resender.context);
            resender.clear();
        }
    }
    
    private SequenceType setUpSequenceType(ObjectMessageContext context,
                                           String sid,
                                           BigInteger messageNumber) {
        RMProperties rmps = createMock(RMProperties.class);
        if (context != null) {
            context.get(RM_PROPERTIES_OUTBOUND);
            EasyMock.expectLastCall().andReturn(rmps);
        } 
        properties.add(rmps);
        SequenceType sequence = createMock(SequenceType.class);
        if (context != null) {
            rmps.getSequence();
            EasyMock.expectLastCall().andReturn(sequence);
        }
        if (messageNumber != null) {
            sequence.getMessageNumber();
            EasyMock.expectLastCall().andReturn(messageNumber);
        } else {
            Identifier id = createMock(Identifier.class);
            sequence.getIdentifier();
            EasyMock.expectLastCall().andReturn(id);
            id.getValue();
            EasyMock.expectLastCall().andReturn(sid);
            identifiers.add(id);
        }
        sequences.add(sequence);
        return sequence;
    }
    
    @SuppressWarnings("unchecked")
    private SourceSequence setUpSequence(String sid, 
                                   BigInteger[] messageNumbers,
                                   boolean[] isAcked) {
        SourceSequence sequence = createMock(SourceSequence.class);
        Identifier id = createMock(Identifier.class);
        sequence.getIdentifier();
        EasyMock.expectLastCall().andReturn(id);
        id.getValue();
        EasyMock.expectLastCall().andReturn(sid);
        identifiers.add(id);
        boolean includesAcked = false;
        for (int i = 0; isAcked != null && i < isAcked.length; i++) {
            sequence.isAcknowledged(messageNumbers[i]);
            EasyMock.expectLastCall().andReturn(isAcked[i]);
            if (isAcked[i]) {
                includesAcked = true;
            }
        }
        if (includesAcked) {
            sequence.getIdentifier();
            EasyMock.expectLastCall().andReturn(id);
            RMStore store = createMock(RMStore.class);
            handler.getStore();
            EasyMock.expectLastCall().andReturn(store);
        }
        return sequence;
    }
    
    /**
     * Creates a mock object ensuring it remains referenced, so as to
     * avoid garbage collection and attendant issues with finalizer
     * calls on mocks.
     * 
     * @param toMock the class to mock up
     * @return the mock object
     */
    <T> T createMock(Class<T> toMock) {
        T ret = control.createMock(toMock);
        mocks.add(ret);
        return ret;
    }
    
    private static class TestResender implements RetransmissionQueueImpl.Resender {
        ObjectMessageContext context;
        boolean includeAckRequested;
        
        public void resend(ObjectMessageContext ctx, boolean requestAcknowledge) {
            context = ctx;
            includeAckRequested = requestAcknowledge;
        }
        
        void clear() {
            context = null;
            includeAckRequested = false;            
        }
    };
}
