package org.objectweb.celtix.bus.ws.rm;



import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Timer;

import javax.xml.datatype.DatatypeConfigurationException;

import junit.framework.TestCase;

import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.bus.configuration.wsrm.AcksPolicyType;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.ObjectFactory;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType.AcknowledgementInterval;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType.BaseRetransmissionInterval;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType.ExponentialBackoff;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceControl;

public class DestinationSequenceTest extends TestCase {

    IMocksControl control;
    ObjectFactory factory = new ObjectFactory();
    Identifier id;
    Expires expires;
    EndpointReferenceType ref;
    RMSource source;
    RMDestination destination;
    RMHandler handler;
    ConfigurationHelper configurationHelper;
    RMAssertionType rma;
    AcksPolicyType ap;
 
    public void setUp() {
        control = createNiceControl();
        ref = control.createMock(EndpointReferenceType.class);
        source = control.createMock(RMSource.class); 
        destination = control.createMock(RMDestination.class);
        handler = control.createMock(RMHandler.class);
        configurationHelper = control.createMock(ConfigurationHelper.class);
                
        expires = factory.createExpires();
        id = factory.createIdentifier();
        id.setValue("seq");
        
        ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        rma = RMUtils.getWSRMPolicyFactory().createRMAssertionType();
        BaseRetransmissionInterval bri =
            RMUtils.getWSRMPolicyFactory().createRMAssertionTypeBaseRetransmissionInterval();
        bri.setMilliseconds(new BigInteger("3000"));
        rma.setBaseRetransmissionInterval(bri);
        ExponentialBackoff eb = 
            RMUtils.getWSRMPolicyFactory().createRMAssertionTypeExponentialBackoff();
        eb.getOtherAttributes().put(ConfigurationHelper.EXPONENTIAL_BACKOFF_BASE_ATTR,
                                    RetransmissionQueue.DEFAULT_EXPONENTIAL_BACKOFF);
        
    }

    public void testConstructors() throws DatatypeConfigurationException {

        Identifier otherId = factory.createIdentifier();
        otherId.setValue("otherSeq");
        
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        assertEquals(id, seq.getIdentifier());
        assertNull(seq.getLastMessageNr());
        assertSame(ref, seq.getAcksTo());
        assertNotNull(seq.getAcknowledgment());
        assertNotNull(seq.getMonitor());   
        
        SequenceAcknowledgement ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();        
        seq = new DestinationSequence(id, ref, BigInteger.TEN, ack);
        assertEquals(id, seq.getIdentifier());
        assertEquals(BigInteger.TEN, seq.getLastMessageNr());
        assertSame(ref, seq.getAcksTo());
        assertSame(ack, seq.getAcknowledgment());
        assertNotNull(seq.getMonitor());    
    }
    
    public void testEqualsAndHashCode() {
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        DestinationSequence otherSeq = null;
        assertTrue(!seq.equals(otherSeq));
        otherSeq = new DestinationSequence(id, ref, destination);
        assertEquals(seq, otherSeq);
        assertEquals(seq.hashCode(), otherSeq.hashCode());
        Identifier otherId = factory.createIdentifier();
        otherId.setValue("otherSeq");
        otherSeq = new DestinationSequence(otherId, ref, destination);
        assertTrue(!seq.equals(otherSeq));
        assertTrue(seq.hashCode() != otherSeq.hashCode()); 
        assertTrue(!seq.equals(this));
    }
    
    public void testGetSetDestination() {
        control.replay();
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        seq.setDestination(destination);
        assertSame(destination, seq.getDestination());
    }
    
    public void testGetEndpointIdentifier() {
        destination.getHandler();
        expectLastCall().andReturn(handler);
        handler.getConfigurationHelper();
        expectLastCall().andReturn(configurationHelper);
        configurationHelper.getEndpointId();
        expectLastCall().andReturn("abc.xyz");
        control.replay();
        
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        seq.setDestination(destination);
        assertEquals("abc.xyz", seq.getEndpointIdentifier());
   
        control.verify();
    }
    
    public void testGetAcknowledgementAsStream() throws SequenceFault {
        destination.getHandler();
        expectLastCall().andReturn(handler).times(2);
        handler.getConfigurationHelper();
        expectLastCall().andReturn(configurationHelper).times(2);
        configurationHelper.getRMAssertion();
        expectLastCall().andReturn(rma);
        configurationHelper.getAcksPolicy();
        expectLastCall().andReturn(ap);
        control.replay();
        
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        List<AcknowledgementRange> ranges = seq.getAcknowledgment().getAcknowledgementRange();
        assertEquals(0, ranges.size());
              
        seq.acknowledge(new BigInteger("1"));  
        assertNotNull(seq.getAcknowledgmentAsStream());
        
        control.verify();
    }
    
    public void testAcknowledgeBasic() throws SequenceFault {
        destination.getHandler();
        expectLastCall().andReturn(handler).times(4);
        handler.getConfigurationHelper();
        expectLastCall().andReturn(configurationHelper).times(4);
        configurationHelper.getRMAssertion();
        expectLastCall().andReturn(rma).times(2);
        configurationHelper.getAcksPolicy();
        expectLastCall().andReturn(ap).times(2);
        control.replay();
        
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        List<AcknowledgementRange> ranges = seq.getAcknowledgment().getAcknowledgementRange();
        assertEquals(0, ranges.size());
              
        seq.acknowledge(new BigInteger("1"));        
        assertEquals(1, ranges.size());
        AcknowledgementRange r1 = ranges.get(0);
        assertEquals(1, r1.getLower().intValue());
        assertEquals(1, r1.getUpper().intValue());
        
        seq.acknowledge(new BigInteger("2"));
        assertEquals(1, ranges.size());
        r1 = ranges.get(0);
        assertEquals(1, r1.getLower().intValue());
        assertEquals(2, r1.getUpper().intValue());
        
        control.verify();
    }
    
    public void testAcknowledgeLastMessageNumberExceeded() throws SequenceFault {  
        
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        
        RMAssertionType ra = control.createMock(RMAssertionType.class);

        expect(destination.getHandler()).andReturn(handler).times(2);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper).times(2);
        expect(configurationHelper.getRMAssertion()).andReturn(ra);
        expect(ra.getAcknowledgementInterval()).andReturn(null);
        expect(configurationHelper.getAcksPolicy()).andReturn(null);
        control.replay();
        
        seq.acknowledge(BigInteger.ONE);
        seq.setLastMessageNumber(BigInteger.ONE);
        try {
            seq.acknowledge(new BigInteger("2"));
            fail("Expected SequenceFault not thrown.");
        } catch (SequenceFault sf) {
            assertEquals("LastMessageNumberExceeded", sf.getFaultInfo().getFaultCode().getLocalPart());
        }
        
        control.verify();
    }
    
    public void testAcknowledgeAppendRange() throws SequenceFault {
   
        expect(destination.getHandler()).andReturn(handler).times(10);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper).times(10);
        expect(configurationHelper.getRMAssertion()).andReturn(rma).times(5);
        expect(configurationHelper.getAcksPolicy()).andReturn(ap).times(5);
        control.replay();
        
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        List<AcknowledgementRange> ranges = seq.getAcknowledgment().getAcknowledgementRange();        
        seq.acknowledge(new BigInteger("1"));
        seq.acknowledge(new BigInteger("2"));  
        seq.acknowledge(new BigInteger("5"));
        seq.acknowledge(new BigInteger("4"));
        seq.acknowledge(new BigInteger("6"));
        assertEquals(2, ranges.size());
        AcknowledgementRange r = ranges.get(0);
        assertEquals(1, r.getLower().intValue());
        assertEquals(2, r.getUpper().intValue());
        r = ranges.get(1);
        assertEquals(4, r.getLower().intValue());
        assertEquals(6, r.getUpper().intValue()); 
        
        control.verify();
    }
    
    public void testAcknowledgeInsertRange() throws SequenceFault {
        expect(destination.getHandler()).andReturn(handler).times(14);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper).times(14);
        expect(configurationHelper.getRMAssertion()).andReturn(rma).times(7);
        expect(configurationHelper.getAcksPolicy()).andReturn(ap).times(7);
        control.replay();
        
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        List<AcknowledgementRange> ranges = seq.getAcknowledgment().getAcknowledgementRange();        
        seq.acknowledge(new BigInteger("1"));
        seq.acknowledge(new BigInteger("2"));
        seq.acknowledge(new BigInteger("9"));
        seq.acknowledge(new BigInteger("10"));
        seq.acknowledge(new BigInteger("4"));
        seq.acknowledge(new BigInteger("9"));
        seq.acknowledge(new BigInteger("2"));
        
        assertEquals(3, ranges.size());
        AcknowledgementRange r = ranges.get(0);
        assertEquals(1, r.getLower().intValue());
        assertEquals(2, r.getUpper().intValue());
        r = ranges.get(1);
        assertEquals(4, r.getLower().intValue());
        assertEquals(4, r.getUpper().intValue()); 
        r = ranges.get(2);
        assertEquals(9, r.getLower().intValue());
        assertEquals(10, r.getUpper().intValue()); 
        
        control.verify();
    }
    
    public void testAcknowledgePrependRange() throws SequenceFault { 
        expect(destination.getHandler()).andReturn(handler).times(12);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper).times(12);
        expect(configurationHelper.getRMAssertion()).andReturn(rma).times(6);
        expect(configurationHelper.getAcksPolicy()).andReturn(ap).times(6);
        control.replay();
        
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        List<AcknowledgementRange> ranges = seq.getAcknowledgment().getAcknowledgementRange();
        seq.acknowledge(new BigInteger("4"));
        seq.acknowledge(new BigInteger("5"));
        seq.acknowledge(new BigInteger("6"));
        seq.acknowledge(new BigInteger("4"));
        seq.acknowledge(new BigInteger("2"));
        seq.acknowledge(new BigInteger("2"));
        assertEquals(2, ranges.size());
        AcknowledgementRange r = ranges.get(0);
        assertEquals(2, r.getLower().intValue());
        assertEquals(2, r.getUpper().intValue());
        r = ranges.get(1);
        assertEquals(4, r.getLower().intValue());
        assertEquals(6, r.getUpper().intValue()); 
        
        control.verify();
    }
    
    public void testMonitor() throws SequenceFault {
        // Timer t = new Timer();
        expect(destination.getHandler()).andReturn(handler).times(30);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper).times(30);
        // expect(handler.getTimer()).andReturn(t).times(15);
        expect(configurationHelper.getRMAssertion()).andReturn(rma).times(15);
        expect(configurationHelper.getAcksPolicy()).andReturn(ap).times(15);
        control.replay();
                
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        SequenceMonitor monitor = seq.getMonitor();
        assertNotNull(monitor);
        monitor.setMonitorInterval(500);
        
        assertEquals(0, monitor.getMPM());
        
        BigInteger mn = BigInteger.ONE;
        
        for (int i = 0; i < 10; i++) {
            seq.acknowledge(mn);
            mn = mn.add(BigInteger.ONE);
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
        int mpm1 = monitor.getMPM();
        assertTrue(mpm1 > 0);
        
        for (int i = 0; i < 5; i++) {
            seq.acknowledge(mn);
            mn = mn.add(BigInteger.ONE);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
        int mpm2 = monitor.getMPM();
        assertTrue(mpm2 > 0);
        assertTrue(mpm1 > mpm2);
        
        control.verify();
    }
    
    public void testAcknowledgeImmediate() throws SequenceFault {
        expect(destination.getHandler()).andReturn(handler).times(2);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper).times(2);
        expect(configurationHelper.getRMAssertion()).andReturn(rma).times(1);
        expect(configurationHelper.getAcksPolicy()).andReturn(ap).times(1);        
        control.replay();
        
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        assertTrue(!seq.sendAcknowledgement());
              
        seq.acknowledge(new BigInteger("1")); 
        
        assertTrue(seq.sendAcknowledgement());
        seq.acknowledgmentSent();
        assertFalse(seq.sendAcknowledgement());
        
        control.verify();
    }
    
    public void testAcknowledgeDeferred() throws SequenceFault, IOException {
        ap.setIntraMessageThreshold(0);
        AcknowledgementInterval ai = 
            RMUtils.getWSRMPolicyFactory().createRMAssertionTypeAcknowledgementInterval();
        ai.setMilliseconds(new BigInteger("200"));
        rma.setAcknowledgementInterval(ai);        
       
        Timer timer = new Timer();        
        expect(destination.getHandler()).andReturn(handler).times(8);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper).times(6);
        expect(handler.getTimer()).andReturn(timer).times(1);
        expect(configurationHelper.getRMAssertion()).andReturn(rma).times(3);
        expect(configurationHelper.getAcksPolicy()).andReturn(ap).times(3);
        
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        assertTrue(!seq.sendAcknowledgement());
        
        RMProxy proxy = control.createMock(RMProxy.class);
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.acknowledge(seq);
        expectLastCall();
        
        control.replay(); 
              
        seq.acknowledge(new BigInteger("1")); 
        seq.acknowledge(new BigInteger("2"));
        seq.acknowledge(new BigInteger("3"));
        
        assertFalse(seq.sendAcknowledgement());
        
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            // ignore
        }
        assertTrue(seq.sendAcknowledgement());
        seq.acknowledgmentSent();
        assertFalse(seq.sendAcknowledgement());
        
        control.verify();
    }
    
    public void testCorrelationID() {
        DestinationSequence seq = new DestinationSequence(id, ref, destination);
        String correlationID = "abdc1234";
        assertNull("unexpected correlation ID", seq.getCorrelationID());
        seq.setCorrelationID(correlationID);
        assertEquals("unexpected correlation ID",
                     correlationID,
                     seq.getCorrelationID());
    }

}
