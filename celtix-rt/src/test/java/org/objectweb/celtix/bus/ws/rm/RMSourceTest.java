package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.math.BigInteger;


import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.workqueue.AutomaticWorkQueue;
import org.objectweb.celtix.workqueue.WorkQueueManager;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;
import org.objectweb.celtix.ws.rm.SequenceType;
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
    
    public void testSequenceAccess() throws IOException, SequenceFault {
        Identifier sid = s.generateSequenceIdentifier();
        SourceSequence seq = new SourceSequence(sid);
        assertNull(s.getCurrent());
        s.addSequence(seq);
        assertNotNull(s.getSequence(sid));
        assertNull(s.getCurrent());        
        SourceSequence anotherSeq = new SourceSequence(s.generateSequenceIdentifier());
        s.addSequence(anotherSeq);
        assertNull(s.getCurrent());
        assertEquals(2, s.getAllSequences().size());
        s.removeSequence(seq);
        assertEquals(1, s.getAllSequences().size());
        assertNull(s.getSequence(sid));
    }

    public void testCurrent() {
        Identifier sid = s.generateSequenceIdentifier();
        SourceSequence seq = new SourceSequence(sid);
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
    
    public void testAddUnacknowledged() {
        reset(handler);
        ObjectMessageContext ctx = new ObjectMessageContextImpl();  
        RMPropertiesImpl rmps = new RMPropertiesImpl();
        SequenceType seq = createNiceMock(SequenceType.class);
        Identifier sid = createNiceMock(Identifier.class);
        rmps.setSequence(seq);
        RMContextUtils.storeRMProperties(ctx, rmps, true);
        
        AbstractClientBinding binding = createNiceMock(AbstractClientBinding.class);
        handler.getBinding();
        expectLastCall().andReturn(binding);
        ObjectMessageContext clone =  new ObjectMessageContextImpl();
        binding.createObjectContext();
        expectLastCall().andReturn(clone);
        seq.getIdentifier();
        expectLastCall().andReturn(sid);
        sid.getValue();
        expectLastCall().andReturn("s1");
        
        replay(handler);
        replay(binding);
        replay(seq);
        replay(sid);
        
        s.addUnacknowledged(ctx);
        
        verify(handler);
        verify(binding);
        verify(seq);
        verify(sid);
    }

    
    public void testSetAcknowledged() throws NoSuchMethodException, IOException {
        Identifier sid1 = s.generateSequenceIdentifier();
        Identifier sid2 = s.generateSequenceIdentifier();
        SourceSequence seq = new SourceSequence(sid1, null, null, BigInteger.TEN, true);
        seq.setSource(s);
        s.addSequence(seq);
               
        SequenceAcknowledgement ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        ack.setIdentifier(sid1);        
        s.setAcknowledged(ack);
        assertSame(ack, seq.getAcknowledgement());
        ack.setIdentifier(sid2);
        s.setAcknowledged(ack);  
        
        reset(handler);
   
        ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        ack.setIdentifier(sid1);
        AcknowledgementRange range = 
            RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();

        range.setLower(BigInteger.ONE);
        range.setUpper(BigInteger.TEN);
        ack.getAcknowledgementRange().add(range);
        
        RMProxy proxy = createNiceMock(RMProxy.class);    
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.terminateSequence(seq);
        expectLastCall();
        
        replay(handler);
        replay(proxy);
        
        s.setAcknowledged(ack);
        
        verify(handler);
        verify(proxy);
        
        reset(handler);
        reset(proxy);
        
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.terminateSequence(seq);
        expectLastCall().andThrow(new IOException("can't terminate sequence"));
        
        replay(handler);
        replay(proxy);
        
        s.setAcknowledged(ack);
        
        verify(handler);
        verify(proxy);      
        
    }
    
    public void testShutdown() {
        s.shutdown();
    }
    
    private RMSource createSource(RMHandler h) {
        Bus bus = createNiceMock(Bus.class);
        h.getBus();
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
        c.getObject(RMAssertionType.class, "rmAssertion");
        expectLastCall().andReturn(null);
   
        replay(h);
        replay(bus);
        replay(wqm);
        replay(c);
        
        return new RMSource(h);
    }
}
