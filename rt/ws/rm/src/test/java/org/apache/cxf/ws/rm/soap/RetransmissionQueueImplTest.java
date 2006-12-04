/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.cxf.ws.rm.soap;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import junit.framework.TestCase;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.RMManager;
import org.apache.cxf.ws.rm.RMMessageConstants;
import org.apache.cxf.ws.rm.RMProperties;
import org.apache.cxf.ws.rm.SequenceType;
import org.apache.cxf.ws.rm.SourceSequence;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;


/**
 * Test resend logic.
 */
public class RetransmissionQueueImplTest extends TestCase {

    private IMocksControl control;
    private RMManager manager;
    private Executor executor;
    private RetransmissionQueueImpl queue;
    private TestResender resender;
    private List<Message> messages =
        new ArrayList<Message>();
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
        manager = createMock(RMManager.class);
        queue = new RetransmissionQueueImpl(manager);
        resender = new TestResender();
        queue.replaceResender(resender);
        executor = createMock(Executor.class);
        
    }
    
    public void tearDown() {
        control.verify();
        queue.stop();
        messages.clear();
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
        Message message1 = setUpMessage("sequence1");
        Message message2 = setUpMessage("sequence2");
        Message message3 = setUpMessage("sequence1");
        
        ready();
        
        assertNotNull("expected resend candidate",
                      queue.cacheUnacknowledged(message1));
        assertEquals("expected non-empty unacked map", 
                     1,
                     queue.getUnacknowledged().size());
        List<RetransmissionQueueImpl.ResendCandidate> sequence1List = 
            queue.getUnacknowledged().get("sequence1");
        assertNotNull("expected non-null context list", sequence1List);
        assertSame("expected context list entry",
                   message1,
                   sequence1List.get(0).getMessage());

        assertNotNull("expected resend candidate",
                      queue.cacheUnacknowledged(message2));
        assertEquals("unexpected unacked map size", 
                     2,
                     queue.getUnacknowledged().size());
        List<RetransmissionQueueImpl.ResendCandidate> sequence2List = 
            queue.getUnacknowledged().get("sequence2");
        assertNotNull("expected non-null context list", sequence2List);
        assertSame("expected context list entry",
                   message2,
                   sequence2List.get(0).getMessage());
        
        assertNotNull("expected resend candidate",
                      queue.cacheUnacknowledged(message3));
        assertEquals("un expected unacked map size", 
                     2,
                     queue.getUnacknowledged().size());
        sequence1List = 
            queue.getUnacknowledged().get("sequence1");
        assertNotNull("expected non-null context list", sequence1List);
        assertSame("expected context list entry",
                   message3,
                   sequence1List.get(1).getMessage());
    }
    
    public void testPurgeAcknowledgedSome() {
        BigInteger[] messageNumbers = {BigInteger.TEN, BigInteger.ONE};
        SourceSequence sequence = setUpSequence("sequence1",
                                          messageNumbers, 
                                          new boolean[] {true, false});
        List<RetransmissionQueueImpl.ResendCandidate> sequenceList =
            new ArrayList<RetransmissionQueueImpl.ResendCandidate>();
        queue.getUnacknowledged().put("sequence1", sequenceList);
        Message message1 =
            setUpMessage("sequence1", messageNumbers[0]);
        sequenceList.add(queue.createResendCandidate(message1));
        Message message2 =
            setUpMessage("sequence1", messageNumbers[1]);
        sequenceList.add(queue.createResendCandidate(message2));
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
        Message message1 =
            setUpMessage("sequence1", messageNumbers[0]);
        sequenceList.add(queue.createResendCandidate(message1));
        Message message2 =
            setUpMessage("sequence1", messageNumbers[1]);
        sequenceList.add(queue.createResendCandidate(message2));
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
        Message message1 =
            setUpMessage("sequence1", messageNumbers[0], false);
        sequenceList.add(queue.createResendCandidate(message1));
        Message message2 =
            setUpMessage("sequence1", messageNumbers[1], false);
        sequenceList.add(queue.createResendCandidate(message2));
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
    
    public void xtestPopulate() {
  
        /*
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
        
        RMSoapHandler rmh = control.createMock(RMSoapHandler.class);
        MAPCodec wsah = control.createMock(MAPCodec.class);

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
        */
    }
    
    public void testResendInitiatorBackoffLogic() {
        Message message1 = setUpMessage("sequence1");
        Message message2 = setUpMessage("sequence2");
        Message message3 = setUpMessage("sequence1");
        
        ready();
        RetransmissionQueueImpl.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(message1);
        RetransmissionQueueImpl.ResendCandidate candidate2 =
            queue.cacheUnacknowledged(message2);
        RetransmissionQueueImpl.ResendCandidate candidate3 =
            queue.cacheUnacknowledged(message3);
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
        Message message1 = setUpMessage("sequence1");
        Message message2 = setUpMessage("sequence2");
        Message message3 = setUpMessage("sequence1");
        ready();
        RetransmissionQueueImpl.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(message1);
        RetransmissionQueueImpl.ResendCandidate candidate2 =
            queue.cacheUnacknowledged(message2);
        RetransmissionQueueImpl.ResendCandidate candidate3 =
            queue.cacheUnacknowledged(message3);
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
        Message message1 = setUpMessage("sequence1");
        Message message2 = setUpMessage("sequence2");
        Message message3 = setUpMessage("sequence1");
        ready();
        RetransmissionQueueImpl.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(message1);
        RetransmissionQueueImpl.ResendCandidate candidate2 =
            queue.cacheUnacknowledged(message2);
        RetransmissionQueueImpl.ResendCandidate candidate3 =
            queue.cacheUnacknowledged(message3);
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

    public void xtestResenderInitiatorNoRescheduleOnShutdown() {
        /*
        ready();
        
        queue.shutdown();
        queue.getResendInitiator().run();
        */
    }
    
    public void testDefaultResenderClient() throws Exception {
        doTestDefaultResender(true);
    }
    
    public void xtestDefaultResenderServer() throws Exception {
        doTestDefaultResender(false);
    }

    private void doTestDefaultResender(boolean isRequestor) throws Exception {
        Message message1 = setUpMessage("sequence1");
        queue.replaceResender(queue.getDefaultResender());
        ready();
        RetransmissionQueueImpl.ResendCandidate candidate1 =
            queue.cacheUnacknowledged(message1);
        RetransmissionQueueImpl.ResendCandidate[] allCandidates = {candidate1};
    
        // initial run => none due
        runInitiator();
    
        // single candidate due
        runInitiator(allCandidates);
        setUpDefaultResender(0, isRequestor, message1);
        allCandidates[0].run();
    }

    private Message setUpMessage(String sid) {
        return setUpMessage(sid, null);
    }

    private Message setUpMessage(String sid,
                                        BigInteger messageNumber) {
        return setUpMessage(sid, messageNumber, true);
    }

    private Message setUpMessage(String sid,
                                        BigInteger messageNumber,
                                        boolean storeSequence) {
        Message message =
            createMock(Message.class);
        if (storeSequence) {
            setUpSequenceType(message, sid, messageNumber);
        }
        messages.add(message);
        
        return message;
    }
   
    /*
    private void setupContextMessage(ObjectMessageContext context) throws Exception {
        SOAPMessage message = createMock(SOAPMessage.class);
        context.get("org.apache.cxf.bindings.soap.message");
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
    */

    private void ready() {
        control.replay();
        queue.start();
    }
    
    private void setUpDefaultResender(int i,
                                      boolean isRequestor,
                                      Message context) 
        throws Exception {
        assertTrue("too few contexts", i < messages.size());
        assertTrue("too few properties", i < properties.size());
        assertTrue("too few sequences", i < sequences.size());
        control.verify();
        control.reset();  
        
        messages.get(i).get(RMMessageConstants.RM_PROPERTIES_OUTBOUND);
        EasyMock.expectLastCall().andReturn(properties.get(i)).times(1);
        properties.get(i).getSequence();
        EasyMock.expectLastCall().andReturn(sequences.get(i)).times(1);
        
        messages.get(i).get(Message.REQUESTOR_ROLE);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(isRequestor));
        
        if (isRequestor) {
            Exchange ex = createMock(Exchange.class);
            messages.get(i).getExchange();
            EasyMock.expectLastCall().andReturn(ex);
            Conduit conduit = createMock(Conduit.class);
            ex.getConduit();
            EasyMock.expectLastCall().andReturn(conduit);
            conduit.send(messages.get(i));
            EasyMock.expectLastCall();
            OutputStream os = createMock(OutputStream.class);
            messages.get(i).getContent(OutputStream.class);
            EasyMock.expectLastCall().andReturn(os).times(2);
            ByteArrayOutputStream saved = createMock(ByteArrayOutputStream.class);
            messages.get(i).get(RMMessageConstants.SAVED_OUTPUT_STREAM);
            EasyMock.expectLastCall().andReturn(saved);
            byte[] content = "the saved message".getBytes();
            saved.toByteArray();
            EasyMock.expectLastCall().andReturn(content);
            os.write(EasyMock.isA(byte[].class), EasyMock.eq(0), EasyMock.eq(content.length));
            EasyMock.expectLastCall();
            os.flush();
            EasyMock.expectLastCall();
            os.close();
            EasyMock.expectLastCall(); 
        }
        control.replay();
    }

    /*
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
    */

    /*
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
    */

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
            Exchange ex = createMock(Exchange.class);
            dueCandidates[i].getMessage().getExchange();
            EasyMock.expectLastCall().andReturn(ex);
            Endpoint ep = createMock(Endpoint.class);
            ex.get(Endpoint.class);
            EasyMock.expectLastCall().andReturn(ep);
            ep.getExecutor();
            EasyMock.expectLastCall().andReturn(executor);
            executor.execute(dueCandidates[i]);
            EasyMock.expectLastCall();
        }
        
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
                       candidates[i].getMessage(),
                       resender.message);
            resender.clear();
        }
    }
    
    private SequenceType setUpSequenceType(Message message,
                                           String sid,
                                           BigInteger messageNumber) {
        RMProperties rmps = createMock(RMProperties.class);
        if (message != null) {
            message.get(RMMessageConstants.RM_PROPERTIES_OUTBOUND);
            EasyMock.expectLastCall().andReturn(rmps);
        } 
        properties.add(rmps);
        SequenceType sequence = createMock(SequenceType.class);
        if (message != null) {
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
            manager.getStore();
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
        Message message;
        boolean includeAckRequested;
        
        public void resend(Message ctx, boolean requestAcknowledge) {
            message = ctx;
            includeAckRequested = requestAcknowledge;
        }
        
        void clear() {
            message = null;
            includeAckRequested = false;            
        }
    };
}
