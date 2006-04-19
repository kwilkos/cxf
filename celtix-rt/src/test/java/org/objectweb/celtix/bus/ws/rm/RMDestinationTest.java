package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.configuration.wsrm.DestinationPolicyType;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceType;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

public class RMDestinationTest extends TestCase {
    
    private RMHandler handler;
    private EndpointReferenceType address;

    public void setUp() {
        handler = createMock(RMHandler.class);
        address = createMock(EndpointReferenceType.class);        
    }   

    public void testSequenceAccess() {
        RMDestination d = new RMDestination(handler);
        Identifier sid = d.generateSequenceIdentifier();
        DestinationSequence seq = new DestinationSequence(sid, address, d);
        d.addSequence(seq);
        assertSame(seq, d.getSequence(sid));
        assertEquals(1, d.getAllSequences().size());
        d.removeSequence(seq);
        assertNull(d.getSequence(sid));
        assertEquals(0, d.getAllSequences().size());
    }
    
    public void testGetDestinationPolicies() {
        DestinationPolicyType dp = null;
        Configuration c = createMock(Configuration.class);
        reset(handler);
        handler.getConfiguration();
        expectLastCall().andReturn(c);
        c.getObject(DestinationPolicyType.class, "destinationPolicies");
        expectLastCall().andReturn(dp);
        replay(handler);
        replay(c);  
        RMDestination d = new RMDestination(handler);
        assertNotNull(d.getDestinationPolicies());
        verify(handler);
        verify(c);
                
        reset(handler);
        reset(c);
        
        dp = createMock(DestinationPolicyType.class);
        handler.getConfiguration();
        expectLastCall().andReturn(c);
        c.getObject(DestinationPolicyType.class, "destinationPolicies");         
        expectLastCall().andReturn(dp);
        replay(handler);
        replay(c);  
        d = new RMDestination(handler);
        assertNotNull(d.getDestinationPolicies());
        verify(handler);
        verify(c);        
    }

    public void testAcknowledge() throws SequenceFault {
        RMDestination d = new RMDestination(handler);
        Identifier sid = d.generateSequenceIdentifier();
        DestinationSequence seq = new DestinationSequence(sid, address, d);
        d.addSequence(seq);
        
        Configuration c = createMock(Configuration.class);
        reset(handler);
        handler.getConfiguration();
        expectLastCall().andReturn(c);
        c.getObject(RMAssertionType.class, "rmAssertion");
        expectLastCall().andReturn(null);
        handler.getConfiguration();
        expectLastCall().andReturn(c);
        c.getObject(DestinationPolicyType.class, "destinationPolicies");
        expectLastCall().andReturn(null);
        replay(handler);
        replay(c);  

        SequenceType st = RMUtils.getWSRMFactory().createSequenceType();
        st.setIdentifier(sid);
        BigInteger m = new BigInteger("3");
        st.setMessageNumber(m);
        d.acknowledge(st, RMUtils.getAddressingConstants().getNoneURI());
        
        Identifier unknown = d.generateSequenceIdentifier();
        st.setIdentifier(unknown);
        try {
            d.acknowledge(st, RMUtils.getAddressingConstants().getNoneURI());
            fail("Expected sequenceFault not thrown.");
        } catch (SequenceFault sf) {
            assertEquals("UnknownSequence", sf.getFaultInfo().getFaultCode().getLocalPart());
        }
    }

}
