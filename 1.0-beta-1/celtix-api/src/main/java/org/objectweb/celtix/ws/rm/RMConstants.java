package org.objectweb.celtix.ws.rm;


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
     * @return CreateSequence operation name
     */
    String getCreateSequenceOperationName();
    
    /**
     * @return TerminateSequence Action
     */
    String getTerminateSequenceAction();
    
    /**
     * @return TerminateSequence operation name
     */
    String getTerminateSequenceOperationName();
    
    /**
     * @return SequenceInfo Action
     */
    String getSequenceInfoAction();
    
    /**
     * @return SequenceInfo operation name
     */
    /*
    String getSequenceInfoOperationName();  
    */
}
