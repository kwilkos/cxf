package org.objectweb.celtix.messaging;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

/**
 * A Destination is a transport-level endpoint capable of receiving
 * unsolicited incoming messages from different peers.
 */
public interface Destination extends Observable {
    
    /**
     * @return the reference associated with this Destination
     */    
    EndpointReferenceType getAddress();
    
    /**
     * Retreive a back-channel Conduit, which must be policy-compatible
     * with the current Message and associated Destination. For example
     * compatible Quality of Protection must be asserted on the back-channel.
     * This would generally only be an issue if the back-channel is decoupled.
     * 
     * @param message the current message (null to indicate a disassociated
     * back-channel.
     * @param address the backchannel address (null to indicate anonymous)
     * @return a suitable Conduit
     */
    Conduit getBackChannel(Message message, EndpointReferenceType address);

    /**
     * Shutdown the Destination, i.e. stop accepting incoming messages.
     */
    void shutdown();
}
