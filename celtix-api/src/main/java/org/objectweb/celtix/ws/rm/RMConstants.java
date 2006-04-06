package org.objectweb.celtix.ws.rm;

import javax.xml.namespace.QName;


public interface RMConstants {
    /**
     * @return namespace defined by the normative WS-RM schema
     */
    String getNamespaceURI();
    
    /**
     * @return namespace defined by the normative WS-RM WSDL bindings
     * schema
     */
    String getWSDLNamespaceURI();
    
    /**
     * @return CreateSequence Action
     */
    String getCreateSequenceAction();
    
    
    /**
     * @return CreateSequenceResponse Action
     */
    String getCreateSequenceResponseAction();
    
    /**
     * @return TerminateSequence Action
     */
    String getTerminateSequenceAction();
    
    /**
     * @return LastMessage Action
     */
    String getLastMessageAction();
    
    /**
     * @return SequenceAcknowledgment Action
     */
    String getSequenceAcknowledgmentAction();
    
    
    /**
     * @return SequenceInfo Action
     */
    String getSequenceInfoAction();
    
    /**
     * @return UnknownSequence fault code
     */
    QName getUnknownSequenceFaultCode();
        
    /**
     * @return SequenceTerminated fault code
     */
    QName getSequenceTerminatedFaultCode();
        
    /**
     * @return InvalidAcknowledgemt fault code
     */
    QName getInvalidAcknowledgmentFaultCode();
    
    /**
     * @return CreateSequenceRefused fault code
     */
    QName getCreateSequenceRefusedFaultCode();
    
    /**
     * @return MessageNumberRollover fault code
     */
    QName getMessageNumberRolloverFaultCode();
    
    
    /**
     * @return LastMessageNumberExceeded fault code
     */
    QName getLastMessageNumberExceededFaultCode();
  
}
