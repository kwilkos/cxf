package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.bus.configuration.wsrm.DestinationPolicyType;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceType;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.persistence.RMStore;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

public class RMDestinationTest extends TestCase {
    
    private IMocksControl control;
    private RMHandler handler;
    private EndpointReferenceType address;

    public void setUp() {
        control = EasyMock.createNiceControl();
        handler = control.createMock(RMHandler.class);
        address = control.createMock(EndpointReferenceType.class);        
    }   

    public void testSequenceAccess() {
        RMStore store = control.createMock(RMStore.class);
        EasyMock.expect(handler.getStore()).andReturn(store).times(2);
        store.createDestinationSequence(EasyMock.isA(RMDestinationSequence.class));
        EasyMock.expectLastCall();
        store.removeDestinationSequence(EasyMock.isA(Identifier.class));
        EasyMock.expectLastCall();
        
        control.replay();
        RMDestination d = new RMDestination(handler);
        Identifier sid = d.generateSequenceIdentifier();
        DestinationSequence seq = new DestinationSequence(sid, address, d);
        d.addSequence(seq);
        assertSame(seq, d.getSequence(sid));
        assertEquals(1, d.getAllSequences().size());
        d.removeSequence(seq);
        assertNull(d.getSequence(sid));
        assertEquals(0, d.getAllSequences().size());
        control.verify();
    }
    
    public void testGetDestinationPolicies() {
        Configuration c = control.createMock(Configuration.class);
        DestinationPolicyType dp = null;
        EasyMock.expect(handler.getConfiguration()).andReturn(c);
        EasyMock.expect(c.getObject(DestinationPolicyType.class, "destinationPolicies")).andReturn(dp);
        control.replay(); 
        RMDestination d = new RMDestination(handler);
        assertNotNull(d.getDestinationPolicies());
        control.verify();
        
        control.reset();        
        dp = control.createMock(DestinationPolicyType.class);
        EasyMock.expect(handler.getConfiguration()).andReturn(c);         
        EasyMock.expect(c.getObject(DestinationPolicyType.class, "destinationPolicies")).andReturn(dp); 
        control.replay();
        assertNotNull(d.getDestinationPolicies());
        control.verify();
    }

    public void testAcknowledge() throws SequenceFault {
        RMStore store = control.createMock(RMStore.class);
        EasyMock.expect(handler.getStore()).andReturn(store);
        store.createDestinationSequence(EasyMock.isA(RMDestinationSequence.class));
        EasyMock.expectLastCall();
        Configuration c = control.createMock(Configuration.class);       
        EasyMock.expect(handler.getConfiguration()).andReturn(c);
        EasyMock.expect(c.getObject(RMAssertionType.class, "rmAssertion")).andReturn(null);
        EasyMock.expect(handler.getConfiguration()).andReturn(c);
        EasyMock.expect(c.getObject(DestinationPolicyType.class, "destinationPolicies")).andReturn(null);
        
        control.replay();
        RMDestination d = new RMDestination(handler);
        Identifier sid = d.generateSequenceIdentifier();
        DestinationSequence seq = new DestinationSequence(sid, address, d);
        d.addSequence(seq);
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
        control.verify();
    }
    
    public void testRestore() {
        RMStore store = control.createMock(RMStore.class);        
        EasyMock.expect(handler.getStore()).andReturn(store);
        Configuration c = control.createMock(Configuration.class);
        EasyMock.expect(handler.getConfiguration()).andReturn(c);
        Configuration pc = control.createMock(Configuration.class);
        EasyMock.expect(c.getParent()).andReturn(pc);
        EasyMock.expect(pc.getId()).andReturn("endpoint"); 
        Collection<RMDestinationSequence> dss = new ArrayList<RMDestinationSequence>();
        EasyMock.expect(store.getDestinationSequences("endpoint")).andReturn(dss);
        
        control.replay();
        RMDestination d = new RMDestination(handler);
        d.restore();
        assertEquals(0, d.getAllSequences().size());
        control.verify();
    }

}
