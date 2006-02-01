package org.objectweb.celtix.bus.ws.rm;

import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;

public class RMSource extends RMEndpoint {
  
    private static final String SOURCE_POLICIES_PROPERTY_NAME = "sourcePolicies";
    private Sequence current;
    private final RetransmissionQueue retransmissionQueue;
    
        
    RMSource(RMHandler h) {
        super(h);
        retransmissionQueue = new RetransmissionQueue();
    }
    
    public SourcePolicyType getSourcePolicies() {
        SourcePolicyType sp = 
            (SourcePolicyType) getHandler().getConfiguration().getObject(SOURCE_POLICIES_PROPERTY_NAME);
        if (null == sp) {
            sp = RMUtils.getWSRMConfFactory().createSourcePolicyType();
        }
        return sp;
    }
    
    public SequenceTerminationPolicyType getSequenceTerminationPolicy() {
        SourcePolicyType sp = getSourcePolicies();
        assert null != sp;
        System.out.println("source policies: " + sp);
        SequenceTerminationPolicyType stp = sp.getSequenceTerminationPolicy();
        System.out.println("sequence termination policies: " + stp);
        if (null == stp) {
            stp = RMUtils.getWSRMConfFactory().createSequenceTerminationPolicyType();
        }
        System.out.println("sequence termination policies: " + stp);
        return stp;
    }
    
    public RetransmissionQueue getRetransmissionQueue() {
        return retransmissionQueue;
    }
    
    
    /**
     * Returns the current sequence
     * @return the current sequence.
     */ 
    Sequence getCurrent() {
        return current;  
    }
    
    
    
    /**
     * Creates a new Sequence object initialising it with data obtained from a CreateSequenceResponse,
     * and stores it under its sequence identifier.
     * 
     * @param id the sequence identifier.
     * @param expires the lifetime of the sequence.
     */
    void createSequence(Identifier id, EndpointReferenceType acksTo, Expires expires) {
        Sequence seq = new Sequence(id, this, acksTo, expires);
        addSequence(seq);
        current = seq;
    }
    
    /**
     * Stores the received acknowledgement in the Sequence object
     * identified in the <code>SequenceAcknowldegement</code> parameter.
     * Then evicts any acknowledged messages from the retransmission
     * queue and requests sequence termination if necessary.
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
}