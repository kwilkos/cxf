package org.objectweb.celtix.bus.ws.rm;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;

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
        c.getObject("sourcePolicies");
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
        c.getObject("sourcePolicies");         
        expectLastCall().andReturn(sp);
        replay(handler);
        replay(c);  
        s = new RMSource(handler);
        assertNotNull(s.getSourcePolicies());
        verify(handler);
        verify(c);        
    }
    
    public void testCreateSequence() {
        RMSource s = new RMSource(handler);
        assertNull(s.getCurrent());
        Identifier sid = s.generateSequenceIdentifier();
        EndpointReferenceType a = createMock(EndpointReferenceType.class);
        Expires e = RMUtils.getWSRMFactory().createExpires();
        e.setValue(Sequence.PT0S);
        s.createSequence(sid, a, e);        
        Sequence c = s.getCurrent();
        assertNotNull(c);
        Sequence seq = s.getSequence(sid);
        assertNotNull(seq);       
        assertSame(seq, c);        
    }
    
    public void testSetAcknowledged() throws NoSuchMethodException {
        RMSource s = new RMSource(handler);
        EndpointReferenceType acksTo = createMock(EndpointReferenceType.class);
        Identifier sid1 = s.generateSequenceIdentifier();
        Identifier sid2 = s.generateSequenceIdentifier();
        Expires e = RMUtils.getWSRMFactory().createExpires();
        e.setValue(Sequence.PT0S);
        s.createSequence(sid1, acksTo, e); 
        Sequence seq = s.getSequence(sid1);
        
        SequenceAcknowledgement ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        ack.setIdentifier(sid1);        
        s.setAcknowledged(ack);
        assertSame(ack, seq.getAcknowledged());
        ack.setIdentifier(sid2);
        s.setAcknowledged(ack);   
    }
}
