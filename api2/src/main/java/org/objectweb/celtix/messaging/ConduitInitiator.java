package org.objectweb.celtix.messaging;

import java.io.IOException;

import javax.wsdl.WSDLException;

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
    Conduit getConduit(EndpointReferenceType target) throws WSDLException, IOException;
}
