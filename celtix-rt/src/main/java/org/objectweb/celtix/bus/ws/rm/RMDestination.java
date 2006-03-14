package org.objectweb.celtix.bus.ws.rm;

import java.util.logging.Logger;

import org.objectweb.celtix.bus.configuration.wsrm.AcksPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.DestinationPolicyType;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceType;

public class RMDestination extends RMEndpoint {

    private static final String DESTINATION_POLICIES_PROPERTY_NAME = "destinationPolicies";
    private static final Logger LOG = LogUtils.getL7dLogger(RMDestination.class);
    
    RMDestination(RMHandler h) {
        super(h);
    }
    
    /**
     * Called by the RM destination when no sequence with the given identifier
     * exists.
     */
    public void addSequence(Identifier id, EndpointReferenceType a) {
        Sequence seq = new Sequence(id, this, a);
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
    
    public AcksPolicyType getAcksPolicy() {
        DestinationPolicyType dp = getDestinationPolicies();
        assert null != dp;
        AcksPolicyType ap = dp.getAcksPolicy();
        if (null == ap) {
            ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        }
        return ap;
    }
   
    
    /**
     * Acknowledges receipt of a sequence message.
     *
     */
    public void acknowledge(SequenceType sequenceType) {
        Sequence seq = getSequence(sequenceType.getIdentifier());
        if (null != seq) {
            seq.acknowledge(sequenceType.getMessageNumber());
        } else {
            LOG.severe("Unknown sequence: " + sequenceType.getIdentifier().getValue());
        }
    }

}
