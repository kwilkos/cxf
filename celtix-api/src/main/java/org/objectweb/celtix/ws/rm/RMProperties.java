package org.objectweb.celtix.ws.rm;

import java.math.BigInteger;

import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

/**
 * Abstraction of WS-RM Properties. 
 */
public interface RMProperties {
    
    /**
     * Accessor for the <b>Sequence Identifier</b> property.
     * @return current value of Sequence Identifier property
     */
    Identifier getSequenceId();
    
    /**
     * Mutator for the <b>Sequence Identifier</b> property.
     * @param sid new value for Sequence Identifier property
     */
    void setSequenceId(Identifier sid);
    
    /**
     * Accessor for the <b>Message Number</b> property.
     * @return current value of Message Number property
     */
    BigInteger getMessageNumber();
    
    /**
     * Mutator for the <b>Message Number</b> property.
     * @param sid new value for Sequence Identifier property
     */
    void setMessageNumber(BigInteger m);
    
    /**
     * Accessor for the <b>AcksTo</b> property.
     * @return current value of AcksTo property
     */
    EndpointReferenceType getAcksTo();
    
    /**
     * Mutator for the <b>AcksTo</b> property.
     * @param sid new value for AcksTo property
     */
    void setAcksTo(EndpointReferenceType a);
}
