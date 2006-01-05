package org.objectweb.celtix.bus.ws.addressing;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;


/**
 * Holder for WS-Addressing names (of headers, namespaces etc.).
 */
public final class Names {
   
    public static final String WSA_NAMESPACE_NAME = 
        "http://www.w3.org/2005/08/addressing";
    public static final String WSA_NAMESPACE_WSDL_NAME = 
        WSA_NAMESPACE_NAME + "/wsdl";
    public static final String WSA_NAMESPACE_PREFIX = "wsa";
    public static final String WSA_NAMESPACE_WSDL_PREFIX = "wsaw";

    public static final String WSA_REFERENCE_PARAMETERS_NAME = 
        "ReferenceParameters";
    public static final QName WSA_REFERENCE_PARAMETERS_QNAME = 
        new QName(WSA_NAMESPACE_NAME, WSA_REFERENCE_PARAMETERS_NAME);
    
    public static final String WSA_IS_REFERENCE_PARAMETER_NAME = 
        "IsReferenceParameter";
    public static final QName WSA_IS_REFERENCE_PARAMETER_QNAME =
        new QName(WSA_NAMESPACE_NAME, WSA_IS_REFERENCE_PARAMETER_NAME);
    
    public static final String WSA_ADDRESS_NAME = "Address";
    public static final QName WSA_ADDRESS_QNAME = 
        new QName(WSA_NAMESPACE_NAME, WSA_ADDRESS_NAME);
    
    public static final String WSA_METADATA_NAME = "Metadata";
    public static final QName WSA_METADATA_QNAME = 
        new QName(WSA_NAMESPACE_NAME, WSA_METADATA_NAME);
    
    public static final String WSA_FROM_NAME = "From";
    public static final QName WSA_FROM_QNAME = 
        new QName(WSA_NAMESPACE_NAME, WSA_FROM_NAME);

    public static final String WSA_TO_NAME = "To";
    public static final QName WSA_TO_QNAME = 
        new QName(WSA_NAMESPACE_NAME, WSA_TO_NAME);
    
    public static final String WSA_REPLYTO_NAME = "ReplyTo";
    public static final QName WSA_REPLYTO_QNAME = 
        new QName(WSA_NAMESPACE_NAME, WSA_REPLYTO_NAME);

    public static final String WSA_FAULTTO_NAME = "FaultTo";
    public static final QName WSA_FAULTTO_QNAME = 
        new QName(WSA_NAMESPACE_NAME, "FaultTo");
    
    public static final String WSA_ACTION_NAME = "Action";
    public static final QName WSA_ACTION_QNAME = 
        new QName(WSA_NAMESPACE_NAME, WSA_ACTION_NAME);

    public static final String WSA_MESSAGEID_NAME = "MessageID";
    public static final QName WSA_MESSAGEID_QNAME = 
        new QName(WSA_NAMESPACE_NAME, WSA_MESSAGEID_NAME);    
    
    public static final String WSA_RELATIONSHIP_REPLY = 
        WSA_NAMESPACE_NAME + "/reply";
    
    public static final String WSA_RELATESTO_NAME = "RelatesTo";
    public static final QName WSA_RELATESTO_QNAME =
        new QName(WSA_NAMESPACE_NAME, WSA_RELATESTO_NAME);

    public static final String WSA_RELATIONSHIPTYPE_NAME = "RelationshipType";
    public static final QName WSA_RELATIONSHIPTYPE_QNAME = 
        new QName(WSA_NAMESPACE_NAME, WSA_RELATIONSHIPTYPE_NAME);
    
    public static final String WSA_ANONYMOUS_ADDRESS = 
        WSA_NAMESPACE_NAME + "/anonymous";
    public static final String WSA_NONE_ADDRESS =
        WSA_NAMESPACE_NAME + "/none";

    public static final String WSA_DEFAULT_FAULT_ACTION =
        WSA_NAMESPACE_NAME + "/fault";
    
    public static final String WSAW_ACTION_NAME = "Action";
    public static final QName WSAW_ACTION_QNAME = 
        new QName(WSA_NAMESPACE_WSDL_NAME, WSAW_ACTION_NAME);
    
    public static final String WSAW_USING_ADDRESSING_NAME = "UsingAddressing";
    public static final QName WSAW_USING_ADDRESSING_QNAME = 
        new QName(WSA_NAMESPACE_WSDL_NAME, WSAW_USING_ADDRESSING_NAME);
    
    public static final String WSDL_INSTANCE_NAMESPACE_NAME = 
        "http://www.w3.org/2004/08/wsdl-instance";

    public static final String INVALID_MAP_NAME =
        "InvalidMessageAddressingProperty";    
    public static final QName INVALID_MAP_QNAME = 
        new QName(WSA_NAMESPACE_NAME, INVALID_MAP_NAME);
    public static final String MAP_REQUIRED_NAME =
        "MessageAddressingPropertyRequired";
    public static final QName MAP_REQUIRED_QNAME = 
        new QName(WSA_NAMESPACE_NAME, MAP_REQUIRED_NAME);
    public static final String DESTINATION_UNREACHABLE_NAME =
        "DestinationUnreachable";
    public static final QName DESTINATION_UNREACHABLE_QNAME = 
        new QName(WSA_NAMESPACE_NAME, DESTINATION_UNREACHABLE_NAME);
    public static final String ACTION_NOT_SUPPORTED_NAME =
        "ActionNotSupported";
    public static final QName ACTION_NOT_SUPPORTED_QNAME = 
        new QName(WSA_NAMESPACE_NAME, ACTION_NOT_SUPPORTED_NAME);
    public static final String ENDPOINT_UNAVAILABLE_NAME =
        "EndpointUnavailable";
    public static final QName ENDPOINT_UNAVAILABLE_QNAME = 
        new QName(WSA_NAMESPACE_NAME, ENDPOINT_UNAVAILABLE_NAME);

    public static final String DUPLICATE_MESSAGE_ID_NAME =
        "DuplicateMessageID";
    public static final QName DUPLICATE_MESSAGE_ID_QNAME =
        new QName(WSA_NAMESPACE_NAME, DUPLICATE_MESSAGE_ID_NAME);
    
    public static final String SOAP11HTTP_ADDRESSING_BINDING = 
        "http://schemas.xmlsoap.org/soap/envelope/?addressing=ms";
    public static final String SOAP12HTTP_ADDRESSING_BINDING = 
        "http://www.w3.org/2003/05/soap-envelope?addressing=ms";
    
    /**
     * The set of headers understood by the protocol binding.
     */
    public static final Set<QName> HEADERS;
    static {
        Set<QName> headers = new HashSet<QName>();
        headers.add(WSA_FROM_QNAME);
        headers.add(WSA_TO_QNAME);
        headers.add(WSA_REPLYTO_QNAME);
        headers.add(WSA_FAULTTO_QNAME);
        headers.add(WSA_ACTION_QNAME);
        headers.add(WSA_MESSAGEID_QNAME);
        HEADERS = Collections.unmodifiableSet(headers);
    }

    /**
     * Prevents instantiation.
     */
    private Names() {
    }
}
