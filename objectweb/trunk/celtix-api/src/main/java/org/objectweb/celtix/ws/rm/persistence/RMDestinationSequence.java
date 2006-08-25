package org.objectweb.celtix.ws.rm.persistence;

import java.io.InputStream;
import java.math.BigInteger;

import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;

public interface RMDestinationSequence {
    
    /**
     * @return the sequence identifier
     */
    Identifier getIdentifier();
    
    /**
     * @return the acksTo address for the sequence
     */
    EndpointReferenceType getAcksTo();
    
    /**
     * @return the message number of the last message or null if the last message had not been received.
     */
    BigInteger getLastMessageNr();
    
    /**
     * @return the sequence acknowledgement presenting the sequences thus far received by a destination 
     */
    SequenceAcknowledgement getAcknowledgment();
    
    /**
     * @return the sequence acknowledgement presenting the sequences thus far received by a destination
     * as an input stream 
     */
    InputStream getAcknowledgmentAsStream();
    
    /**
     * @return the identifier of the rm destination
     */
    String getEndpointIdentifier(); 
    
    
}
