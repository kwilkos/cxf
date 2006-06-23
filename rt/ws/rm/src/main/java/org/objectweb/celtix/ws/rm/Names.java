package org.objectweb.celtix.ws.rm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;


/**
 * Holder for WS-RM names (of headers, namespaces etc.).
 */
public final class Names {
   
    public static final String WSRM_NAMESPACE_NAME = 
        "http://schemas.xmlsoap.org/ws/2005/02/rm";
    
    public static final String WSRMP_NAMESPACE_NAME = 
        "http://schemas.xmlsoap.org/ws/2005/02/rm/policy";
    
    public static final String WSA_NAMESPACE_NAME = 
        "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    
    public static final String WSRM_NAMESPACE_PREFIX = "wsrm";
    
    public static final String WSRM_WSDL_NAMESPACE_NAME = 
        WSRM_NAMESPACE_NAME + "/wsdl";
    
    public static final String CELTIX_WSRM_NAMESPACE_NAME = 
        "http://celtix.objectweb.org/ws/rm";
    
    public static final String CELTIX_WSRM_WSDL_NAMESPACE_NAME = 
        CELTIX_WSRM_NAMESPACE_NAME + "/wsdl";
    
    public static final String WSRM_CREATE_SEQUENCE_ACTION =
        WSRM_NAMESPACE_NAME + "/CreateSequence";
    
    public static final String WSRM_CREATE_SEQUENCE_RESPONSE_ACTION =
        WSRM_NAMESPACE_NAME + "/CreateSequenceResponse";
    
    public static final String WSRM_TERMINATE_SEQUENCE_ACTION =
        WSRM_NAMESPACE_NAME + "/TerminateSequence";
    
    public static final String WSRM_LAST_MESSAGE_ACTION =
        WSRM_NAMESPACE_NAME + "/LastMessage";
    
    public static final String WSRM_SEQUENCE_ACKNOWLEDGMENT_ACTION =
        WSRM_NAMESPACE_NAME + "/SequenceAcknowledgement";
    
    public static final String WSRM_SEQUENCE_INFO_ACTION =
        CELTIX_WSRM_NAMESPACE_NAME + "/SequenceInfo";
    
    public static final String WSRM_UNKNOWN_SEQUENCE_FAULT_CODE =
        "UnknownSequence";
    
    public static final String WSRM_SEQUENCE_TERMINATED_FAULT_CODE =
        "SequenceTerminated";
    
    public static final String WSRM_INVALID_ACKNOWLEDGMENT_FAULT_CODE =
        "InvalidAcknowledgement";
    
    public static final String WSRM_MESSAGE_NUMBER_ROLLOVER_FAULT_CODE =
        "MessageNumberRollover";
    
    public static final String WSRM_CREATE_SEQUENCE_REFUSED_FAULT_CODE =
        "CreateSequenceRefused";
    
    public static final String WSRM_LAST_MESSAGE_NUMBER_EXCEEDED_FAULT_CODE =
        "LastMessageNumberExceeded";
    
    public static final String WSRM_SEQUENCE_NAME =
        "Sequence";
    
    public static final QName WSRM_SEQUENCE_QNAME =
        new QName(WSRM_NAMESPACE_NAME, WSRM_SEQUENCE_NAME);
    
    public static final String WSRM_SEQUENCE_ACK_NAME =
        "SequenceAcknowledgement";
    
    public static final QName WSRM_SEQUENCE_ACK_QNAME =
        new QName(WSRM_NAMESPACE_NAME, WSRM_SEQUENCE_ACK_NAME);
    
    public static final String WSRM_ACK_REQUESTED_NAME =
        "AckRequested";
    
    public static final QName WSRM_ACK_REQUESTED_QNAME =
        new QName(WSRM_NAMESPACE_NAME, WSRM_ACK_REQUESTED_NAME);
    
    public static final String WSA_ANONYMOUS_ADDRESS = 
        WSA_NAMESPACE_NAME + "/anonymous";
    public static final String WSA_NONE_ADDRESS =
        WSA_NAMESPACE_NAME + "/none";
    
    
    /**
     * The set of headers understood by the protocol binding.
     */
    public static final Set<QName> HEADERS;
    static {
        Set<QName> headers = new HashSet<QName>();
        headers.add(WSRM_SEQUENCE_QNAME);
        headers.add(WSRM_SEQUENCE_ACK_QNAME);
        headers.add(WSRM_ACK_REQUESTED_QNAME);
        HEADERS = Collections.unmodifiableSet(headers);
    }
    
    protected Names() {
    }
}
