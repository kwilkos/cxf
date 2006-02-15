package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.ws.rm.CreateSequenceResponseType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;

public class RMSource extends RMEndpoint {

    private static final String SOURCE_POLICIES_PROPERTY_NAME = "sourcePolicies";
    private Sequence current;
    private final RetransmissionQueue retransmissionQueue;
    private Set<Identifier> offeredIdentifiers;

    RMSource(RMHandler h) {
        super(h);
        retransmissionQueue = new RetransmissionQueue();
        offeredIdentifiers = new HashSet<Identifier>();
    }

    public SourcePolicyType getSourcePolicies() {
        SourcePolicyType sp = (SourcePolicyType)getHandler().getConfiguration()
            .getObject(SOURCE_POLICIES_PROPERTY_NAME);
        if (null == sp) {
            sp = RMUtils.getWSRMConfFactory().createSourcePolicyType();
        }
        return sp;
    }

    public SequenceTerminationPolicyType getSequenceTerminationPolicy() {
        SourcePolicyType sp = getSourcePolicies();
        assert null != sp;
        SequenceTerminationPolicyType stp = sp.getSequenceTerminationPolicy();
        if (null == stp) {
            stp = RMUtils.getWSRMConfFactory().createSequenceTerminationPolicyType();
        }
        return stp;
    }

    public RetransmissionQueue getRetransmissionQueue() {
        return retransmissionQueue;
    }

    /**
     * Returns the current sequence
     * 
     * @return the current sequence.
     */
    Sequence getCurrent() {
        return current;
    }

    /**
     * Creates a new Sequence object initialising it with data obtained from a
     * CreateSequenceResponse, and stores it under its sequence identifier.
     * 
     * @param id the sequence identifier.
     * @param expires the lifetime of the sequence.
     */
    void createSequence() {
        CreateSequenceResponseType createSequenceResponse = null;
        try {
            createSequenceResponse = getHandler().getService().createSequence(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Sequence seq = new Sequence(createSequenceResponse.getIdentifier(), this, createSequenceResponse
            .getExpires());
        addSequence(seq);
        current = seq;
    }

    /**
     * Create a copy of the message, store it in the retransmission queue and
     * schedule the next transmission
     * 
     * @param context
     */
    public void addUnacknowledged(MessageContext context) {

        MessageContext clone = getHandler().getBinding().createObjectContext();
        clone.putAll(context);
        getRetransmissionQueue().put(clone);
        // TODO: schedule retransmission
    }

    /**
     * Stores the received acknowledgement in the Sequence object identified in
     * the <code>SequenceAcknowldegement</code> parameter. Then evicts any
     * acknowledged messages from the retransmission queue and requests sequence
     * termination if necessary.
     * 
     * @param acknowledgement
     */
    public void setAcknowledged(SequenceAcknowledgement acknowledgement) {
        Identifier sid = acknowledgement.getIdentifier();
        Sequence seq = getSequence(sid);
        if (null != seq) {
            seq.setAcknowledged(acknowledgement);
            retransmissionQueue.evict(seq);
        }
    }

    public Identifier offer() {
        Identifier sid = generateSequenceIdentifier();
        offeredIdentifiers.add(sid);
        return sid;
    }
}
