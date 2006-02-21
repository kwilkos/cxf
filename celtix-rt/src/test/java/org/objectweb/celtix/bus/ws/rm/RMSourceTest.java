package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

public class RMSourceTest extends TestCase {
    
    private RMHandler handler;
    
    public void setUp() {
        handler = createMock(RMHandler.class);
    }
    
    public void testRMSourceConstructor() {
        RMSource s = new RMSource(handler);
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
        RMSource s = new RMSource(handler);
        assertNotNull(s.getSourcePolicies());
        verify(handler);
        verify(c);
        
        
        reset(handler);
        reset(c);
        
        sp = createMock(SourcePolicyType.class);
        handler.getConfiguration();
        expectLastCall().andReturn(c);
        c.getObject(SourcePolicyType.class, "sourcePolicies");         
        expectLastCall().andReturn(sp);
        replay(handler);
        replay(c);  
        s = new RMSource(handler);
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
        RMSource s = new RMSource(handler);
        assertNotNull(s.getSequenceTerminationPolicy());
        verify(handler);
        verify(c);
    }
    
    public void testAddSequence() throws IOException, SequenceFault {
        RMSource s = new RMSource(handler);
        Identifier sid = s.generateSequenceIdentifier();
        Expires e = RMUtils.getWSRMFactory().createExpires();
        e.setValue(Sequence.PT0S);
        Sequence seq = new Sequence(sid, s, e);
        assertNull(s.getCurrent());
        s.addSequence(seq);
        assertSame(seq, s.getCurrent());
        Sequence anotherSeq = new Sequence(s.generateSequenceIdentifier(), s, e);
        s.addSequence(anotherSeq);
        assertSame(anotherSeq, s.getCurrent());        
    }

    
    public void testSetAcknowledged() throws NoSuchMethodException {
        RMSource s = new RMSource(handler);
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
}
