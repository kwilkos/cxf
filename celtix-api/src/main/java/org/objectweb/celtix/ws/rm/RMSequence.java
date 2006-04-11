package org.objectweb.celtix.ws.rm;

import java.math.BigInteger;

import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;

public interface RMSequence {
    
    /**
     * @return the sequence identifier
     */
    Identifier getIdentifier();
    
    /**
     * @return the message number assigned to the most recent outgoing application message.
     */
    BigInteger getCurrentMessageNr();
    
    /**
     * @return the number of the message that was designated the last message or null if the last message
     * had not yet been sent
     */
    BigInteger getLastMessageNr();
    
    /**
     * @return the acksTo address for the sequence
     */
    EndpointReferenceType getAcksTo();
    
    /**
     * @return the sequence acknowledgement presenting the sequences thus far received by a destination 
     */
    SequenceAcknowledgement getAcknowledgment();
    
    /**
     * @return the identifier of the sequence that was created on behalf of the CreateSequence request 
     * that included this sequence as an offer
     */
    Identifier getOfferingSequenceIdentifier();
    
    /**
     * @return the identifier of the rm endpoint
     */
    String getEndpointIdentifier();
    
    /**
     * @return true if the endpoint associated with this sequence is an RM destination
     */
    boolean isDestination();  
}
