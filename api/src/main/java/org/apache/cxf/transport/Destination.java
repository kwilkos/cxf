package org.apache.cxf.transport;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.apache.cxf.message.Message;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

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
     * @param inMessage the current message (null to indicate a disassociated
     * back-channel.
     * @param partialResponse in the decoupled case, this is expected to be the
     * outbound Message to be sent over the in-built back-channel. 
     * @param address the backchannel address (null to indicate anonymous)
     * @return a suitable Conduit
     */
    Conduit getBackChannel(Message inMessage,
                           Message partialResponse,
                           EndpointReferenceType address)
        throws WSDLException, IOException;

    /**
     * Shutdown the Destination, i.e. stop accepting incoming messages.
     */
    void shutdown();
}
