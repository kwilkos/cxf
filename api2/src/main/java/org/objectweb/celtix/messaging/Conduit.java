package org.objectweb.celtix.messaging;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

/**
 * A pipe between peers that channels transport-level messages.
 * <p>
 * A Conduit channels messages to a <b>single</b> destination, though
 * this destination may fan-out to multiple receivers (for example a JMS topic).
 * <p>
 * A Conduit may have a back-channel, on which transport-level responses
 * are received. Alternatively the back-channel destination may be decoupled,
 * in which case the response it is received via a separate Conduit.
 * The crucial distinction is whether the Conduit can itself correlate
 * the response (which may be synchronous, or may be delivered via 
 * a dedicated destination). 
 * <p>
 * Conduits may be used for multiple messages, either serially or 
 * concurrently, with the implementation taking care of mapping onto
 * multiple transport resources (e.g. connections) if neccessary to
 * support concurrency.
 * <p>
 * Binding-level MEPs may be realized over one or more Conduits.
 */
public interface Conduit extends Observable {
    
    /**
     * Send an outbound message.
     * 
     * @param message the message to be sent.
     */
    void send(Message message);
    
    /**
     * @return the reference associated with the target Destination
     */    
    EndpointReferenceType getTarget();
    
    /**
     * Retreive the back-channel Destination.
     * 
     * @return the backchannel Destination (or null if the backchannel is
     * built-in)
     */
    Destination getBackChannel();
        
    /**
     * Close the conduit
     */
    void close();
}
