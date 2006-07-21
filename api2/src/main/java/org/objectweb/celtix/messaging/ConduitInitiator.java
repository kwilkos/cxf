package org.objectweb.celtix.messaging;

import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

/**
 * Factory for Conduits.
 */
public interface ConduitInitiator {
    /**
     * Initiate an oubound Conduit.
     * 
     * @param target the target endpoint
     * @return a suitable new or pre-existing Conduit
     */
    Conduit getConduit(EndpointReferenceType target);
}
