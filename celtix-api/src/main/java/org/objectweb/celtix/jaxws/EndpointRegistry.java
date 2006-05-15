package org.objectweb.celtix.jaxws;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

/**
 * Provider the EndpointRegistry interface for bus
 *
 */
public interface EndpointRegistry {
    
    /**
     * Register the Endpoint to Registry
     * @param ep
     * Endpoint reference
     */
    void registerEndpoint(Endpoint ep);
    
    /**
     * Unregister the Endpoint from Registry and try to stop the Endpoint
     * @param ep
     * Endpoint reference
     */
    void unregisterEndpoint(Endpoint ep);
    
    /**
     * Shuts down the Registry and stops the Endpoints in the registry
     */
    void shutdown();
    
    /**
     * @param epr keyed @{link EndpointReferenceType}.
     * @return Returns the Endpoint referenced by the given {@link EndpointReferenceType}.
     */
    Endpoint getEndpoint(EndpointReferenceType epr);


}
