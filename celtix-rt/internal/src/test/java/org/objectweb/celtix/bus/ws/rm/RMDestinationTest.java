package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.math.BigInteger;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
// import org.objectweb.celtix.bus.configuration.wsrm.AcksPolicyType;
// import org.objectweb.celtix.bus.configuration.wsrm.DestinationPolicyType;
import org.objectweb.celtix.ws.addressing.v200408.AttributedURI;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceType;
import org.objectweb.celtix.ws.rm.SequenceType.LastMessage;
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

    public void testAcknowledge() throws SequenceFault {
        RMStore store = control.createMock(RMStore.class);
        EasyMock.expect(handler.getStore()).andReturn(store);
        store.createDestinationSequence(EasyMock.isA(RMDestinationSequence.class));
        EasyMock.expectLastCall();
        ConfigurationHelper ch = control.createMock(ConfigurationHelper.class);       
        EasyMock.expect(handler.getConfigurationHelper()).andReturn(ch);
        RMAssertionType rma = control.createMock(RMAssertionType.class);
        EasyMock.expect(ch.getRMAssertion()).andReturn(rma);
        EasyMock.expect(rma.getAcknowledgementInterval()).andReturn(null);
        EasyMock.expect(handler.getConfigurationHelper()).andReturn(ch);
        EasyMock.expect(ch.getAcksPolicy()).andReturn(null);
        
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
    
    public void testAcknowledgeUnknownSequence() {
        RMDestination d = new RMDestination(handler);
        SequenceType st = control.createMock(SequenceType.class); 
        Identifier id = control.createMock(Identifier.class);
        EasyMock.expect(st.getIdentifier()).andReturn(id).times(2);
        EasyMock.expect(id.getValue()).andReturn("dseq1").times(2);
        control.replay();
        
        try {
            d.acknowledge(st, RMUtils.getAddressingConstants().getNoneURI());
            fail("Expected SequenceFault not thrown.");
        } catch (SequenceFault ex) {
            // expected
        }
        
        control.verify();
    }
    
    public void testAcknowledgeLastMessage() throws SequenceFault, IOException    {
        RMStore store = control.createMock(RMStore.class);
        EasyMock.expect(handler.getStore()).andReturn(store);
        store.createDestinationSequence(EasyMock.isA(RMDestinationSequence.class));
        EasyMock.expectLastCall();
        ConfigurationHelper ch = control.createMock(ConfigurationHelper.class);       
        EasyMock.expect(handler.getConfigurationHelper()).andReturn(ch);
        RMAssertionType rma = control.createMock(RMAssertionType.class);
        EasyMock.expect(ch.getRMAssertion()).andReturn(rma);
        EasyMock.expect(rma.getAcknowledgementInterval()).andReturn(null);
        EasyMock.expect(handler.getConfigurationHelper()).andReturn(ch);
        EasyMock.expect(ch.getAcksPolicy()).andReturn(null);
        AttributedURI uri = control.createMock(AttributedURI.class);
        EasyMock.expect(address.getAddress()).andReturn(uri).times(2);
        EasyMock.expect(uri.getValue()).andReturn("").times(2);
        RMProxy proxy = control.createMock(RMProxy.class);
        EasyMock.expect(handler.getProxy()).andReturn(proxy);
        proxy.acknowledge(EasyMock.isA(DestinationSequence.class));
        EasyMock.expectLastCall().andThrow(new IOException("Failed to send standalone acknowledgement."));
        control.replay();
        
        RMDestination d = new RMDestination(handler);
        Identifier sid = d.generateSequenceIdentifier();
        DestinationSequence seq = new DestinationSequence(sid, address, d);
        d.addSequence(seq);
        SequenceType st = RMUtils.getWSRMFactory().createSequenceType();
        st.setIdentifier(sid);
        BigInteger m = new BigInteger("3");
        st.setMessageNumber(m);
        LastMessage lm = RMUtils.getWSRMFactory().createSequenceTypeLastMessage();
        st.setLastMessage(lm);
        d.acknowledge(st, RMUtils.getAddressingConstants().getNoneURI()); 
     
        control.verify();
    }

}
