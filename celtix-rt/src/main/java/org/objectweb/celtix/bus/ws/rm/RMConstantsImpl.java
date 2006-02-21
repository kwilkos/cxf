package org.objectweb.celtix.bus.ws.rm;


import org.objectweb.celtix.ws.rm.RMConstants;

/**
 * Encapsulation of version-specific WS-RM constants.
 */
public class RMConstantsImpl implements RMConstants {

    public String getNamespaceURI() {
        return Names.WSRM_NAMESPACE_NAME;
    }
    
    public String getWSDLNamespaceURI() {
        return Names.WSRM_WSDL_NAMESPACE_NAME;
    }
    
    public String getCreateSequenceAction() {
        return Names.WSRM_CREATE_SEQUENCE_ACTION;
    }

    public String getCreateSequenceResponseAction() {
        return Names.WSRM_CREATE_SEQUENCE_RESPONSE_ACTION;
    }

    public String getCreateSequenceOperationName() {
        return Names.WSRM_CREATE_SEQUENCE_OPERATION_NAME;
    }
    
    public String getTerminateSequenceAction() {
        return Names.WSRM_TERMINATE_SEQUENCE_ACTION;
    }
    
    public String getTerminateSequenceResponseAction() {
        return Names.WSRM_TERMINATE_SEQUENCE_RESPONSE_ACTION;
    }
    
    public String getTerminateSequenceOperationName() {
        return Names.WSRM_TERMINATE_SEQUENCE_OPERATION_NAME;
    }
    
    public String getSequenceInfoAction() {
        return Names.WSRM_SEQUENCE_INFO_ACTION;
    }
    
    public String getSequenceInfoResponseAction() {
        return Names.WSRM_SEQUENCE_INFO_RESPONSE_ACTION;
    }
    
    public String getSequenceInfoOperationName() {
        return Names.WSRM_SEQUENCE_INFO_OPERATION_NAME;
    }
    
    
    

   

    

    
}
