package org.objectweb.celtix.transports;

import org.objectweb.celtix.addressing.EndpointReferenceType;

/**
 * ServerTransport
 * @author dkulp
 *
 */
public interface ServerTransport extends Transport {
    
    /**
     * Intialize the transport with e.g Configuration
     * @param address - the WS-Addressing address the represents the location for this transport.
     *                  The transport should update the address to reflect the transient location.
     */
    void initializeTransient(EndpointReferenceType address);

    /**
     * activate the server transport, involves starting listeners or creating of message queues.
     * @param callback - The call back object that the transport calls when there is a message to 
     *                   dispatch
     */
    void activate(ServerTransportCallback callback);

    /**
     * deactivate the server transport, involves stopping the listeners or message queues.
     * subsequently the transport could be activated using activate call. 
     */
    void deactivate();

}
