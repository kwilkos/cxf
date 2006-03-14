package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;
import java.util.List;
import java.util.Timer;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.configuration.wsrm.AcksPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.ObjectFactory;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

public class SequenceTest extends TestCase {

    ObjectFactory factory = new ObjectFactory();
    Identifier id;
    Expires expires;
    EndpointReferenceType ref;
    RMSource source;
    RMDestination destination;
    RMHandler handler;
 
    public void setUp() {
        expires = factory.createExpires();
        id = factory.createIdentifier();
        id.setValue("seq");
        ref = createMock(EndpointReferenceType.class);
        source = createMock(RMSource.class); 
        destination = createMock(RMDestination.class);
        handler = createMock(RMHandler.class);
    }

    public void testConstructors() throws DatatypeConfigurationException {
        
        Sequence seq = new Sequence(id, destination, ref);
        assertEquals(Sequence.Type.DESTINATION, seq.getType());
        assertEquals(id, seq.getIdentifier());
        assertNull(seq.getLastMessageNumber());
        assertSame(ref, seq.getAcksTo());
        assertTrue(!seq.isExpired());
        assertNull(seq.getCurrentMessageNumber());
        assertNotNull(seq.getAcknowledged());
        assertNotNull(seq.getAcknowledged(null));
        assertTrue(!seq.allAcknowledged());
        assertNotNull(seq.getMonitor());
        
        seq = new Sequence(id, source, expires);
        assertEquals(Sequence.Type.SOURCE, seq.getType());
        assertEquals(id, seq.getIdentifier());
        assertNull(seq.getLastMessageNumber());
        assertNull(seq.getAcksTo());
        assertTrue(!seq.isExpired());
        assertEquals(BigInteger.ZERO, seq.getCurrentMessageNumber());
        assertNotNull(seq.getAcknowledged());
        assertNotNull(seq.getAcknowledged(null));
        assertTrue(!seq.allAcknowledged());
        assertNull(seq.getMonitor());
        
        DatatypeFactory dbf = DatatypeFactory.newInstance();
        Duration d = dbf.newDuration(0);        
        expires.setValue(d);
        seq = new Sequence(id, source, expires);
        assertTrue(!seq.isExpired());
        
        d = dbf.newDuration("PT0S");        
        expires.setValue(d);
        seq = new Sequence(id, source, expires);
        assertTrue(!seq.isExpired());
        
        d = dbf.newDuration(1000);        
        expires.setValue(d);
        seq = new Sequence(id, source, expires);
        assertTrue(!seq.isExpired());
        
        d = dbf.newDuration("-PT1S");        
        expires.setValue(d);
        seq = new Sequence(id, source, expires);
        assertTrue(seq.isExpired());   
    }
    
    public void testSetAcknowledged() {
        Sequence seq = new Sequence(id, source, expires);
        assertEquals(0, seq.getAcknowledged().getAcknowledgementRange().size());
        acknowledge(seq, 1, 2, 3);
        SequenceAcknowledgement ack = seq.getAcknowledged();
        assertEquals(1, ack.getAcknowledgementRange().size());
        acknowledge(seq, 1, 2, 4, 5, 6, 8, 9, 10);
        ack = seq.getAcknowledged();
        assertEquals(3, ack.getAcknowledgementRange().size());
        assertTrue(!seq.isAcknowledged(new BigInteger("3")));
        
        seq = new Sequence(id, destination, ref);
        assertEquals(0, seq.getAcknowledged().getAcknowledgementRange().size());
        acknowledge(seq, 1, 2, 3);
        assertEquals(0, seq.getAcknowledged().getAcknowledgementRange().size());       
    } 
    
    public void testAcknowledgeBasic() {
        AcksPolicyType ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        destination.getHandler();
        expectLastCall().andReturn(handler).times(3);
        // handler.getTimer();
        // expectLastCall().andReturn(null).times(3);
        destination.getAcksPolicy();
        expectLastCall().andReturn(ap).times(3);
        replay(destination);
        replay(handler);
        
        Sequence seq = new Sequence(id, destination, ref);
        List<AcknowledgementRange> ranges = seq.getAcknowledged().getAcknowledgementRange();
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
        
        seq = new Sequence(id, source, expires);
        seq.acknowledge(BigInteger.TEN);
        assertEquals(0, seq.getAcknowledged().getAcknowledgementRange().size());
    }
    
    public void testAcknowledgeAppendRange() {
        AcksPolicyType ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        destination.getHandler();
        expectLastCall().andReturn(handler).times(5);
        //handler.getTimer();
        //expectLastCall().andReturn(null).times(5);
        destination.getAcksPolicy();
        expectLastCall().andReturn(ap).times(5);
        replay(destination);
        replay(handler);
        
        Sequence seq = new Sequence(id, destination, ref);
        List<AcknowledgementRange> ranges = seq.getAcknowledged().getAcknowledgementRange();        
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
    }
    
    public void testAcknowledgeInsertRange() {
        AcksPolicyType ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        destination.getHandler();
        expectLastCall().andReturn(handler).times(5);
        //handler.getTimer();
        //expectLastCall().andReturn(null).times(5);
        destination.getAcksPolicy();
        expectLastCall().andReturn(ap).times(5);
        replay(destination);
        replay(handler);
        
        Sequence seq = new Sequence(id, destination, ref);
        List<AcknowledgementRange> ranges = seq.getAcknowledged().getAcknowledgementRange();        
        seq.acknowledge(new BigInteger("1"));
        seq.acknowledge(new BigInteger("2"));
        seq.acknowledge(new BigInteger("9"));
        seq.acknowledge(new BigInteger("10"));
        seq.acknowledge(new BigInteger("4"));
        
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
    }
    
    public void testAcknowledgePrependRange() { 
        AcksPolicyType ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        destination.getHandler();
        expectLastCall().andReturn(handler).times(4);
        //handler.getTimer();
        //expectLastCall().andReturn(null).times(4);
        destination.getAcksPolicy();
        expectLastCall().andReturn(ap).times(4);
        replay(destination);
        replay(handler);
        
        Sequence seq = new Sequence(id, destination, ref);
        List<AcknowledgementRange> ranges = seq.getAcknowledged().getAcknowledgementRange();
        seq.acknowledge(new BigInteger("4"));
        seq.acknowledge(new BigInteger("5"));
        seq.acknowledge(new BigInteger("6"));
        seq.acknowledge(new BigInteger("2"));
        assertEquals(2, ranges.size());
        AcknowledgementRange r = ranges.get(0);
        assertEquals(2, r.getLower().intValue());
        assertEquals(2, r.getUpper().intValue());
        r = ranges.get(1);
        assertEquals(4, r.getLower().intValue());
        assertEquals(6, r.getUpper().intValue());      
    }
    
    public void testAllAcknowledged() {
        AcksPolicyType ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        destination.getHandler();
        expectLastCall().andReturn(handler).times(11);
        //handler.getTimer();
        //expectLastCall().andReturn(null).times(11);
        destination.getAcksPolicy();
        expectLastCall().andReturn(ap).times(11);
        replay(destination);
        replay(handler);
        
        Sequence seq = new Sequence(id, destination, ref);        
        for (int i = 0; i < 4; i++) {
            seq.nextMessageNumber();
        }
        assertTrue(!seq.allAcknowledged());
        seq.setLastMessageNumber(new BigInteger("4"));
        assertTrue(!seq.allAcknowledged());
        for (int i = 1; i < 5; i++) {
            Integer ih = new Integer(i);
            seq.acknowledge(new BigInteger(ih.toString()));
            assertEquals("At i = " + i + ": all messages are acknowledged",
                         i < 4, !seq.allAcknowledged());
        }
        
        seq = new Sequence(id, destination, ref);
        for (int i = 0; i < 10; i++) {
            seq.nextMessageNumber();
        }
        seq.setLastMessageNumber(BigInteger.TEN);
        for (int i = 1; i < 5; i++) {
            Integer ih = new Integer(i);
            seq.acknowledge(new BigInteger(ih.toString()));
            assertTrue(!seq.allAcknowledged());
        }
        for (int i = 8; i < 11; i++) {
            Integer ih = new Integer(i);
            seq.acknowledge(new BigInteger(ih.toString()));
            assertTrue(!seq.allAcknowledged());
        }
    }
    
    public void testNextMessageNumber() {     
        Sequence seq = null;
        SourcePolicyType sp = RMUtils.getWSRMConfFactory().createSourcePolicyType();
        
        // default termination policy
        
        SequenceTerminationPolicyType stp = 
            RMUtils.getWSRMConfFactory().createSequenceTerminationPolicyType();        
        sp.setSequenceTerminationPolicy(stp);
        
        seq = new Sequence(id, source, expires);  
        assertTrue(!nextSequences(seq, sp, 10));
        
        // termination policy max length = 1
        
        seq = new Sequence(id, source, expires);  
        stp.setMaxLength(BigInteger.ONE);
        assertTrue(nextSequences(seq, sp, 10));
        assertEquals(BigInteger.ONE, seq.getCurrentMessageNumber());
        
        // termination policy max length = 5
        seq = new Sequence(id, source, expires);  
        stp.setMaxLength(new BigInteger("5"));
        assertTrue(!nextSequences(seq, sp, 2));
        
        // termination policy max range exceeded
        
        seq = new Sequence(id, source, expires);  
        stp.setMaxLength(null);
        stp.setMaxRanges(new Integer(3));
        acknowledge(seq, 1, 2, 4, 5, 6, 8, 9, 10);
        assertTrue(nextSequences(seq, sp, 10));
        assertEquals(BigInteger.ONE, seq.getCurrentMessageNumber());
        
        // termination policy max range not exceeded
        
        seq = new Sequence(id, source, expires);  
        stp.setMaxLength(null);
        stp.setMaxRanges(new Integer(4));
        acknowledge(seq, 1, 2, 4, 5, 6, 8, 9, 10);
        assertTrue(!nextSequences(seq, sp, 10));
        
        // termination policy max unacknowledged 
    }
    
    public void testMonitor() {
        Timer t = new Timer();
        AcksPolicyType ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        destination.getHandler();
        expectLastCall().andReturn(handler).times(15);
        handler.getTimer();
        expectLastCall().andReturn(t).times(15);
        destination.getAcksPolicy();
        expectLastCall().andReturn(ap).times(15);
        replay(destination);
        replay(handler);
        
        
        Sequence seq = new Sequence(id, destination, ref);
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
    }
    
    public void testAcknowledgeImmediate() {
        AcksPolicyType ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        destination.getHandler();
        expectLastCall().andReturn(handler).times(1);
        //handler.getTimer();
        //expectLastCall().andReturn(null).times(1);
        destination.getAcksPolicy();
        expectLastCall().andReturn(ap).times(1);
        replay(destination);
        replay(handler);
        
        Sequence seq = new Sequence(id, destination, ref);
        assertTrue(!seq.sendAcknowledgement());
              
        seq.acknowledge(new BigInteger("1")); 
        
        assertTrue(seq.sendAcknowledgement());
        seq.acknowledgmentSent();
        assertFalse(seq.sendAcknowledgement());
    }
    
    public void testAcknowledgeDeferred() {
        AcksPolicyType ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        ap.setIntraMessageThreshold(0);
        ap.setDeferredBy(200);
        Timer timer = new Timer();
        destination.getHandler();
        expectLastCall().andReturn(handler).times(3);
        handler.getTimer();
        expectLastCall().andReturn(timer).times(1);
        destination.getAcksPolicy();
        expectLastCall().andReturn(ap).times(3);
        replay(destination);
        replay(handler);
        
        Sequence seq = new Sequence(id, destination, ref);
        assertTrue(!seq.sendAcknowledgement());
              
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
    }
    
    private boolean nextSequences(Sequence seq, SourcePolicyType sp, int n) {
        reset(source);
        source.getSequenceTerminationPolicy();
        expectLastCall().andReturn(sp.getSequenceTerminationPolicy());
        replay(source);
        
        int i = 0;
        while ((i < n) && (null == seq.getLastMessageNumber())) {
            assertNotNull(seq.nextMessageNumber());
            verify(source);
            reset(source);
            source.getSequenceTerminationPolicy();
            expectLastCall().andReturn(sp.getSequenceTerminationPolicy());
            replay(source);
            i++;
        }
        return null != seq.getLastMessageNumber();
    }
    
    // this methods cannot be private because of a bug in PMD which otherwise
    // would report it as an 'unused private method' 
    protected void acknowledge(Sequence seq, int... messageNumbers) {
        SequenceAcknowledgement ack = factory.createSequenceAcknowledgement();
        int i = 0;
        while (i < messageNumbers.length) {
            AcknowledgementRange r = factory.createSequenceAcknowledgementAcknowledgementRange();
            Integer li = new Integer(messageNumbers[i]);
            BigInteger l = new BigInteger(li.toString());
            r.setLower(l);
            i++;
            
            while (i < messageNumbers.length && (messageNumbers[i] - messageNumbers[i - 1]) == 1) {
                i++;
            }
            Integer ui = new Integer(messageNumbers[i - 1]);
            BigInteger u = new BigInteger(ui.toString());
            r.setUpper(u);
            ack.getAcknowledgementRange().add(r);
        }
        seq.setAcknowledged(ack);
        if (Sequence.Type.SOURCE == seq.getType()) {
            for (int m : messageNumbers) {
                Integer ih = new Integer(m);
                assertTrue(seq.isAcknowledged(new BigInteger(ih.toString())));
            }
        }
    }
    
}
