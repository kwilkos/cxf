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

package org.apache.cxf.transport;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;

/**
 * Abstract base class factoring out common Destination logic, 
 * allowing non-decoupled transports to be written without any
 * regard for the decoupled back-channel or partial response logic.
 */
public abstract class AbstractDestination implements Destination {

    protected final EndpointReferenceType reference;
    protected final EndpointInfo endpointInfo;
    protected MessageObserver incomingObserver;
    
    public AbstractDestination(EndpointReferenceType ref,
                               EndpointInfo ei) {
        reference = ref;
        endpointInfo = ei;
    }
    
    /**
     * @return the reference associated with this Destination
     */    
    public EndpointReferenceType getAddress() {
        return reference;
    }

    /**
     * Retreive a back-channel Conduit, which must be policy-compatible
     * with the current Message and associated Destination. For example
     * compatible Quality of Protection must be asserted on the back-channel.
     * This would generally only be an issue if the back-channel is decoupled.
     * 
     * @param inMessage the current inbound message (null to indicate a 
     * disassociated back-channel)
     * @param partialResponse in the decoupled case, this is expected to be the
     * outbound Message to be sent over the in-built back-channel. 
     * @param address the backchannel address (null to indicate anonymous)
     * @return a suitable Conduit
     */
    public Conduit getBackChannel(Message inMessage,
                                  Message partialResponse,
                                  EndpointReferenceType address)
        throws IOException {
        Conduit backChannel = null;
        Exchange ex = inMessage.getExchange();
        EndpointReferenceType target = address != null
                                       ? address
                                       : ex.get(EndpointReferenceType.class);
        if (target == null) {
            backChannel = getInbuiltBackChannel(inMessage);
        } else {
            if (partialResponse != null) {
                if (markPartialResponse(partialResponse, target)) {
                    backChannel = getInbuiltBackChannel(inMessage);
                }
            } else {
                ConduitInitiator conduitInitiator = getConduitInitiator();
                if (conduitInitiator != null) {
                    backChannel = conduitInitiator.getConduit(endpointInfo, target);
                    // ensure decoupled back channel input stream is closed
                    backChannel.setMessageObserver(new MessageObserver() {
                        public void onMessage(Message m) {
                            if (m.getContentFormats().contains(InputStream.class)) {
                                InputStream is = m.getContent(InputStream.class);
                                try {
                                    is.close();
                                } catch (Exception e) {
                                    // ignore
                                }
                            }
                        }
                    });
                }
            }
        }
        return backChannel;
    }

    /**
     * Register a message observer for incoming messages.
     * 
     * @param observer the observer to notify on receipt of incoming
     */
    public synchronized void setMessageObserver(MessageObserver observer) {
        if (observer != incomingObserver) {
            MessageObserver old = incomingObserver;
            incomingObserver = observer;
            if (observer != null) {
                getLogger().fine("registering incoming observer: " + observer);
                if (old == null) {
                    activate();
                }
            } else {
                getLogger().fine("unregistering incoming observer: " + incomingObserver);
                if (old != null) {
                    deactivate();
                }
            }
        }
    }
    
    /**
     * @return the observer to notify on receipt of incoming message
     */
    public MessageObserver getMessageObserver() {
        return incomingObserver;
    }
    
    /**
     * Shutdown the Destination, i.e. stop accepting incoming messages.
     */
    public void shutdown() {
        // nothing to do by default
    }

    /**
     * Mark message as a partial message. Only required if decoupled
     * mode is supported.
     * 
     * @param partialResponse the partial response message
     * @param the decoupled target
     * @return true iff partial responses are supported
     */
    protected boolean markPartialResponse(Message partialResponse,
                                          EndpointReferenceType decoupledTarget) {
        return false;
    }
    
    /**
     * @return the associated conduit initiator, or null if decoupled mode
     * not supported.
     */
    protected ConduitInitiator getConduitInitiator() {
        return null;
    }

    /**
     * Activate receipt of incoming messages.
     */
    protected void activate() {
        // nothing to do by default
    }

    /**
     * Deactivate receipt of incoming messages.
     */
    protected void deactivate() {
        // nothing to do by default        
    }
    
    /**
     * Get the exposed reference.
     * 
     * @param address the corresponding EndpointInfo
     * @return the actual reference
     */
    protected static EndpointReferenceType getTargetReference(String addr) {
        EndpointReferenceType ref = new EndpointReferenceType();
        AttributedURIType address = new AttributedURIType();
        address.setValue(addr);
        ref.setAddress(address);        
        return ref;
    }
    
    /**
     * @return the logger to use
     */
    protected abstract Logger getLogger();

    /**
     * @param inMessage the incoming message
     * @return the inbuilt backchannel
     */
    protected abstract Conduit getInbuiltBackChannel(Message inMessage);
    
    /**
     * Backchannel conduit.
     */
    protected abstract class AbstractBackChannelConduit extends AbstractConduit {

        public AbstractBackChannelConduit() {
            super(EndpointReferenceUtils.getAnonymousEndpointReference());
        }

        /**
         * Register a message observer for incoming messages.
         * 
         * @param observer the observer to notify on receipt of incoming
         */
        public void setMessageObserver(MessageObserver observer) {
            // shouldn't be called for a back channel conduit
        }
        
        protected Logger getLogger() {
            return AbstractDestination.this.getLogger();
        }
    }
}
