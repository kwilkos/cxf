package org.objectweb.celtix.ws.rm;

import java.util.Collection;

/**
 * Abstraction of Reliable Messaging Properties. 
 */

public interface RMProperties {
    
    /**
     * Accessor for the <b>Sequence</b> property.
     * @return current value of Sequence property
     */
    SequenceType getSequence();
    
    /**
     * Mutator for the <b>Sequence</b> property.
     * @param st new value for Sequence property
     */
    void setSequence(SequenceType st);
    
    /**
     * Accessor for the <b>Acks</b> property.
     * @return current value of Acks property
     */
    Collection<SequenceAcknowledgement> getAcks();
    
    /**
     * Mutator for the <b>Acks</b> property.
     * @param acks new value for Acks property
     */
    void setAcks(Collection<SequenceAcknowledgement> acks);
    
    /**
     * Accessor for the <b>AcksRequested</b> property.
     * @return current value of AcksRequested property
     */
    Collection<AckRequestedType> getAcksRequested();
    
    /**
     * Mutator for the <b>AcksRequested</b> property.
     * @param acks new value for AcksRequested property
     */
    void setAcksRequested(Collection<AckRequestedType> acks);    

}
