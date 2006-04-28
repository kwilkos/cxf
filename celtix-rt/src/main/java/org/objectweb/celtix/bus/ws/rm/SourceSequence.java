package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;

public class SourceSequence extends AbstractSequenceImpl implements RMSourceSequence {

    public static final Duration PT0S;
    private static final Logger LOG = LogUtils.getL7dLogger(SourceSequence.class);
    
    private SequenceAcknowledgement acked;
    
    private Date expires;
    private RMSource source;
    private BigInteger currentMessageNumber;
    private boolean lastMessage;
    private Identifier offeringId;
    private org.objectweb.celtix.ws.addressing.EndpointReferenceType target;
    
    static {
        Duration pt0s = null;
        try {
            DatatypeFactory df = DatatypeFactory.newInstance();
            pt0s = df.newDuration("PT0S");
        } catch (DatatypeConfigurationException ex) {
            LOG.log(Level.INFO, "Could not create Duration object.", ex);
        }
        PT0S = pt0s;
    }
    
    public SourceSequence(Identifier i) {
        this(i, null, null);
    }
    
    public SourceSequence(Identifier i, Date e, Identifier oi) {
        this(i, e, oi, BigInteger.ZERO, false);
    }
   
    
    public SourceSequence(Identifier i, Date e, Identifier oi, BigInteger cmn, boolean lm) {
        super(i);
        expires = e;

        offeringId = oi;

        currentMessageNumber = cmn;
        lastMessage = lm;
        acked = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        acked.setIdentifier(id);
    }
    
    // begin RMSourceSequence interface
    
    public BigInteger getCurrentMessageNr() {
        return currentMessageNumber;
    }

    public String getEndpointIdentifier() {
        return null;
    }

    public Date getExpiry() {
        return expires;
    }

    public boolean isLastMessage() {
        return lastMessage;
    }

    public Identifier getOfferingSequenceIdentifier() {
        return offeringId;
    }
    
    // end RMSourceSequence interface
    
    void setSource(RMSource s) {
        source = s;
    }
    
    void setLastMessage(boolean lm) {
        lastMessage = lm;
    }
    
    /**
     * Returns true if this sequence was constructed from an offer for an inbound sequence
     * includes in the CreateSequenceRequest in response to which the sequence with
     * the specified identifier was created.
     * 
     * @param id the sequence identifier
     * @return true if the sequence was constructed from an offer.
     */
    boolean offeredBy(Identifier sid) {
        return null != offeringId && offeringId.getValue().equals(sid.getValue());
    }
    
    /**
     * Returns true if the sequence is expired.
     * 
     * @return true if the sequence is expired.
     */

    boolean isExpired() {
        return expires == null ? false : new Date().after(expires);
    }
    
    void setExpires(Expires ex) {
        Duration d = null;
        if (null != ex) {
            d = ex.getValue();
        }

        if (null != d && (null == PT0S || !PT0S.equals(d))) {
            Date now = new Date();
            expires = new Date(now.getTime() + ex.getValue().getTimeInMillis(now));
        }
    }
    
    /**
     * Returns the next message number and increases the message number.
     * 
     * @return the next message number.
     */
    BigInteger nextMessageNumber() {
        return nextMessageNumber(null, null);
    }

    /**
     * Returns the next message number and increases the message number.
     * The parameters, if not null, indicate that this message is being sent as a response 
     * to the message with the specified message number in the sequence specified by the
     * by the identifier, and are used to decide if this message should be the last in
     * this sequence.
     * 
     * @return the next message number.
     */
    BigInteger nextMessageNumber(Identifier inSeqId, BigInteger inMsgNumber) {

        assert !lastMessage;
        
        BigInteger result = null;
        synchronized (this) {
            currentMessageNumber = currentMessageNumber.add(BigInteger.ONE);
            checkLastMessage(inSeqId, inMsgNumber);
            result = currentMessageNumber;
        } 
        return result;
    }
    
    void nextAndLastMessageNumber() {
        assert !lastMessage;
        
        synchronized (this) {
            currentMessageNumber = currentMessageNumber.add(BigInteger.ONE);
            lastMessage = true;
        }
    }
    
    /**
     * Used by the RM source to cache received acknowledgements for this
     * sequence.
     * 
     * @param acknowledgement an acknowledgement for this sequence
     */
    void setAcknowledged(SequenceAcknowledgement acknowledgement) {        
        acked = acknowledgement;      
    }
    
    
    SequenceAcknowledgement getAcknowledgement() {
        return acked;
    }
    
    /**
     * Checks if the message with the given number has been acknowledged.
     * 
     * @param m the message number
     * @return true of the message with the given number has been acknowledged.
     */
    boolean isAcknowledged(BigInteger m) {
        for (AcknowledgementRange r : acked.getAcknowledgementRange()) {
            if (m.subtract(r.getLower()).signum() >= 0 && r.getUpper().subtract(m).signum() >= 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if a last message had been sent for this sequence and if all
     * messages for this sequence have been acknowledged.
     * 
     * @return true if all messages have been acknowledged.
     */
    boolean allAcknowledged() {
        if (!lastMessage) {
            return false;
        }

        if (acked.getAcknowledgementRange().size() == 1) {         
            AcknowledgementRange r = acked.getAcknowledgementRange().get(0);
            return r.getLower().equals(BigInteger.ONE) && r.getUpper().equals(currentMessageNumber);
        }
        return false;
    }
    
    /**
     * The target for the sequence is the first non-anonymous address that
     * a message is sent to as part of this sequence. It is subsequently used
     * for as the target of out-of-band protocol messages related to that
     * sequence that originate from the sequnce source (i.e. TerminateSequence 
     * and LastMessage, but not AckRequested or SequenceAcknowledgement as these 
     * are orignate from the sequence destination).
     * 
     * @param to
     */
    synchronized void setTarget(org.objectweb.celtix.ws.addressing.EndpointReferenceType to) {
        if (target == null && !ContextUtils.isGenericAddress(to)) {
            target = to;
        }
    }
    
    synchronized org.objectweb.celtix.ws.addressing.EndpointReferenceType getTarget() {
        return target;
    } 
   
    /**
     * Checks if the current message should be the last message in this sequence
     * and if so sets the lastMessageNumber property.
     */
    private void checkLastMessage(Identifier inSeqId, BigInteger inMsgNumber) { 

        assert null != source;
        
        // check if this is a response to a message that was is the last message in the sequence
        // that included this sequence as an offer 
        
        if (null != inSeqId && null != inMsgNumber) {
            DestinationSequence inSeq = source.getHandler().getDestination().getSequence(inSeqId);
            if (null != inSeq && offeredBy(inSeqId) && inMsgNumber.equals(inSeq.getLastMessageNr())) {
                lastMessage = true;     
            }
        } 
        
        if (!lastMessage) {
            SequenceTerminationPolicyType stp = source.getSequenceTerminationPolicy();
            assert null != stp;

            if ((!stp.getMaxLength().equals(BigInteger.ZERO) && stp.getMaxLength()
                .compareTo(currentMessageNumber) <= 0)
                || (stp.getMaxRanges() > 0 && acked.getAcknowledgementRange().size() >= stp.getMaxRanges())
                || (stp.getMaxUnacknowledged() > 0 && source.getRetransmissionQueue()
                    .countUnacknowledged(this) >= stp.getMaxUnacknowledged())) {
                lastMessage = true;
            }
        }
        
        if (LOG.isLoggable(Level.FINE) && lastMessage) {
            LOG.fine(currentMessageNumber + " should be the last message in this sequence.");
        }
    }

}
