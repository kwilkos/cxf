package org.objectweb.celtix.ws.rm.persistence;

import java.math.BigInteger;

import org.objectweb.celtix.ws.rm.Identifier;

public interface RMSourceSequence {
    
    /**
     * @return the sequence identifier
     */
    Identifier getIdentifier();
    
    /**
     * @return the message number assigned to the most recent outgoing application message.
     */
    BigInteger getCurrentMessageNr();
    
    /**
     * @return true if the last message had been sent for this sequence. 
     */
    boolean getLastMessage();
    
    /**
     * @return the identifier of the sequence that was created on behalf of the CreateSequence request 
     * that included this sequence as an offer
     */
    Identifier getOfferingSequenceIdentifier();
    
    /**
     * @return the identifier of the rm source
     */
    String getEndpointIdentifier();  
}
