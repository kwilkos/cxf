package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;


import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;
import org.objectweb.celtix.ws.rm.SequenceType;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;
import org.objectweb.celtix.ws.rm.persistence.RMStore;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.expectLastCall;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;

public class RMSourceTest extends TestCase {
    
    private Bus bus;
    private BusLifeCycleManager lcm;
    private RMHandler handler;
    private RMSource s;
    private Configuration cfg;
    private IMocksControl control;
    
    public void setUp() {
        control = EasyMock.createNiceControl();
        handler = control.createMock(RMHandler.class);
        s = createSource(handler);
    }
    
    public void testRMSourceConstructor() {
        assertNotNull(s.getRetransmissionQueue());
        assertNull(s.getCurrent());
    }
    
    public void testGetSourcePolicies() {   
        SourcePolicyType sp = null;
        handler.getConfiguration();
        expectLastCall().andReturn(cfg);
        cfg.getObject(SourcePolicyType.class, "sourcePolicies");
        expectLastCall().andReturn(sp);
        
        control.replay();
        assertNotNull(s.getSourcePolicies());
        control.verify();
        
        control.reset();
        sp = control.createMock(SourcePolicyType.class);
        handler.getConfiguration();
        expectLastCall().andReturn(cfg);
        cfg.getObject(SourcePolicyType.class, "sourcePolicies");         
        expectLastCall().andReturn(sp);
        control.replay();
        assertNotNull(s.getSourcePolicies());
        control.verify();
    }
    
    public void testGetSequenceTerminationPolicies() {
        SourcePolicyType sp = null;
        handler.getConfiguration();
        expectLastCall().andReturn(cfg);
        cfg.getObject(SourcePolicyType.class, "sourcePolicies");
        expectLastCall().andReturn(sp); 
        
        control.replay();
        assertNotNull(s.getSequenceTerminationPolicy());
        control.verify();
    }
    
    public void testSequenceAccess() throws IOException, SequenceFault {
        RMStore store = control.createMock(RMStore.class);        
        expect(handler.getStore()).andReturn(store).times(3);
        store.createSourceSequence(EasyMock.isA(SourceSequence.class));
        expectLastCall().times(2);
        store.removeSourceSequence(EasyMock.isA(Identifier.class));

        control.replay();
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
        control.verify();
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
        ObjectMessageContext ctx = new ObjectMessageContextImpl();
        AddressingProperties maps = new AddressingPropertiesImpl();
        ctx.put(CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, maps);
        RMPropertiesImpl rmps = new RMPropertiesImpl();
        SequenceType seq = control.createMock(SequenceType.class);
        Identifier sid = control.createMock(Identifier.class);
        rmps.setSequence(seq);
        RMContextUtils.storeRMProperties(ctx, rmps, true);   
        SourceSequence ss = control.createMock(SourceSequence.class);
        RMMessage msg = control.createMock(RMMessage.class);
        AbstractClientBinding binding = control.createMock(AbstractClientBinding.class);
        handler.getBinding();
        expectLastCall().andReturn(binding);
        ObjectMessageContext clonedCtx =  new ObjectMessageContextImpl();
        binding.createObjectContext();
        expectLastCall().andReturn(clonedCtx);
        msg.getContext();        
        expectLastCall().andReturn(ctx);
        seq.getIdentifier();
        expectLastCall().andReturn(sid);
        RMStore store = control.createMock(RMStore.class);
        handler.getStore();
        expectLastCall().andReturn(store);
        store.persistOutgoing(ss, msg);
        expectLastCall();
        
        control.replay();
        s.addUnacknowledged(ss, msg);
        control.verify();
    }

    
    public void testSetAcknowledged() throws NoSuchMethodException, IOException {
        RMStore store = control.createMock(RMStore.class);        
        expect(handler.getStore()).andReturn(store);
        store.createSourceSequence(EasyMock.isA(SourceSequence.class));
        expectLastCall();
        
        control.replay();
        
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
        
        control.verify();
        
        control.reset();
   
        ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        ack.setIdentifier(sid1);
        AcknowledgementRange range = 
            RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();

        range.setLower(BigInteger.ONE);
        range.setUpper(BigInteger.TEN);
        ack.getAcknowledgementRange().add(range);
        
        RMProxy proxy = control.createMock(RMProxy.class);    
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.terminateSequence(seq);
        expectLastCall();
        
        control.replay();
        s.setAcknowledged(ack);
        control.verify();

        control.reset();
        
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.terminateSequence(seq);
        expectLastCall().andThrow(new IOException("can't terminate sequence"));
        
        control.replay();
        s.setAcknowledged(ack);
        control.verify();
        
    }
    
    public void testShutdown() {
        s.shutdown();
    }
    
    public void testRestore() {
        RMStore store = control.createMock(RMStore.class);        
        EasyMock.expect(handler.getStore()).andReturn(store);
        Configuration c = control.createMock(Configuration.class);
        EasyMock.expect(handler.getConfiguration()).andReturn(c);
        Configuration pc = control.createMock(Configuration.class);
        EasyMock.expect(c.getParent()).andReturn(pc);
        EasyMock.expect(pc.getId()).andReturn("endpoint"); 
        Identifier id = RMUtils.getWSRMFactory().createIdentifier();
        id.setValue("source1");
        SourceSequence ss = new SourceSequence(id);
        Collection<RMSourceSequence> sss = new ArrayList<RMSourceSequence>();
        sss.add(ss);
        EasyMock.expect(store.getSourceSequences("endpoint")).andReturn(sss);
        EasyMock.expect(handler.getStore()).andReturn(store);
        Collection<RMMessage> msgs = new ArrayList<RMMessage>();
        EasyMock.expect(store.getMessages(id, true)).andReturn(msgs);
        control.replay();
        s.restore();
        assertEquals(1, s.getAllSequences().size());
        control.verify();
    }
    
    private RMSource createSource(RMHandler h) {
        bus = control.createMock(Bus.class);
        h.getBus();
        expectLastCall().andReturn(bus); 
        lcm = control.createMock(BusLifeCycleManager.class);
        bus.getLifeCycleManager();
        expectLastCall().andReturn(lcm);
        cfg = control.createMock(Configuration.class);
        h.getConfiguration();
        expectLastCall().andReturn(cfg);
        cfg.getObject(RMAssertionType.class, "rmAssertion");
        expectLastCall().andReturn(null);
   
        control.replay();
        
        RMSource src = new RMSource(h);
        control.verify();
        control.reset();
        return src;
    }
}
