package org.objectweb.celtix.bus.ws.rm;

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
    public void addSequence(Identifier id) {
        Sequence seq = new Sequence(id);
        addSequence(seq);
    }
    
    public Object getDestinationPolicies() {
        return getHandler().getConfiguration().getObject(DESTINATION_POLICIES_PROPERTY_NAME);
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