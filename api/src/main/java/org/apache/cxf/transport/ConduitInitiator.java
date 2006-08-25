package org.apache.cxf.transport;

import java.io.IOException;

import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

/**
 * Factory for Conduits.
 */
public interface ConduitInitiator {
    /**
     * Initiate an outbound Conduit.
     * 
     * @param targetInfo the endpoint info of the target 
     * @return a suitable new or pre-existing Conduit
     */
    Conduit getConduit(EndpointInfo targetInfo) throws IOException;

    /**
     * Initiate an outbound Conduit.
     * 
     * @param localInfo the endpoint info for a local endpoint on which the
     * the configuration should be based
     * @param target the target EPR
     * @return a suitable new or pre-existing Conduit
     */
    Conduit getConduit(EndpointInfo localInfo,
                       EndpointReferenceType target) throws IOException;
}
