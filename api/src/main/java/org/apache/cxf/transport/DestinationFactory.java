package org.apache.cxf.transport;

import java.io.IOException;

import org.apache.cxf.service.model.EndpointInfo;

/**
 * Factory for Destinations.
 */
public interface DestinationFactory {
    
    /**
     * Create a destination.
     * 
     * @param ei the endpoint info of the destination.
     * @return the created Destination.
     */
    Destination getDestination(EndpointInfo ei) throws IOException;
}
