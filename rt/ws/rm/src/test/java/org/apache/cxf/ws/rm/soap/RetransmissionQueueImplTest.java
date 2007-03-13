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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import junit.framework.TestCase;

import org.apache.cxf.message.Message;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.policy.builder.jaxb.JaxbAssertion;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.RMConstants;
import org.apache.cxf.ws.rm.RMManager;
import org.apache.cxf.ws.rm.RMMessageConstants;
import org.apache.cxf.ws.rm.RMProperties;
import org.apache.cxf.ws.rm.SequenceType;
import org.apache.cxf.ws.rm.SourceSequence;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.apache.cxf.ws.rm.policy.RMAssertion;
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
    private RMAssertion rma;
    
    public void setUp() {
        control = EasyMock.createNiceControl();
        manager = createMock(RMManager.class);
        queue = new RetransmissionQueueImpl(manager);
        resender = new TestResender();
        queue.replaceResender(resender);
        executor = createMock(Executor.class);
        rma = createMock(RMAssertion.class);
        assertNotNull(executor);
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
        ready(false);        
        assertNotNull("expected unacked map", queue.getUnacknowledged());
        assertEquals("expected empty unacked map", 
                     0,
                     queue.getUnacknowledged().size());
        
        queue = new RetransmissionQueueImpl(null);
        assertNull(queue.getManager());
        queue.setManager(manager);
        assertSame("Unexpected RMManager", manager, queue.getManager());        
    }
    
    public void testGetBaseRetranmissionIntervalFromPolicies() {
        Message message = createMock(Message.class);
        AssertionInfoMap aim = createMock(AssertionInfoMap.class);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(aim);
        AssertionInfo ai1 = createMock(AssertionInfo.class);
        AssertionInfo ai2 = createMock(AssertionInfo.class);
        AssertionInfo ai3 = createMock(AssertionInfo.class);
        AssertionInfo ai4 = createMock(AssertionInfo.class);
        Collection<AssertionInfo> ais = new ArrayList<AssertionInfo>();
        ais.add(ai1);
        ais.add(ai2);
        ais.add(ai3);
        ais.add(ai4);
        EasyMock.expect(aim.get(RMConstants.getRMAssertionQName())).andReturn(ais);
        JaxbAssertion ja1 = createMock(JaxbAssertion.class);
        EasyMock.expect(ai1.getAssertion()).andReturn(ja1);
        RMAssertion rma1 = createMock(RMAssertion.class);
        EasyMock.expect(ja1.getData()).andReturn(rma1);
        EasyMock.expect(rma1.getBaseRetransmissionInterval()).andReturn(null);
        JaxbAssertion ja2 = createMock(JaxbAssertion.class);
        EasyMock.expect(ai2.getAssertion()).andReturn(ja2);
        RMAssertion rma2 = createMock(RMAssertion.class);
        EasyMock.expect(ja2.getData()).andReturn(rma2);
        RMAssertion.BaseRetransmissionInterval bri2 = 
            createMock(RMAssertion.BaseRetransmissionInterval.class);
        EasyMock.expect(rma2.getBaseRetransmissionInterval()).andReturn(bri2);
        EasyMock.expect(bri2.getMilliseconds()).andReturn(null);
        JaxbAssertion ja3 = createMock(JaxbAssertion.class);
        EasyMock.expect(ai3.getAssertion()).andReturn(ja3);
        RMAssertion rma3 = createMock(RMAssertion.class);
        EasyMock.expect(ja3.getData()).andReturn(rma3);
        RMAssertion.BaseRetransmissionInterval bri3 = 
            createMock(RMAssertion.BaseRetransmissionInterval.class);
        EasyMock.expect(rma3.getBaseRetransmissionInterval()).andReturn(bri3);
        EasyMock.expect(bri3.getMilliseconds()).andReturn(new BigInteger("10000"));
        JaxbAssertion ja4 = createMock(JaxbAssertion.class);
        EasyMock.expect(ai4.getAssertion()).andReturn(ja4);
        RMAssertion rma4 = createMock(RMAssertion.class);
        EasyMock.expect(ja4.getData()).andReturn(rma4);
        RMAssertion.BaseRetransmissionInterval bri4 = 
            createMock(RMAssertion.BaseRetransmissionInterval.class);
        EasyMock.expect(rma4.getBaseRetransmissionInterval()).andReturn(bri4);
        EasyMock.expect(bri4.getMilliseconds()).andReturn(new BigInteger("5000"));
        
        control.replay();
        assertEquals("Unexpected value for base retransmission interval", 
                     5000, queue.getBaseRetransmissionInterval(message));
    }
    
    public void testGetBaseRetransmissionIntervalFromManager() {
        Message message = createMock(Message.class);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(null);
        EasyMock.expect(manager.getRMAssertion()).andReturn(rma);
        EasyMock.expect(rma.getBaseRetransmissionInterval()).andReturn(null);
        control.replay();
        assertEquals("Unexpected value for base retransmission interval", 
                     0, queue.getBaseRetransmissionInterval(message));
        control.verify();
        control.reset();
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(null);
        EasyMock.expect(manager.getRMAssertion()).andReturn(rma);
        RMAssertion.BaseRetransmissionInterval bri = createMock(RMAssertion.BaseRetransmissionInterval.class);
        EasyMock.expect(rma.getBaseRetransmissionInterval()).andReturn(bri);
        EasyMock.expect(bri.getMilliseconds()).andReturn(null);
        control.replay();
        assertEquals("Unexpected value for base retransmission interval", 
                     0, queue.getBaseRetransmissionInterval(message));
        control.verify();
        control.reset();
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(null);
        EasyMock.expect(manager.getRMAssertion()).andReturn(rma);
        EasyMock.expect(rma.getBaseRetransmissionInterval()).andReturn(bri);
        EasyMock.expect(bri.getMilliseconds()).andReturn(new BigInteger("7000"));
        control.replay();
        assertEquals("Unexpected value for base retransmission interval", 
                     7000, queue.getBaseRetransmissionInterval(message));
    }
    
    public void testUseExponentialBackoff() {
        Message message = createMock(Message.class);
        AssertionInfoMap aim = createMock(AssertionInfoMap.class);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(aim);
        AssertionInfo ai = createMock(AssertionInfo.class);
        Collection<AssertionInfo> ais = new ArrayList<AssertionInfo>();
        EasyMock.expect(aim.get(RMConstants.getRMAssertionQName())).andReturn(ais);
        ais.add(ai);
        JaxbAssertion ja = createMock(JaxbAssertion.class);
        EasyMock.expect(ai.getAssertion()).andReturn(ja);
        EasyMock.expect(ja.getData()).andReturn(rma);
        EasyMock.expect(rma.getExponentialBackoff()).andReturn(null);
        control.replay();
        assertTrue("Should not use exponential backoff", !queue.useExponentialBackoff(message));
        control.verify();
        control.reset();
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(null);
        EasyMock.expect(manager.getRMAssertion()).andReturn(rma);
        EasyMock.expect(rma.getExponentialBackoff()).andReturn(null);
        control.replay();
        assertTrue("Should not use exponential backoff", !queue.useExponentialBackoff(message));
        control.verify();
        control.reset();
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(null);
        EasyMock.expect(manager.getRMAssertion()).andReturn(rma);
        RMAssertion.ExponentialBackoff eb = createMock(RMAssertion.ExponentialBackoff.class);
        EasyMock.expect(rma.getExponentialBackoff()).andReturn(eb);
        control.replay();
        assertTrue("Should use exponential backoff", queue.useExponentialBackoff(message));        
    }
    
    public void testResendCandidateCtor() {
        Message message = createMock(Message.class);
        setupMessagePolicies(message);
        control.replay();
        long now = System.currentTimeMillis();
        RetransmissionQueueImpl.ResendCandidate candidate = queue.createResendCandidate(message);
        assertSame(message, candidate.getMessage());
        assertEquals(0, candidate.getResends());
        Date refDate = new Date(now + 5000);
        assertTrue(!candidate.getNext().before(refDate));
        refDate = new Date(now + 7000);
        assertTrue(!candidate.getNext().after(refDate));
        assertTrue(!candidate.isPending());
    }
    
    public void testResendCandidateAttempted() {
        Message message = createMock(Message.class);
        setupMessagePolicies(message);
        ready(true);
        long now = System.currentTimeMillis();
        RetransmissionQueueImpl.ResendCandidate candidate = queue.createResendCandidate(message);
        candidate.attempted();
        assertEquals(1, candidate.getResends());
        Date refDate = new Date(now + 15000);
        assertTrue(!candidate.getNext().before(refDate));
        refDate = new Date(now + 17000);
        assertTrue(!candidate.getNext().after(refDate));
        assertTrue(!candidate.isPending());        
    }
    
    public void testCacheUnacknowledged() {
        Message message1 = setUpMessage("sequence1");
        Message message2 = setUpMessage("sequence2");
        Message message3 = setUpMessage("sequence1");
        
        setupMessagePolicies(message1);
        setupMessagePolicies(message2);
        setupMessagePolicies(message3);
        
        ready(false);
        
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
        setupMessagePolicies(message1);        
        Message message2 =
            setUpMessage("sequence1", messageNumbers[1]);
        setupMessagePolicies(message2);
        ready(false);
        
        sequenceList.add(queue.createResendCandidate(message1));
        sequenceList.add(queue.createResendCandidate(message2));

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
        setupMessagePolicies(message1);        
        Message message2 =
            setUpMessage("sequence1", messageNumbers[1]);
        setupMessagePolicies(message2);        
        ready(false);
        
        sequenceList.add(queue.createResendCandidate(message1));
        sequenceList.add(queue.createResendCandidate(message2));

        queue.purgeAcknowledged(sequence);
        assertEquals("unexpected unacked map size", 
                     1,
                     queue.getUnacknowledged().size());
        assertEquals("unexpected unacked list size", 
                     2,
                     sequenceList.size());
    }
    
    public void testIsEmpty() {
        ready(false);
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
        setupMessagePolicies(message1);        
        Message message2 =
            setUpMessage("sequence1", messageNumbers[1], false);
        setupMessagePolicies(message1);
        ready(false);
        
        sequenceList.add(queue.createResendCandidate(message1));
        sequenceList.add(queue.createResendCandidate(message2));

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
        ready(false);

        assertEquals("unexpected unacked count", 
                     0,
                     queue.countUnacknowledged(sequence));
    }
    
    public void testStartStop() {
        control.replay();
        queue.start();
        queue.stop();
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
    
    private void setupMessagePolicies(Message message) {
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(null);
        EasyMock.expect(manager.getRMAssertion()).andReturn(rma).times(2);
        RMAssertion.BaseRetransmissionInterval bri = 
            createMock(RMAssertion.BaseRetransmissionInterval.class);
        EasyMock.expect(rma.getBaseRetransmissionInterval()).andReturn(bri);
        EasyMock.expect(bri.getMilliseconds()).andReturn(new BigInteger("5000"));
        RMAssertion.ExponentialBackoff eb = createMock(RMAssertion.ExponentialBackoff.class);
        EasyMock.expect(rma.getExponentialBackoff()).andReturn(eb);        
    }
    

    private void ready(boolean doStart) {
        control.replay();
        if (doStart) {
            queue.start();
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
