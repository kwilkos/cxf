package org.objectweb.celtix.ws.rm;


import javax.xml.namespace.QName;

/**
 * Encapsulation of version-specific WS-RM constants.
 */
public class RMConstantsImpl implements RMConstants {

    public String getNamespaceURI() {
        return Names.WSRM_NAMESPACE_NAME;
    }
     
    public String getRMPolicyNamespaceURI() {
        return Names.WSRMP_NAMESPACE_NAME;
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
    
    public String getTerminateSequenceAction() {
        return Names.WSRM_TERMINATE_SEQUENCE_ACTION;
    }
    
    public String getLastMessageAction() {
        return Names.WSRM_LAST_MESSAGE_ACTION;
    }
    
    public String getSequenceAcknowledgmentAction() {
        return Names.WSRM_SEQUENCE_ACKNOWLEDGMENT_ACTION;
    }
    
    public String getSequenceInfoAction() {
        return Names.WSRM_SEQUENCE_INFO_ACTION;
    }
    
    public QName getUnknownSequenceFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_UNKNOWN_SEQUENCE_FAULT_CODE);
    }
        
    public QName getSequenceTerminatedFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_SEQUENCE_TERMINATED_FAULT_CODE);
    }
        
    public QName getInvalidAcknowledgmentFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_INVALID_ACKNOWLEDGMENT_FAULT_CODE);
    }
  
    public QName getMessageNumberRolloverFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_MESSAGE_NUMBER_ROLLOVER_FAULT_CODE);
    }
    
    public QName getCreateSequenceRefusedFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_CREATE_SEQUENCE_REFUSED_FAULT_CODE);
    }
    
    public QName getLastMessageNumberExceededFaultCode() {
        return new QName(Names.WSRM_NAMESPACE_NAME, Names.WSRM_LAST_MESSAGE_NUMBER_EXCEEDED_FAULT_CODE);
    }
    
}
