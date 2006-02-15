package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.ws.rm.CreateSequenceResponseType;
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
    
    public void testGetSequenceTerminationPolicies() {
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
        assertNotNull(s.getSequenceTerminationPolicy());
        verify(handler);
        verify(c);
    }
    
    public void testCreateSequence() throws IOException {
        RMSource s = new RMSource(handler);
        Identifier sid = s.generateSequenceIdentifier();
        Expires e = RMUtils.getWSRMFactory().createExpires();
        e.setValue(Sequence.PT0S);
        CreateSequenceResponseType csr = RMUtils.getWSRMFactory().createCreateSequenceResponseType();
        csr.setIdentifier(sid);
        csr.setExpires(e);
       
        reset(handler);
        RMService service = createMock(RMService.class);
        handler.getService();
        expectLastCall().andReturn(service);
        service.createSequence(s);
        expectLastCall().andReturn(csr);
        
        replay(handler);
        replay(service);
        assertNull(s.getCurrent());
        s.createSequence();
        Sequence seq = s.getCurrent();
        assertNotNull(seq);      
        assertEquals(seq.getIdentifier(), sid); 
        
        verify(handler);
        verify(service);
        
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
