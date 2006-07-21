package org.objectweb.celtix.messaging;

import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

/**
 * Factory for Destinations.
 */
public interface DestinationFactory {
    
    /**
     * Create a destination.
     * 
     * @param reference the endpoint reference for the Destination.
     * @return the created Destination.
     */
    Destination getDestination(EndpointReferenceType reference);
    
}
