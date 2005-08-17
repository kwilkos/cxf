package org.objectweb.celtix.transports;

import org.objectweb.celtix.addressing.EndpointReferenceType;

/**
 * Transport
 * @author dkulp
 *
 */
public interface Transport {
    
    void initialize(EndpointReferenceType reference);
    
    void shutdown();

}
