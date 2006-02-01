package org.objectweb.celtix.bus.ws.rm;

import org.objectweb.celtix.ws.rm.RMConstants;

/**
 * Encapsulation of version-specific WS-RM constants.
 */
public class RMConstantsImpl implements RMConstants {

    public String getCreateSequenceAction() {
        return Names.WSRM_CREATE_SEQUENCE_ACTION;
    }

    public String getCreateSequenceResponseAction() {
        return Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION;
    }

    public String getNamespaceURI() {
        return Names.WSRM_NAMESPACE_NAME;
    }

    

    
}
