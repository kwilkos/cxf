package org.objectweb.celtix.bus.ws.addressing;


import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.addressing.RelatesToType;


/**
 * Abstraction of Message Addressing Properties. 
 */
public class AddressingPropertiesImpl implements AddressingProperties {
    private AttributedURIType to;
    private AttributedURIType messageID;
    private EndpointReferenceType replyTo;
    private RelatesToType relatesTo;

    /**
     * Accessor for the <b>To</b> property.
     * @return To property
     */
    public AttributedURIType getTo() {
        return to;
    }

    /**
     * Mutator for the <b>To</b> property.
     * @param iri new value for To property
     */
    public void setTo(AttributedURIType iri) {
        to = iri;
    }

    /**
     * Accessor for the <b>MessageID</b> property.
     * @return current value of MessageID property
     */
    public AttributedURIType getMessageID() {
        return messageID;
    }

    /**
     * Mutator for the <b>MessageID</b> property.
     * @param iri new value for MessageTo property
     */
    public void setMessageID(AttributedURIType iri) {
        messageID = iri;
    }

    /**
     * Accessor for the <b>ReplyTo</b> property.
     * @return current value of ReplyTo property
     */
    public EndpointReferenceType getReplyTo() {
        return replyTo;
    }

    /**
     * Mutator for the <b>ReplyTo</b> property.
     * @param ref new value for ReplyTo property
     */
    public void setReplyTo(EndpointReferenceType ref) {
        replyTo = ref;
    }

    /**
     * Accessor for the <b>RelatesTo</b> property.
     * @return current value of RelatesTo property
     */
    public RelatesToType getRelatesTo() {
        return relatesTo;
    }

    /**
     * Mutator for the <b>RelatesTo</b> property.
     * @param rel new value for RelatesTo property
     */
    public void setRelatesTo(RelatesToType rel) {
        relatesTo = rel;
    }
}
