package org.objectweb.celtix.ws.addressing;


import javax.xml.namespace.QName;


/**
 * Encapsulation of version-specific WS-Addressing constants.
 */
public interface AddressingConstants {
    /**
     * @return namespace defined by the normative WS-Addressing Core schema
     */
    String getNamespaceURI();
    
    /**
     * @return prefix associated with the WS-Addressing namespace
     */
    String getNamespacePrefix();
    
    /**
     * @return namespace defined by the normative WS-Addressing WSDL bindings
     * schema
     */
    String getWSDLNamespaceURI();
    
    /**
     * @return prefix associated with the WS-Addressing WSDL bindings schema
     */
    String getWSDLNamespacePrefix();
    
    /**
     * @return QName of the WSDL extensiblity element
     */
    QName getWSDLExtensibilityQName();
    
    /**
     * @return QName of the wsaw:Action element
     */
    QName getWSDLActionQName();
    
    /**
     * @return Anonymous address URI
     */
    String getAnonymousURI();
    
    /**
     * @return None address URI
     */
    String getNoneURI();
    
    /**
     * @return QName of the From addressing header
     */
    QName getFromQName();
    
    /**
     * @return QName of the To addressing header
     */
    QName getToQName();
    
    /**
     * @return QName of the ReplyTo addressing header
     */
    QName getReplyToQName();
    
    /**
     * @return QName of the FaultTo addressing header
     */
    QName getFaultToQName();
    
    /**
     * @return QName of the Action addressing header
     */
    QName getActionQName();
    
    /**
     * @return QName of the MessageID addressing header
     */
    QName getMessageIDQName();
    
    /**
     * @return Default value for RelationshipType indicating a reply 
     * to the related message
     */
    String getRelationshipReply();
    
    /**
     * @return QName of the RelatesTo addressing header
     */
    QName getRelatesToQName();
    
    /**
     * @return QName of the Relationship addressing header
     */
    QName getRelationshipTypeQName();

    /**
     * @return QName of the Metadata
     */
    QName getMetadataQName();
    
    /**
     * @return QName of the Address
     */
    QName getAddressQName();
    
    /**
     * @return package name of the implementation
     */
    String getPackageName();
    
    /**
     * @return QName of the reference parameter marker
     */
    QName getIsReferenceParameterQName();
    
    /**
     * @return QName of the Invalid Message Addressing Property fault subcode
     */
    QName getInvalidMapQName();
    
    /**
     * @return QName of the Message Addressing Property Required fault subcode
     */
    QName getMapRequiredQName();
    
    /**
     * @return QName of the Destination Unreachable fault subcode
     */
    QName getDestinationUnreachableQName();
    
    /**
     * @return QName of the Action Not Supported fault subcode
     */
    QName getActionNotSupportedQName();
    
    /**
     * @return QName of the Endpoint Unavailable fault subcode
     */
    QName getEndpointUnavailableQName();
    
    /**
     * @return Default Fault Action
     */
    String getDefaultFaultAction();
    
    /**
     * @return Action Not Supported text
     */
    String getActionNotSupportedText();

    /**
     * @return Destination Unreachable text
     */
    String getDestinationUnreachableText();

    /**
     * @return Endpoint Unavailable text
     */
    String getEndpointUnavailableText();

    /**
     * @return Invalid Message Addressing Property text
     */
    String getInvalidMapText();

    /**
     * @return Message Addressing Property Required text
     */
    String getMapRequiredText();

    /**
     * @return Duplicate Message ID text
     */
    String getDuplicateMessageIDText();
}

