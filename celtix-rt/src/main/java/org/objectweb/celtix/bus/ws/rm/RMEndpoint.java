package org.objectweb.celtix.bus.ws.rm;

// import java.math.BigInteger;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.objectweb.celtix.bus.configuration.wsrm.EndpointPolicyType;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType.BaseRetransmissionInterval;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType.ExponentialBackoff;

public class RMEndpoint {

    private static final String POLICIES_PROPERTY_NAME = "policies";
    private static final String RMASSERTION_PROPERTY_NAME = "rmAssertion";
    
    protected Map<String, Sequence> map;
    private RMHandler handler;
    

    protected RMEndpoint(RMHandler h) {
        handler = h;
        map = new HashMap<String, Sequence>();
    }
    
    
    public RMHandler getHandler() {
        return handler;
    }
    
    public RMAssertionType getRMAssertion() {
        RMAssertionType a = getHandler().getConfiguration()
            .getObject(RMAssertionType.class, RMASSERTION_PROPERTY_NAME);        

        // the following should not be necessary as the rm handler configuration metadata 
        // supplies a default value for the RMAssertion
        
        if (null == a) {
            a = RMUtils.getWSRMPolicyFactory().createRMAssertionType();
            RMUtils.getWSRMPolicyFactory().createRMAssertionType();
        }
        
        if (null == a.getBaseRetransmissionInterval()) {
            BaseRetransmissionInterval bri = 
                RMUtils.getWSRMPolicyFactory().createRMAssertionTypeBaseRetransmissionInterval();
            bri.setMilliseconds(new BigInteger(RetransmissionQueue.DEFAULT_BASE_RETRANSMISSION_INTERVAL));
            a.setBaseRetransmissionInterval(bri);
        }
        
        if (null == a.getExponentialBackoff()) {
            ExponentialBackoff eb  = 
                RMUtils.getWSRMPolicyFactory().createRMAssertionTypeExponentialBackoff();
            a.setExponentialBackoff(eb);
        }
        Map<QName, String> otherAttributes = a.getExponentialBackoff().getOtherAttributes();
        String val = otherAttributes.get(RetransmissionQueue.EXPONENTIAL_BACKOFF_BASE_ATTR);
        if (null == val) {
            otherAttributes.put(RetransmissionQueue.EXPONENTIAL_BACKOFF_BASE_ATTR, 
                                RetransmissionQueue.DEFAULT_EXPONENTIAL_BACKOFF);
        }  

        return a;
    }
    

    /**
     * Generates and returns a sequence identifier.
     * 
     * @return the sequence identifier.
     */
    public Identifier generateSequenceIdentifier() {
        String sequenceID = ContextUtils.generateUUID();
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue(sequenceID);        
        return sid;
    }

    public EndpointPolicyType getPolicies() {
        return (EndpointPolicyType)handler.getConfiguration().getObject(POLICIES_PROPERTY_NAME);
    }

    /**
     * Returns the sequence with the given identifier,
     * 
     * @param id the sequence identifier.
     * @return the sequence.
     */
    public Sequence getSequence(Identifier id) {        
        return map.get(id.getValue());
    }
    
    /**
     * Returns the collection of all sequences currently maintained by this source.
     * 
     * @return the collection of sequences
     */
    public Collection<Sequence> getAllSequences() {
        return map.values();
    }
    
    /**
     * Returns a collection of all sequences for which have not yet been
     * completely acknowledged.
     * 
     * @return the collection of unacknowledged sequences.
     */
    public Collection<Sequence> getAllUnacknowledgedSequences() {
        Collection<Sequence> seqs = new ArrayList<Sequence>();
        for (Sequence seq : map.values()) {
            if (!seq.allAcknowledged()) {
                seqs.add(seq);
            }
        }        
        return seqs;        
    }

    /**
     * Stores the sequence under its sequence identifier.
     * 
     * @param id the sequence identifier.
     * @param seq the sequence.
     */
    public void addSequence(Sequence seq) {
        map.put(seq.getIdentifier().getValue(), seq);
    }
    
    /**
     * Removes the sequence.
     * 
     * @param seq the sequence to be removed.
     */
    public void removeSequence(Sequence seq) {
        map.remove(seq.getIdentifier());
    }
}
