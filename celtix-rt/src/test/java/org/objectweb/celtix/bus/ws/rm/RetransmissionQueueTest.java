package org.objectweb.celtix.bus.ws.rm;

import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.workqueue.WorkQueue;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.RMProperties;
import org.objectweb.celtix.ws.rm.SequenceType;

import static org.objectweb.celtix.bindings.JAXWSConstants.DATABINDING_CALLBACK_PROPERTY;
import static org.objectweb.celtix.context.ObjectMessageContext.REQUESTOR_ROLE_PROPERTY;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;
import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.RM_PROPERTIES_OUTBOUND;


/**
 * Test resend logic.
 */
public class RetransmissionQueueTest extends TestCase {

    private IMocksControl control;
    private RMHandler handler;
    private WorkQueue workQueue;
    private RetransmissionQueue queue;
    private TestResender resender;
    private List<ObjectMessageContext> contexts =
        new ArrayList<ObjectMessageContext>();
    private List<RMProperties> properties =
        new ArrayList<RMProperties>();
    private List<SequenceType> sequences =
        new ArrayList<SequenceType>();
    private List<Identifier> identifiers =
        new ArrayList<Identifier>();
    
    public void setUp() {
        control = EasyMock.createNiceControl();
        handler = control.createMock(RMHandler.class);
        queue = new RetransmissionQueue(handler);
        resender = new TestResender();
        queue.replaceResender(resender);
        workQueue = control.createMock(WorkQueue.class);
        workQueue.schedule(queue.getResendInitiator(), 
                           queue.getBaseRetransmissionInterval());
        EasyMock.expectLastCall();
    }
    
    public void tearDown() {
        control.verify();
        contexts.clear();
        properties.clear();
        sequences.clear();
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
        ready();
        
        assertNotNull("expected resend candidate",
                      queue.cacheUnacknowledged(context1));
        assertEquals("expected non-empty unacked map", 
                     1,
                     queue.getUnacknowledged().size());
        List<RetransmissionQueue.ResendCandidate> sequence1List = 
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
        List<RetransmissionQueue.ResendCandidate> sequence2List = 
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
        List<RetransmissionQueue.ResendCandidate> sequenceList =
            new ArrayList<RetransmissionQueue.ResendCandidate>();
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
        List<RetransmissionQueue.ResendCandidate> sequenceList =
            new ArrayList<RetransmissionQueue.ResendCandidate>();
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

    public void testCountUnacknowledged() {
        BigInteger[] messageNumbers = {BigInteger.TEN, BigInteger.ONE};
        SourceSequence sequence = setUpSequence("sequence1",
                                          messageNumbers, 
                                          null);
        List<RetransmissionQueue.ResendCandidate> sequenceList =
            new ArrayList<RetransmissionQueue.ResendCandidate>();
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
    
    public void testResendInitiatorBackoffLogic() {
        ObjectMessageContext context1 = setUpContext("sequence1");
        ObjectMessageContext context2 = setUpContext("sequence2");
        ObjectMessageContext context3 = setUpContext("sequence1");
        ready();
        RetransmissionQueue.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(context1);
        RetransmissionQueue.ResendCandidate candidate2 =
            queue.cacheUnacknowledged(context2);
        RetransmissionQueue.ResendCandidate candidate3 =
            queue.cacheUnacknowledged(context3);
        RetransmissionQueue.ResendCandidate[] allCandidates = 
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
        ready();
        RetransmissionQueue.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(context1);
        RetransmissionQueue.ResendCandidate candidate2 =
            queue.cacheUnacknowledged(context2);
        RetransmissionQueue.ResendCandidate candidate3 =
            queue.cacheUnacknowledged(context3);
        RetransmissionQueue.ResendCandidate[] allCandidates = 
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
        runInitiator(new RetransmissionQueue.ResendCandidate[] {candidate1, 
                                                                candidate2});

        runCandidates(allCandidates, expectAckRequested);

        // exponential backoff => none due
        runInitiator();

        // candidates 3 run belatedly => now due
        runInitiator(new RetransmissionQueue.ResendCandidate[] {candidate3});
        
        // exponential backoff => none due
        runInitiator();

        // candidates 1 & 2 now due
        runInitiator(new RetransmissionQueue.ResendCandidate[] {candidate1, 
                                                                candidate2});
    }
    
    public void testResendInitiatorResolvedLogic() {
        ObjectMessageContext context1 = setUpContext("sequence1");
        ObjectMessageContext context2 = setUpContext("sequence2");
        ObjectMessageContext context3 = setUpContext("sequence1");
        ready();
        RetransmissionQueue.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(context1);
        RetransmissionQueue.ResendCandidate candidate2 =
            queue.cacheUnacknowledged(context2);
        RetransmissionQueue.ResendCandidate candidate3 =
            queue.cacheUnacknowledged(context3);
        RetransmissionQueue.ResendCandidate[] allCandidates = 
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
        runInitiator(new RetransmissionQueue.ResendCandidate[] {candidate2});
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
        queue.replaceResender(queue.getDefaultResender());
        ready();
        RetransmissionQueue.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(context1);
        RetransmissionQueue.ResendCandidate[] allCandidates = {candidate1};

        // initial run => none due
        runInitiator();

        // single candidate due
        runInitiator(allCandidates);
        setUpDefaultResender(0, isRequestor);
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
            control.createMock(ObjectMessageContext.class);
        if (storeSequence) {
            setUpSequenceType(context, sid, messageNumber);
        }
        contexts.add(context);
        return context;
    }
    
    private void ready() {
        control.replay();
        queue.start(workQueue);
    }
    
    private void setUpDefaultResender(int i, boolean isRequestor) 
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
        AddressingPropertiesImpl maps =
            control.createMock(AddressingPropertiesImpl.class);
        contexts.get(i).get(REQUESTOR_ROLE_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(isRequestor)).times(2);
        contexts.get(i).get(SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
        EasyMock.expectLastCall().andReturn(maps);
        sequences.get(i).getIdentifier();
        EasyMock.expectLastCall().andReturn(identifiers.get(i));
        Transport transport = isRequestor
                              ? control.createMock(ClientTransport.class)
                              : control.createMock(ServerTransport.class);
        if (isRequestor) {
            handler.getClientTransport();
            EasyMock.expectLastCall().andReturn(transport).times(2);
        } else {
            handler.getServerTransport(); 
            EasyMock.expectLastCall().andReturn(transport).times(1);
        }
        AbstractBindingBase binding = 
            control.createMock(AbstractBindingBase.class);
        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding);
        HandlerInvoker handlerInvoker =
            control.createMock(HandlerInvoker.class);
        binding.createHandlerInvoker();
        EasyMock.expectLastCall().andReturn(handlerInvoker);
        AbstractBindingImpl bindingImpl = 
            control.createMock(AbstractBindingImpl.class);
        binding.getBindingImpl();
        EasyMock.expectLastCall().andReturn(bindingImpl).times(isRequestor
                                                               ? 7
                                                               : 5);
        bindingImpl.createBindingMessageContext(contexts.get(i));
        MessageContext bindingContext = 
            control.createMock(MessageContext.class);
        EasyMock.expectLastCall().andReturn(bindingContext);
        OutputStreamMessageContext outputStreamContext =
            control.createMock(OutputStreamMessageContext.class);
        transport.createOutputStreamContext(bindingContext);
        EasyMock.expectLastCall().andReturn(outputStreamContext);
        
        if (isRequestor) {
            setUpClientDispatch(handlerInvoker,
                                contexts.get(i),
                                bindingContext,
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
                              ObjectMessageContext objectContext,
                              MessageContext bindingContext,
                              OutputStreamMessageContext outputStreamContext,
                              AbstractBindingImpl bindingImpl,
                              Transport transport) throws Exception {
        handlerInvoker.invokeProtocolHandlers(true, bindingContext);
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
        InputStreamMessageContext inputStreamContext =
            control.createMock(InputStreamMessageContext.class);
        ((ClientTransport)transport).invoke(outputStreamContext);
        EasyMock.expectLastCall().andReturn(inputStreamContext);
        bindingImpl.read(inputStreamContext, bindingContext);
        EasyMock.expectLastCall();
        bindingImpl.hasFault(bindingContext);
        EasyMock.expectLastCall().andReturn(false);
        bindingImpl.unmarshal(bindingContext, objectContext, null);
        EasyMock.expectLastCall();
    }

    private void setUpServerDispatch(
                            MessageContext bindingContext,
                            OutputStreamMessageContext outputStreamContext) {
        DataBindingCallback callback =
            control.createMock(DataBindingCallback.class);
        bindingContext.get(DATABINDING_CALLBACK_PROPERTY);
        EasyMock.expectLastCall().andReturn(callback);
        OutputStream outputStream = control.createMock(OutputStream.class);
        outputStreamContext.getOutputStream();
        EasyMock.expectLastCall().andReturn(outputStream);
    }

    private void runInitiator() {
        runInitiator(null);
    }
    
    private void runInitiator(
                       RetransmissionQueue.ResendCandidate[] dueCandidates) {
        control.verify();
        control.reset();
        
        for (int i = 0; 
             dueCandidates != null && i < dueCandidates.length;
             i++) {
            workQueue.execute(dueCandidates[i]);
            EasyMock.expectLastCall();
        }
        workQueue.schedule(queue.getResendInitiator(), 
                           queue.getBaseRetransmissionInterval()); 
        EasyMock.expectLastCall();
        control.replay();
        queue.getResendInitiator().run();
    }
    
    private void runCandidates(
                          RetransmissionQueue.ResendCandidate[] candidates,
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
        RMProperties rmps = control.createMock(RMProperties.class);
        if (context != null) {
            context.get(RM_PROPERTIES_OUTBOUND);
            EasyMock.expectLastCall().andReturn(rmps);
        } 
        properties.add(rmps);
        SequenceType sequence = control.createMock(SequenceType.class);
        if (context != null) {
            rmps.getSequence();
            EasyMock.expectLastCall().andReturn(sequence);
        }
        if (messageNumber != null) {
            sequence.getMessageNumber();
            EasyMock.expectLastCall().andReturn(messageNumber);
        } else {
            Identifier id = control.createMock(Identifier.class);
            sequence.getIdentifier();
            EasyMock.expectLastCall().andReturn(id);
            id.getValue();
            EasyMock.expectLastCall().andReturn(sid);
            identifiers.add(id);
        }
        sequences.add(sequence);
        return sequence;
    }
        
    private SourceSequence setUpSequence(String sid, 
                                   BigInteger[] messageNumbers,
                                   boolean[] isAcked) {
        SourceSequence sequence = control.createMock(SourceSequence.class);
        Identifier id = control.createMock(Identifier.class);
        sequence.getIdentifier();
        EasyMock.expectLastCall().andReturn(id);
        id.getValue();
        EasyMock.expectLastCall().andReturn(sid);
        identifiers.add(id);
        for (int i = 0; isAcked != null && i < isAcked.length; i++) {
            sequence.isAcknowledged(messageNumbers[i]);
            EasyMock.expectLastCall().andReturn(isAcked[i]);
        }
        return sequence;
    }
    
    private static class TestResender implements RetransmissionQueue.Resender {
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
