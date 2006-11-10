/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.ws.addressing;

/**
 * Abstraction of Message Addressing Properties. 
 */
public class AddressingPropertiesImpl implements AddressingProperties {
    private AttributedURIType to;
    private AttributedURIType messageID;
    private EndpointReferenceType replyTo;
    private EndpointReferenceType faultTo;
    private RelatesToType relatesTo;
    private AttributedURIType action;
    private String namespaceURI;

    /**
     * Constructor, defaults to 2005/08 namespace.
     */
    public AddressingPropertiesImpl() {
        this(Names.WSA_NAMESPACE_NAME);
    }

    /**
     * Constructor.
     * 
     * @param uri the namespace URI
     */
    public AddressingPropertiesImpl(String uri) {
        namespaceURI = uri;
    }

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
     * Accessor for the <b>FaultTo</b> property.
     * @return current value of FaultTo property
     */
    public EndpointReferenceType getFaultTo() {
        return faultTo;
    }

    /**
     * Mutator for the <b>FaultTo</b> property.
     * @param ref new value for FaultTo property
     */
    public void setFaultTo(EndpointReferenceType ref) {
        faultTo = ref;
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
    
    /**
     * Accessor for the <b>Action</b> property.
     * @return current value of Action property
     */
    public AttributedURIType getAction() {
        return action;
    }

    /**
     * Mutator for the <b>Action</b> property.
     * @param iri new value for Action property
     */
    public void setAction(AttributedURIType iri) {
        action = iri;
    }
    
    /**
     * @return WS-Addressing namespace URI
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }
    
    /**
     * Used to specify a different WS-Addressing namespace URI, 
     * so as to cause MAPs to be exposed (i.e. encoded in externalized
     * message with a different WS-Addressing version).
     * 
     * @return WS-Addressing namespace URI
     */
    public void exposeAs(String uri) {
        namespaceURI = uri;
    }
}
