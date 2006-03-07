package org.objectweb.celtix.ws.addressing;


/**
 * Abstraction of Message Addressing Properties. 
 */
public interface AddressingProperties extends AddressingType {
    /**
     * Accessor for the <b>To</b> property.
     * @return current value of To property
     */
    AttributedURIType getTo();

    /**
     * Mutator for the <b>To</b> property.
     * @param iri new value for To property
     */
    void setTo(AttributedURIType iri);

    /**
     * Accessor for the <b>MessageID</b> property.
     * @return current value of MessageID property
     */
    AttributedURIType getMessageID();

    /**
     * Mutator for the <b>MessageID</b> property.
     * @param iri new value for MessageID property
     */
    void setMessageID(AttributedURIType iri);

    /**
     * Accessor for the <b>ReplyTo</b> property.
     * @return current value of ReplyTo property
     */
    EndpointReferenceType getReplyTo();

    /**
     * Mutator for the <b>ReplyTo</b> property.
     * @param ref new value for ReplyTo property
     */
    void setReplyTo(EndpointReferenceType ref);

    /**
     * Accessor for the <b>RelatesTo</b> property.
     * @return current value of RelatesTo property
     */
    RelatesToType getRelatesTo();

    /**
     * Mutator for the <b>RelatesTo</b> property.
     * @param relatesTo new value for RelatesTo property
     */
    void setRelatesTo(RelatesToType relatesTo);
    
    /**
     * Accessor for the <b>Action</b> property.
     * @return current value of Action property
     */
    AttributedURIType getAction();

    /**
     * Mutator for the <b>Action</b> property.
     * @param iri new value for Action property
     */
    void setAction(AttributedURIType iri);
}
