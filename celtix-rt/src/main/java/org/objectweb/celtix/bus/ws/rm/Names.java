package org.objectweb.celtix.bus.ws.rm;


/**
 * Holder for WS-RM names (of headers, namespaces etc.).
 */
public final class Names {
   
    public static final String WSRM_NAMESPACE_NAME = 
        "http://schemas.xmlsoap.org/ws/2005/02/rm";
    
    public static final String WSRM_WSDL_NAMESPACE_NAME = 
        WSRM_NAMESPACE_NAME + "/wsdl";
    
    public static final String WSRM_CREATE_SEQUENCE_ACTION =
        WSRM_NAMESPACE_NAME + "/CreateSequence";
    
    public static final String WSRM_CREATE_SEQUENCE_RESPONSE_ACTION =
        WSRM_NAMESPACE_NAME + "/CreateSequenceResponse";
    
    public static final String WSRM_CREATE_SEQUENCE_OPERATION_NAME =
        "CreateSequence";
    
    public static final String WSRM_TERMINATE_SEQUENCE_ACTION =
        WSRM_NAMESPACE_NAME + "/TerminateSequence";
    
    public static final String WSRM_TERMINATE_SEQUENCE_OPERATION_NAME =
        "TerminateSequence";

    /**
     * Prevents instantiation.
     */
    private Names() {
    }
}
