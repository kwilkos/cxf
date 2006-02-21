package org.objectweb.celtix.bus.ws.rm;

import org.objectweb.celtix.bus.configuration.wsrm.DestinationPolicyType;
import org.objectweb.celtix.ws.addressing.addressing200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceType;

public class RMDestination extends RMEndpoint {

    private static final String DESTINATION_POLICIES_PROPERTY_NAME = "destinationPolicies";
    
    RMDestination(RMHandler h) {
        super(h);
    }
    
    /**
     * Called by the RM destination when no sequence with the given identifier
     * exists.
     */
    public void addSequence(Identifier id, EndpointReferenceType a) {
        Sequence seq = new Sequence(id, a);
        addSequence(seq);
    }
    
    
    public DestinationPolicyType getDestinationPolicies() {
        DestinationPolicyType dp = getHandler().getConfiguration()
            .getObject(DestinationPolicyType.class, DESTINATION_POLICIES_PROPERTY_NAME);
        if (null == dp) {
            dp = RMUtils.getWSRMConfFactory().createDestinationPolicyType();
        }
        return dp;
    }
   
    
    /**
     * Acknowledges receipt of a sequence message.
     *
     */
    public void acknowledge(SequenceType sequenceType) {
        Sequence seq = getSequence(sequenceType.getIdentifier());
        if (null != seq) {
            seq.acknowledge(sequenceType.getMessageNumber());
        }
    }

}