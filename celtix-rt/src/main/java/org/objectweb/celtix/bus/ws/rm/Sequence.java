package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.objectweb.celtix.bus.configuration.wsrm.AcksPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;

public class Sequence {
    public static final Duration PT0S;
    private static final Logger LOG = LogUtils.getL7dLogger(Sequence.class);

    private SequenceAcknowledgement acked;

    private final Identifier id;
    private Date expires;
    private RMSource source;
    private RMDestination destination;
    private EndpointReferenceType acksTo;
    private BigInteger currentMessageNumber;
    private BigInteger lastMessageNumber;
    private SequenceMonitor monitor;
    private boolean acknowledgeOnNextOccasion;
    private List<DeferredAcknowledgment> deferredAcknowledgments;


    public enum Type {
        SOURCE, DESTINATION,
    }

    private final Type type;

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

    private Sequence(Identifier i, Type t) {
        id = i;
        type = t;
        acked = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        acked.setIdentifier(id);
    }

    /**
     * Constructs a Sequence object for use in RM destinations.
     * 
     * @param i the sequence identifier
     * @param d the RM destination
     * @param a the acksTo address
     */
    public Sequence(Identifier i, RMDestination d, EndpointReferenceType a) {
        this(i, Type.DESTINATION);
        destination = d;
        acksTo = a;
        monitor = new SequenceMonitor();
    }

    /**
     * Constructs a Sequence object for use in RM sources.
     * 
     * @param i the sequence identifier
     * @param s the RM source
     * @param duration the lifetime of the sequence
     */
    public Sequence(Identifier i, RMSource s, Expires duration) {
        this(i, Type.SOURCE);
        source = s;

        Duration d = null;
        if (null != duration) {
            d = duration.getValue();
        }

        if (null != d && (null == PT0S || !PT0S.equals(d))) {
            Date now = new Date();
            expires = new Date(now.getTime() + duration.getValue().getTimeInMillis(now));

        }

        currentMessageNumber = BigInteger.ZERO;
    }

    /**
     * Returns the type of this sequence.
     * 
     * @return the sequence type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the identifier of this sequence.
     * 
     * @return the sequence identifier.
     */
    public Identifier getIdentifier() {
        return id;
    }

    /**
     * Returns the last message number.
     * 
     * @return the last message number.
     */
    public BigInteger getLastMessageNumber() {
        return lastMessageNumber;
    }    
    
    /**
     * Sets the last message number.
     * 
     * @return the last message number.
     */
    public void setLastMessageNumber(BigInteger l) {
        lastMessageNumber = l;        
    }
    
    /**
     * Returns the acksTo address for this sequence.
     * 
     * @return the acksTo address for this sequence.
     */
    EndpointReferenceType getAcksTo() {
        return acksTo;
    }   
    
    /**
     * Returns the monitor for this sequence.
     * 
     * @return the sequence monitor.
     */
    public SequenceMonitor getMonitor() {
        return monitor;
    }

    /**
     * Returns true if the sequence is expired.
     * 
     * @return true if the sequence is expired.
     */

    public boolean isExpired() {
        return expires == null ? false : new Date().after(expires);
    }

    /**
     * Returns the next message number and increases the message number.
     * 
     * @return the next message number.
     */
    public BigInteger nextMessageNumber() {
        // TODO
        assert null == lastMessageNumber;
        if (Type.DESTINATION == type) {
            return null;
        }
        BigInteger result = null;
        synchronized (this) {
            currentMessageNumber = currentMessageNumber.add(BigInteger.ONE);
            checkLastMessage();
            result = currentMessageNumber;
        }
        return result;
    }
    
    /**
     * Returns the current message number (i.e. the last message number used in a sequence).
     * @return
     */
    public BigInteger getCurrentMessageNumber() {
        return currentMessageNumber;
    }

    /**
     * Called by the RM destination upon receipt of a message with the given
     * message number for this sequence.
     * 
     * @param messageNumber the number of the received message
     */
    public void acknowledge(BigInteger messageNumber) {
        
        if (Type.SOURCE == type) {
            return;
        }
        
        monitor.acknowledgeMessage();
        
        boolean done = false;
        int i = 0;
        for (; i < acked.getAcknowledgementRange().size(); i++) {
            AcknowledgementRange r = acked.getAcknowledgementRange().get(i);
            BigInteger diff = r.getLower().subtract(messageNumber);
            if (diff.signum() == 1) {
                if (diff.equals(BigInteger.ONE)) {
                    r.setLower(messageNumber);
                    done = true;
                }
                break;
            } else if (messageNumber.subtract(r.getUpper()).equals(BigInteger.ONE)) {
                r.setUpper(messageNumber);
                done = true;
                break;
            }
        }

        if (!done) {
            AcknowledgementRange range = RMUtils.getWSRMFactory()
                .createSequenceAcknowledgementAcknowledgementRange();
            range.setLower(messageNumber);
            range.setUpper(messageNumber);
            acked.getAcknowledgementRange().add(i, range);
        }
        
        // schedule acknowledgement
        
        scheduleAcknowledgement();
    }

    /**
     * Used by the RM source to cache received acknowledgements for this
     * sequence.
     * 
     * @param acknowledgement an acknowledgement for this sequence
     */
    public void setAcknowledged(SequenceAcknowledgement acknowledgement) {
        if (Type.SOURCE == type) {
            acked = acknowledgement;
        }
        
        // TODO: if all messages are acknowledged and the sequence is closed 
        // (i.e. a LastMessage had been sent)
        // terminate this sequence
    }

    /**
     * Used by an RMDestination to obtain infomation about the range of messages
     * that have so far been acknowledged for this sequence.
     * 
     * @param hint a list of message numbers
     * @return a sequence acknowledgment.
     */
    public SequenceAcknowledgement getAcknowledged() {
        return acked;
    }

    /**
     * Used by an RMDestination to obtain infomation about the range of messages
     * that have so far been acknowledged for this sequence.
     * 
     * @param hint a list of message numbers
     * @return a sequence acknowledgment.
     */
    public SequenceAcknowledgement getAcknowledged(List<BigInteger> hint) {
        return acked;
    }

    /**
     * Checks if the message with the given number has been acknowledged.
     * 
     * @param m the message number
     * @return true of the message with the given number has been acknowledged.
     */
    public boolean isAcknowledged(BigInteger m) {
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
    public boolean allAcknowledged() {
        if (null == lastMessageNumber) {
            return false;
        }

        if (acked.getAcknowledgementRange().size() == 1) {         
            AcknowledgementRange r = acked.getAcknowledgementRange().get(0);
            return r.getLower().equals(BigInteger.ONE) && r.getUpper().equals(lastMessageNumber);
        }
        return false;
    }

    /**
     * Called after an acknowledgement header for this sequence has been added to an outgoing message.
     */
    public void acknowledgmentSent() {
        acknowledgeOnNextOccasion = false;
    }

    public boolean sendAcknowledgement() {
        return acknowledgeOnNextOccasion;
    }

    /**
     * Checks if the current message should be the last message in this sequence
     * and if so sets the lastMessageNumber property.
     */
    private void checkLastMessage() {        
        SequenceTerminationPolicyType stp = source.getSequenceTerminationPolicy();
        assert null != stp;
        
        if ((!stp.getMaxLength().equals(BigInteger.ZERO) 
            && stp.getMaxLength().compareTo(currentMessageNumber) <= 0)
            || (stp.getMaxRanges() > 0 && acked.getAcknowledgementRange().size() >= stp.getMaxRanges())
            || (stp.getMaxUnacknowledged() > 0 
                && source.getRetransmissionQueue().countUnacknowledged(this) 
                >= stp.getMaxUnacknowledged())) {            
            setLastMessageNumber(currentMessageNumber);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(currentMessageNumber + " should be the last message in this sequence.");
            }
        }
    }
   
    private void scheduleAcknowledgement() {
        AcksPolicyType ap = destination.getAcksPolicy();
        int delay = ap.getDeferredBy();
        if (delay > 0 && getMonitor().getMPM() >= ap.getIntraMessageThreshold()) {
            scheduleDeferredAcknowledgement(delay);
        } else {
            scheduleImmediateAcknowledgement();
        }
    }


    public void scheduleImmediateAcknowledgement() {
        acknowledgeOnNextOccasion = true;
    }

    private void scheduleDeferredAcknowledgement(int delay) {
        if (null == deferredAcknowledgments) {
            deferredAcknowledgments = new ArrayList<DeferredAcknowledgment>();
        }
        long now = System.currentTimeMillis();
        long expectedExecutionTime = now + delay;
        for (DeferredAcknowledgment da : deferredAcknowledgments) {
            if (da.scheduledExecutionTime() <= expectedExecutionTime) {
                return;
            }
        }
        DeferredAcknowledgment da = new DeferredAcknowledgment();
        deferredAcknowledgments.add(da);
        destination.getHandler().getTimer().schedule(da, delay);
    }

    final class DeferredAcknowledgment extends TimerTask {

        public void run() {
            Sequence.this.scheduleImmediateAcknowledgement();
        }
    }
}
