package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.bus.configuration.wsrm.AcksPolicyType;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;
import org.objectweb.celtix.ws.rm.SequenceFaultType;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

public class DestinationSequence extends AbstractSequenceImpl implements RMDestinationSequence {

    private static final Logger LOG = LogUtils.getL7dLogger(DestinationSequence.class);

    private SequenceAcknowledgement acked;

    private RMDestination destination;
    private EndpointReferenceType acksTo;
    private BigInteger lastMessageNumber;
    private SequenceMonitor monitor;
    private boolean acknowledgeOnNextOccasion;
    private List<DeferredAcknowledgment> deferredAcknowledgments;
    
    public DestinationSequence(Identifier i, EndpointReferenceType a, RMDestination d) {
        this(i, a, null, null);
        setDestination(d);
    }
    
    public DestinationSequence(Identifier i, EndpointReferenceType a,
                              BigInteger lmn, SequenceAcknowledgement ac) {
        super(i);
        acksTo = a;
        lastMessageNumber = lmn;
        acked = ac;
        if (null == acked) {
            acked = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
            acked.setIdentifier(id);
        }
        monitor = new SequenceMonitor();
    }

    
    // RMDestinationSequence interface
    
    
    /**
     * @return the acksTo address for the sequence
     */
    public EndpointReferenceType getAcksTo() {
        return acksTo;
    }
    
    /**
     * @return the message number of the last message or null if the last message had not been received.
     */
    public BigInteger getLastMessageNr() {
        return lastMessageNumber;
    }
    
    /**
     * @return the sequence acknowledgement presenting the sequences thus far received by a destination 
     */
    public SequenceAcknowledgement getAcknowledgment() {
        return acked;
    }
    
    /**
     * @return the sequence acknowledgement presenting the sequences thus far received by a destination
     * as an input stream 
     */
    public InputStream getAcknowledgmentAsStream() {
        return RMUtils.getPersistenceUtils().getAcknowledgementAsInputStream(acked);
    }
    
    /**
     * @return the identifier of the rm destination
     */
    public String getEndpointIdentifier() {
        if (null != destination) {
            return destination.getEndpointId();
        }
        return null;
    }
    
    // end RMDestinationSequence interface
    
    final void setDestination(RMDestination d) {
        destination = d;
    }
    
    RMDestination getDestination() {
        return destination;
    }
    
    void setLastMessageNumber(BigInteger lmn) {
        lastMessageNumber = lmn;
    }
   
    
    
    /**
     * Returns the monitor for this sequence.
     * 
     * @return the sequence monitor.
     */
    SequenceMonitor getMonitor() {
        return monitor;
    }
    

    /**
     * Called by the RM destination upon receipt of a message with the given
     * message number for this sequence.
     * 
     * @param messageNumber the number of the received message
     * @param lastMessage true if this is to be the last message in the sequence
     */
    void acknowledge(BigInteger messageNumber) throws SequenceFault {
        
        if (null != lastMessageNumber && messageNumber.compareTo(lastMessageNumber) > 0) {
            SequenceFaultType sf = RMUtils.getWSRMFactory().createSequenceFaultType();
            sf.setFaultCode(RMUtils.getRMConstants().getLastMessageNumberExceededFaultCode());
            Message msg = new Message("LAST_MESSAGE_NUMBER_EXCEEDED_EXC", LOG, this);
            throw new SequenceFault(msg.toString(), sf);
        }
        
        monitor.acknowledgeMessage();
        
        boolean done = false;
        int i = 0;
        for (; i < acked.getAcknowledgementRange().size(); i++) {
            AcknowledgementRange r = acked.getAcknowledgementRange().get(i);
            if (r.getLower().compareTo(messageNumber) <= 0 
                && r.getUpper().compareTo(messageNumber) >= 0) {
                done = true;
                break;
            } else {
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
        }

        if (!done) {
            AcknowledgementRange range = RMUtils.getWSRMFactory()
                .createSequenceAcknowledgementAcknowledgementRange();
            range.setLower(messageNumber);
            range.setUpper(messageNumber);
            acked.getAcknowledgementRange().add(i, range);
        }
               
        scheduleAcknowledgement();
    }


    /**
     * Called after an acknowledgement header for this sequence has been added to an outgoing message.
     */
    void acknowledgmentSent() {
        acknowledgeOnNextOccasion = false;
    }

    boolean sendAcknowledgement() {
        return acknowledgeOnNextOccasion;
    }
    
    boolean canPiggybackAckOnPartialResponse() {
        // TODO: should also check if we allow breaking the WI Profile rule by which no headers
        // can be included in a HTTP response
        return getAcksTo().getAddress().getValue().equals(Names.WSA_ANONYMOUS_ADDRESS);
    }
    
    static SequenceFault createUnknownSequenceFault(Identifier sid) {
        SequenceFaultType sf = RMUtils.getWSRMFactory().createSequenceFaultType();
        sf.setFaultCode(RMUtils.getRMConstants().getUnknownSequenceFaultCode());
        Message msg = new Message("UNKNOWN_SEQUENCE_EXC", LOG, sid.getValue());
        return new SequenceFault(msg.toString(), sf);
    }
   
    private void scheduleAcknowledgement() {
        RMAssertionType rma = destination.getRMAssertion();
        int delay = 0;
        if (null != rma.getAcknowledgementInterval()) {
            delay = rma.getAcknowledgementInterval().getMilliseconds().intValue();
        }
        AcksPolicyType ap = destination.getAcksPolicy();
        if (delay > 0 && getMonitor().getMPM() >= ap.getIntraMessageThreshold()) {
            scheduleDeferredAcknowledgement(delay);
        } else {
            scheduleImmediateAcknowledgement();
        }
    }


    void scheduleImmediateAcknowledgement() {
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
            DestinationSequence.this.scheduleImmediateAcknowledgement();
            try {
                destination.getHandler().getProxy().acknowledge(DestinationSequence.this);
            } catch (IOException ex) {
                Message msg = new Message("SEQ_ACK_SEND_EXC", LOG, DestinationSequence.this);
                LOG.log(Level.SEVERE, msg.toString(), ex);
            }
        }
    }
}
