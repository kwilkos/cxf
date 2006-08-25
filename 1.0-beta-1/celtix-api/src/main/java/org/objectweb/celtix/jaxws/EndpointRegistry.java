package org.objectweb.celtix.jaxws;

import javax.xml.ws.Endpoint;

/**
 * Provider the EndpointRegistry interface for bus
 *
 */
public interface EndpointRegistry {
    
    /**
     * Register the Endpoint to Registry ,
     * @param ep
     * Endpoint reference
     */
    void registerEndpoint(Endpoint ep);
    
    /**
     * Unregister the Endpoint to Registry ,and try to stop the Endpoint
     * @param ep
     * Endpoint reference
     */
    void unregisterEndpoint(Endpoint ep);
    
    /**
     * Shuts down the Registry, stop the Endpoint in the registry
     * @param ep
     * Endpoint reference
     */
    void shutdown();

}
