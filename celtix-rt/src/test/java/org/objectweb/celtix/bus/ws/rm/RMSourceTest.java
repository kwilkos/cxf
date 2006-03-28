package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;


import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.workqueue.AutomaticWorkQueue;
import org.objectweb.celtix.workqueue.WorkQueueManager;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

public class RMSourceTest extends TestCase {
    
    private RMHandler handler;
    private RMSource s;
    
    public void setUp() {
        handler = createMock(RMHandler.class);
        s = createSource(handler);
    }
    
    public void testRMSourceConstructor() {
        assertNotNull(s.getRetransmissionQueue());
        assertNull(s.getCurrent());
    }
    
    public void testGetSourcePolicies() {
        SourcePolicyType sp = null;
        Configuration c = createMock(Configuration.class);
        reset(handler);
        handler.getConfiguration();
        expectLastCall().andReturn(c);
        c.getObject(SourcePolicyType.class, "sourcePolicies");
        expectLastCall().andReturn(sp);
        replay(handler);
        replay(c);  
        assertNotNull(s.getSourcePolicies());
        verify(handler);
        verify(c);
        
        
        reset(handler);
        reset(c);

        s = createSource(handler);
        reset(handler);
        sp = createMock(SourcePolicyType.class);
        handler.getConfiguration();
        expectLastCall().andReturn(c);
        c.getObject(SourcePolicyType.class, "sourcePolicies");         
        expectLastCall().andReturn(sp);
        replay(handler);
        replay(c);  
        assertNotNull(s.getSourcePolicies());
        verify(handler);
        verify(c);        
    }
    
    public void testGetSequenceTerminationPolicies() {
        SourcePolicyType sp = null;
        Configuration c = createMock(Configuration.class);
        reset(handler);
        handler.getConfiguration();
        expectLastCall().andReturn(c);
        c.getObject(SourcePolicyType.class, "sourcePolicies");
        expectLastCall().andReturn(sp);
        replay(handler);
        replay(c);  
        assertNotNull(s.getSequenceTerminationPolicy());
        verify(handler);
        verify(c);
    }
    
    public void testAddSequence() throws IOException, SequenceFault {
        Identifier sid = s.generateSequenceIdentifier();
        Expires e = RMUtils.getWSRMFactory().createExpires();
        e.setValue(Sequence.PT0S);
        Sequence seq = new Sequence(sid, s, e);
        assertNull(s.getCurrent());
        s.addSequence(seq);
        assertNull(s.getCurrent());
        Sequence anotherSeq = new Sequence(s.generateSequenceIdentifier(), s, e);
        s.addSequence(anotherSeq);
        assertNull(s.getCurrent());
    }

    public void testCurrent() {
        Identifier sid = s.generateSequenceIdentifier();
        Sequence seq = new Sequence(sid, s, null);
        assertNull(s.getCurrent());
        Identifier inSid = s.generateSequenceIdentifier();
        assertNull(s.getCurrent(inSid));
        s.setCurrent(seq);
        assertNotNull(s.getCurrent());
        assertSame(seq, s.getCurrent());
        assertNull(s.getCurrent(inSid));
        s.setCurrent(inSid, seq);
        assertNotNull(s.getCurrent(inSid));
        assertSame(seq, s.getCurrent(inSid));
        assertNull(s.getCurrent(sid));
    }

    
    public void testSetAcknowledged() throws NoSuchMethodException {
        Identifier sid1 = s.generateSequenceIdentifier();
        Identifier sid2 = s.generateSequenceIdentifier();
        Expires e = RMUtils.getWSRMFactory().createExpires();
        e.setValue(Sequence.PT0S);
        Sequence seq = new Sequence(sid1, s, e); 
        s.addSequence(seq);
        
        SequenceAcknowledgement ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        ack.setIdentifier(sid1);        
        s.setAcknowledged(ack);
        assertSame(ack, seq.getAcknowledged());
        ack.setIdentifier(sid2);
        s.setAcknowledged(ack);   
    }
    
    private RMSource createSource(RMHandler h) {
        AbstractClientBinding binding = createNiceMock(AbstractClientBinding.class);
        h.getBinding();
        expectLastCall().andReturn(binding);
        Bus bus = createNiceMock(Bus.class);
        binding.getBus();
        expectLastCall().andReturn(bus);
        WorkQueueManager wqm = createNiceMock(WorkQueueManager.class);
        bus.getWorkQueueManager();
        expectLastCall().andReturn(wqm);
        AutomaticWorkQueue workQueue = createNiceMock(AutomaticWorkQueue.class);
        wqm.getAutomaticWorkQueue();
        expectLastCall().andReturn(workQueue);
        BusLifeCycleManager lcm = createNiceMock(BusLifeCycleManager.class);
        bus.getLifeCycleManager();
        expectLastCall().andReturn(lcm);
        Configuration c = createMock(Configuration.class);
        h.getConfiguration();
        expectLastCall().andReturn(c);
        // RMAssertionType rma = createMock(RMAssertionType.class);
        c.getObject(RMAssertionType.class, "rmAssertion");
        expectLastCall().andReturn(null);
        /*
        BaseRetransmissionInterval bri = createMock(BaseRetransmissionInterval.class);
        rma.getBaseRetransmissionInterval();
        expectLastCall().andReturn(bri);
        bri.getMilliseconds();
        expectLastCall().andReturn(new BigInteger("3000"));
        ExponentialBackoff eb = createMock(ExponentialBackoff.class);
        rma.getExponentialBackoff();
        expectLastCall().andReturn(eb);
        Map<QName, String> oa = new HashMap<QName, String>();
        oa.put(RetransmissionQueue.EXPONENTIAL_BACKOFF_BASE_ATTR, "2");
        eb.getOtherAttributes();
        expectLastCall().andReturn(oa);
        */
   
        replay(h);
        replay(binding);
        replay(bus);
        replay(wqm);
        replay(c);
        //replay(rma);
        //replay(bri);
        //replay(eb);
        
        return new RMSource(h);
    }
}
