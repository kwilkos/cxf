package org.objectweb.celtix.bus.ws.rm;

import junit.framework.TestCase;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

public class RMEndpointTest extends TestCase {
    
    private RMHandler handler;
    
    public void setUp() {
        handler = createMock(RMHandler.class);
    }
    
    public void testRMEndpointConstructor() {
        RMEndpoint e = new RMEndpoint(handler);
        assertSame(handler, e.getHandler());
        assertEquals(0, e.map.size());        
    }
    
    public void generateSequenceIndentifier() {
        RMEndpoint e = new RMEndpoint(handler);
        Identifier sid1 = e.generateSequenceIdentifier();
        assertNotNull(sid1.getValue());
        Identifier sid2 = e.generateSequenceIdentifier();
        assertTrue(!sid1.equals(sid2));
    }
    
    public void testGetPolicies() {
        Configuration c = createMock(Configuration.class);
        reset(handler);
        handler.getConfiguration();
        expectLastCall().andReturn(c);
        c.getObject("policies");
        expectLastCall().andReturn(null);
        replay(handler);
        replay(c);
        
        RMEndpoint e = new RMEndpoint(handler);
        assertNull(e.getPolicies()); 
        verify(handler);
        verify(c);
    }
    
    public void testGetRMAssertion() {
        Configuration c = createMock(Configuration.class);
        reset(handler);
        handler.getConfiguration();
        expectLastCall().andReturn(c);
        c.getObject(RMAssertionType.class, "rmAssertion");
        expectLastCall().andReturn(null);
        replay(handler);
        replay(c);
        
        RMEndpoint e = new RMEndpoint(handler);
        RMAssertionType rma = e.getRMAssertion();
        assertNotNull(rma);
        
        verify(handler);
        verify(c);
    }
    
    public void testAddGetSequence() {
        RMEndpoint e = new RMEndpoint(handler);
        EndpointReferenceType a = createMock(EndpointReferenceType.class);
        RMDestination destination = createMock(RMDestination.class);
        Sequence seq = new Sequence(e.generateSequenceIdentifier(), destination, a);
        e.addSequence(seq);
        assertEquals(1, e.map.size()); 
        assertSame(seq, e.getSequence(seq.getIdentifier()));
        Identifier other = e.generateSequenceIdentifier();
        assertNull(e.getSequence(other));
        other.setValue(seq.getIdentifier().getValue());
        assertNotNull(e.getSequence(other));
        assertSame(seq, e.getSequence(other));
    }
    
    
    
    
    
}
