package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.bus.configuration.wsrm.AcksPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.ObjectFactory;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType.BaseRetransmissionInterval;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType.ExponentialBackoff;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

import static org.easymock.EasyMock.expect;

public class SourceSequenceTest extends TestCase {

    IMocksControl control;
    ObjectFactory factory = new ObjectFactory();
    Identifier id;
    RMSource source;
    RMDestination destination;
    RMHandler handler;
    ConfigurationHelper configurationHelper;
    RMAssertionType rma;
    AcksPolicyType ap;
 
    public void setUp() {
        
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

    public void testConstructors() {

        Identifier otherId = factory.createIdentifier();
        otherId.setValue("otherSeq");
       
        SourceSequence seq = null;
        
        seq = new SourceSequence(id);
        assertEquals(id, seq.getIdentifier());
        assertTrue(!seq.isLastMessage());
        assertTrue(!seq.isExpired());
        assertEquals(BigInteger.ZERO, seq.getCurrentMessageNr());
        assertNotNull(seq.getAcknowledgement());
        assertEquals(0, seq.getAcknowledgement().getAcknowledgementRange().size());
        assertTrue(!seq.allAcknowledged());
        assertFalse(seq.offeredBy(otherId));
        
        Date expiry = new Date(System.currentTimeMillis() + 3600 * 1000);
        
        seq = new SourceSequence(id, expiry, null);
        assertEquals(id, seq.getIdentifier());
        assertTrue(!seq.isLastMessage());
        assertTrue(!seq.isExpired());
        assertEquals(BigInteger.ZERO, seq.getCurrentMessageNr());
        assertNotNull(seq.getAcknowledgement());
        assertEquals(0, seq.getAcknowledgement().getAcknowledgementRange().size());
        assertTrue(!seq.allAcknowledged());
        assertFalse(seq.offeredBy(otherId));
        
        seq = new SourceSequence(id, expiry, otherId);
        assertTrue(seq.offeredBy(otherId));
        assertFalse(seq.offeredBy(id));
    }
    
    public void testSetExpires() throws DatatypeConfigurationException {
        SourceSequence seq = new SourceSequence(id);
        
        Expires expires = factory.createExpires();
        
        DatatypeFactory dbf = DatatypeFactory.newInstance();
        Duration d = dbf.newDuration(0);        
        expires.setValue(d);
        seq.setExpires(expires);
        assertTrue(!seq.isExpired());
        
        d = dbf.newDuration("PT0S");        
        expires.setValue(d);
        seq.setExpires(expires);
        assertTrue(!seq.isExpired());
        
        d = dbf.newDuration(1000);        
        expires.setValue(d);
        seq.setExpires(expires);
        assertTrue(!seq.isExpired());
        
        d = dbf.newDuration("-PT1S");        
        expires.setValue(d);
        seq.setExpires(expires);
        assertTrue(seq.isExpired());   
    }
    
    public void testEqualsAndHashCode() {
        SourceSequence seq = new SourceSequence(id);
        SourceSequence otherSeq = null;
        assertTrue(!seq.equals(otherSeq));
        otherSeq = new SourceSequence(id);
        assertEquals(seq, otherSeq);
        assertEquals(seq.hashCode(), otherSeq.hashCode());
        Identifier otherId = factory.createIdentifier();
        otherId.setValue("otherSeq");
        otherSeq = new SourceSequence(otherId);
        assertTrue(!seq.equals(otherSeq));
        assertTrue(seq.hashCode() != otherSeq.hashCode()); 
        assertTrue(!seq.equals(this));
    }
    
    public void testSetAcknowledged() {
        SourceSequence seq = new SourceSequence(id);
        SequenceAcknowledgement ack = seq.getAcknowledgement();
        ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        AcknowledgementRange r = RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();
        r.setLower(new BigInteger("1"));
        r.setUpper(new BigInteger("2"));
        ack.getAcknowledgementRange().add(r);
        r = RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();
        r.setLower(new BigInteger("4"));
        r.setUpper(new BigInteger("6"));
        ack.getAcknowledgementRange().add(r);
        r = RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();
        r.setLower(new BigInteger("8"));
        r.setUpper(new BigInteger("10"));
        ack.getAcknowledgementRange().add(r);
        seq.setAcknowledged(ack);
        assertSame(ack, seq.getAcknowledgement());
        assertEquals(3, ack.getAcknowledgementRange().size());
        assertTrue(!seq.isAcknowledged(new BigInteger("3")));  
        assertTrue(seq.isAcknowledged(new BigInteger("5")));
    } 
    
    public void testAllAcknowledged() throws SequenceFault {
        
        SourceSequence seq = new SourceSequence(id, null, null, new BigInteger("4"), false);        
        
        assertTrue(!seq.allAcknowledged());
        seq.setLastMessage(true);
        assertTrue(!seq.allAcknowledged());
        SequenceAcknowledgement ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        AcknowledgementRange r = RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();
        r.setLower(BigInteger.ONE);
        r.setUpper(new BigInteger("2"));
        ack.getAcknowledgementRange().add(r);
        seq.setAcknowledged(ack);
        assertTrue(!seq.allAcknowledged());
        r.setUpper(new BigInteger("4"));
        assertTrue(seq.allAcknowledged());
    }
    
    public void testNextMessageNumber() {     
        SourceSequence seq = null;
        control = EasyMock.createNiceControl();
        source = control.createMock(RMSource.class); 
        destination = control.createMock(RMDestination.class);
        handler = control.createMock(RMHandler.class);
        configurationHelper = control.createMock(ConfigurationHelper.class);
        
        // default termination policy
        
        SequenceTerminationPolicyType stp = 
            RMUtils.getWSRMConfFactory().createSequenceTerminationPolicyType();        
        
        seq = new SourceSequence(id);  
        seq.setSource(source);
        assertTrue(!nextMessages(seq, stp, 10));
        
        // termination policy max length = 1
        
        seq = new SourceSequence(id); 
        seq.setSource(source);
        stp.setMaxLength(BigInteger.ONE);
        assertTrue(nextMessages(seq, stp, 10));
        assertEquals(BigInteger.ONE, seq.getCurrentMessageNr());
        
        // termination policy max length = 5
        seq = new SourceSequence(id); 
        seq.setSource(source);
        stp.setMaxLength(new BigInteger("5"));
        assertTrue(!nextMessages(seq, stp, 2));
        
        // termination policy max range exceeded
        
        seq = new SourceSequence(id); 
        seq.setSource(source);
        stp.setMaxLength(null);
        stp.setMaxRanges(new Integer(3));
        acknowledge(seq, 1, 2, 4, 5, 6, 8, 9, 10);
        assertTrue(nextMessages(seq, stp, 10));
        assertEquals(BigInteger.ONE, seq.getCurrentMessageNr());
        
        // termination policy max range not exceeded
        
        seq = new SourceSequence(id); 
        seq.setSource(source);
        stp.setMaxLength(null);
        stp.setMaxRanges(new Integer(4));
        acknowledge(seq, 1, 2, 4, 5, 6, 8, 9, 10);
        assertTrue(!nextMessages(seq, stp, 10));
        
        // termination policy max unacknowledged 
    }
    
    public void testGetEndpointIdentfier() {
        SourceSequence seq = null;
        control = EasyMock.createNiceControl();
        source = control.createMock(RMSource.class); 
        handler = control.createMock(RMHandler.class);
        configurationHelper = control.createMock(ConfigurationHelper.class);  
        
        expect(source.getHandler()).andReturn(handler);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper);
        expect(configurationHelper.getEndpointId())
            .andReturn("abc.xyz");
        control.replay();
        
        seq = new SourceSequence(id);
        seq.setSource(source);
        assertEquals("abc.xyz", seq.getEndpointIdentifier());      
        control.verify();       
    }
    
    public void testCheckOfferingSequenceClosed() {
        SourceSequence seq = null;
        
        control = EasyMock.createNiceControl();
        source = control.createMock(RMSource.class); 
        destination = control.createMock(RMDestination.class);
        handler = control.createMock(RMHandler.class);
        configurationHelper = control.createMock(ConfigurationHelper.class);
        
        DestinationSequence dseq = control.createMock(DestinationSequence.class);   
        Identifier did = control.createMock(Identifier.class);
        
        
        expect(source.getHandler()).andReturn(handler);
        expect(handler.getDestination()).andReturn(destination);
        expect(destination.getSequence(did)).andReturn(dseq);
        expect(dseq.getLastMessageNr()).andReturn(BigInteger.ONE);
        expect(did.getValue()).andReturn("dseq").times(2);
        
        control.replay();
        
        seq = new SourceSequence(id, null, did);  
        seq.setSource(source);        
        seq.nextMessageNumber(did, BigInteger.ONE);
        assertTrue(seq.isLastMessage());
        
        control.verify();
    }
    
    public void testIdentifierEquals() {
        control = EasyMock.createNiceControl();
        Identifier id1 = null;
        Identifier id2 = null;   
        assertTrue(AbstractSequenceImpl.identifierEquals(id1, id2));
        
        id1 = factory.createIdentifier();
        id1.setValue("seq1"); 
        assertTrue(!AbstractSequenceImpl.identifierEquals(id1, id2));
        
        id2 = factory.createIdentifier();
        id2.setValue("seq2"); 
        assertTrue(!AbstractSequenceImpl.identifierEquals(id1, id2));
        
        id2.setValue("seq1");
        assertTrue(AbstractSequenceImpl.identifierEquals(id1, id2));
    }
   
    private boolean nextMessages(SourceSequence seq, SequenceTerminationPolicyType stp, int n) {
        
        control.reset();
        expect(source.getHandler()).andReturn(handler);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper);
        expect(configurationHelper.getSequenceTerminationPolicy())
            .andReturn(stp);
        control.replay();
        
        int i = 0;
        while ((i < n) && !seq.isLastMessage()) {            
            assertNotNull(seq.nextMessageNumber());
            control.verify();
            control.reset();
            expect(source.getHandler()).andReturn(handler);
            expect(handler.getConfigurationHelper()).andReturn(configurationHelper);
            expect(configurationHelper.getSequenceTerminationPolicy())
                .andReturn(stp);
            control.replay();
            i++;
        }
        return seq.isLastMessage();
    }
    
    // this method cannot be private because of a bug in PMD which otherwise
    // would report it as an 'unused private method' 
    protected void acknowledge(SourceSequence seq, int... messageNumbers) {
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
    }
    
}
