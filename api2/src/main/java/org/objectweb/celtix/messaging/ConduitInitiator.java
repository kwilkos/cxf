package org.objectweb.celtix.messaging;

import java.io.IOException;

import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

/**
 * Factory for Conduits.
 */
public interface ConduitInitiator {
    /**
     * Initiate an outbound Conduit.
     * 
     * @param targetInfo the endpoint info of the initiator 
     * @return a suitable new or pre-existing Conduit
     */
    Conduit getConduit(EndpointInfo endpointInfo) throws IOException;

    Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType target) throws IOException;
}
