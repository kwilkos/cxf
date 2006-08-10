package org.objectweb.celtix.messaging;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.service.model.EndpointInfo;
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
    Destination getDestination(EndpointReferenceType reference)
        throws WSDLException, IOException;

    Destination getDestination(EndpointInfo ei) throws WSDLException, IOException;
}