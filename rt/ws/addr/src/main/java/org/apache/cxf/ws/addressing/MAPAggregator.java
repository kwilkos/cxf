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


import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.extensions.ExtensibilityElement;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.FaultMode;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.ws.addressing.policy.MetadataConstants;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;


/**
 * Logical Handler responsible for aggregating the Message Addressing 
 * Properties for outgoing messages.
 */
public class MAPAggregator extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOG = 
        LogUtils.getL7dLogger(MAPAggregator.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();
    

    /**
     * REVISIT: map usage implies that the *same* interceptor instance 
     * is used in all chains.
     */
    protected final Map<String, String> messageIDs = 
        new HashMap<String, String>();
    
    /**
     * Whether the endpoint supports WS-Addressing.
     */

    private Map<Endpoint, Boolean> usingAddressing = new ConcurrentHashMap<Endpoint, Boolean>();
    
    private boolean allowDuplicates = true;
    
    /**
     * Constructor.
     */
    public MAPAggregator() {
        super();
        setPhase(Phase.PRE_LOGICAL);
    }
    
    /**
     * Indicates if duplicate messageIDs are allowed.
     * @return true iff duplicate messageIDs are allowed
     */
    public boolean allowDuplicates() {
        return allowDuplicates;
    }

    /**
     * Allows/disallows duplicate messageIdDs.  
     * @param ad whether duplicate messageIDs are allowed
     */
    public void setAllowDuplicates(boolean ad) {
        allowDuplicates = ad;
    }


    /**
     * Invoked for normal processing of inbound and outbound messages.
     *
     * @param message the current message
     */
    public void handleMessage(Message message) {
        mediate(message, ContextUtils.isFault(message));
    }

    /**
     * Invoked when unwinding normal interceptor chain when a fault occurred.
     *
     * @param message the current message
     */
    public void  handleFault(Message message) {
    }

    /**
     * Determine if addressing is being used
     *
     * @param message the current message
     * @pre message is outbound
     */
    private boolean usingAddressing(Message message) {
        boolean ret = false;
        if (ContextUtils.isRequestor(message)) {
            ret =  WSAContextUtils.retrieveUsingAddressing(message)
                || hasUsingAddressing(message) 
                || hasAddressingAssertion(message);
        } else {
            ret = getMAPs(message, false, false) != null;
        }
        return ret;
    }
      
   /**
    * Determine if the use of addressing is indicated by the presence of a
    * the usingAddressing attribute.
    *
    * @param message the current message
    * @pre message is outbound
    * @pre requestor role
    */
    private boolean hasUsingAddressing(Message message) {
        boolean ret = false;
        Endpoint endpoint = message.getExchange().get(Endpoint.class);
        if (null != endpoint) {
            Boolean b = usingAddressing.get(endpoint);
            if (null == b) {
                EndpointInfo endpointInfo = endpoint.getEndpointInfo();
                List<ExtensibilityElement> endpointExts = endpointInfo != null ? endpointInfo
                    .getExtensors(ExtensibilityElement.class) : null;
                List<ExtensibilityElement> bindingExts = endpointInfo != null
                    && endpointInfo.getBinding() != null ? endpointInfo
                    .getBinding().getExtensors(ExtensibilityElement.class) : null;
                List<ExtensibilityElement> serviceExts = endpointInfo != null
                    && endpointInfo.getService() != null ? endpointInfo
                    .getService().getExtensors(ExtensibilityElement.class) : null;
                ret = hasUsingAddressing(endpointExts) || hasUsingAddressing(bindingExts)
                             || hasUsingAddressing(serviceExts);
                b = ret ? Boolean.TRUE : Boolean.FALSE;
                usingAddressing.put(endpoint, b);
            } else {
                ret = b.booleanValue();
            }
        }    
        return ret;
    }
    
    /**
     * Determine if the use of addressing is indicated by an assertion in the
     * alternative chosen for the current message.
     * 
     * @param message the current message
     * @pre message is outbound
     * @pre requestor role
     */
    private boolean hasAddressingAssertion(Message message) {
        AssertionInfoMap aim = message.get(AssertionInfoMap.class);
        if (null == aim) {
            return false;
            
        }
        Collection<AssertionInfo> ais = aim.get(MetadataConstants.ADDRESSING_ASSERTION_QNAME);
        if (null == ais || ais.size() == 0) {
            return false;
        }
        // no need to analyse the content of the Addressing assertion here
        
        return true;
    }
    
    /**
     * Asserts all Addressing assertions for the current message, regardless their nested 
     * Policies.
     * @param message the current message
     */
    private void assertAddressing(Message message) {
        AssertionInfoMap aim = message.get(AssertionInfoMap.class);
        if (null == aim) {
            return;
            
        }
        Collection<AssertionInfo> ais = aim.get(MetadataConstants.ADDRESSING_ASSERTION_QNAME);
        if (null == ais || ais.size() == 0) {
            return;
        }
        
        for (AssertionInfo ai : ais) {
            ai.setAsserted(true);
        }
    }


    /**
     * @param exts list of extension elements
     * @return true iff the UsingAddressing element is found
     */
    private boolean hasUsingAddressing(List<ExtensibilityElement> exts) {
        boolean found = false;
        if (exts != null) {
            Iterator<ExtensibilityElement> extensionElements = exts.iterator();
            while (extensionElements.hasNext() && !found) {
                ExtensibilityElement ext = 
                    (ExtensibilityElement)extensionElements.next();
                found = Names.WSAW_USING_ADDRESSING_QNAME.equals(ext.getElementType());    
            }
        }
        return found;
    }

    /**
     * Mediate message flow.
     *
     * @param message the current message
     * @param isFault true if a fault is being mediated
     * @return true if processing should continue on dispatch path 
     */
    protected boolean mediate(Message message, boolean isFault) {    
        boolean continueProcessing = true;
        if (ContextUtils.isOutbound(message)) {
            if (usingAddressing(message)) {
                // request/response MAPs must be aggregated
                aggregate(message, isFault);
            }
        } else if (!ContextUtils.isRequestor(message)) {
            // responder validates incoming MAPs
            AddressingPropertiesImpl maps = getMAPs(message, false, false);
            boolean isOneway = message.getExchange().isOneWay();
            continueProcessing = validateIncomingMAPs(maps, message);
            if (continueProcessing) {
                // any faults thrown from here on can be correlated with this message
                message.put(FaultMode.class, FaultMode.LOGICAL_RUNTIME_FAULT);
                if (isOneway
                    || !ContextUtils.isGenericAddress(maps.getReplyTo())) {
                    ContextUtils.rebaseResponse(maps.getReplyTo(),
                                                maps,
                                                message);
                }
                if (!isOneway) {
                    // ensure the inbound MAPs are available in both the full & fault
                    // response messages (used to determine relatesTo etc.)
                    ContextUtils.propogateReceivedMAPs(maps,
                                                       message.getExchange());
                }
            } else {
                // validation failure => dispatch is aborted, response MAPs 
                // must be aggregated
                aggregate(message, isFault);
            }
        }
        if (null != ContextUtils.retrieveMAPs(message, false, ContextUtils.isOutbound(message))) {            
            assertAddressing(message);
        }
        return continueProcessing;
    }

    /**
     * Perform MAP aggregation.
     *
     * @param message the current message
     * @param isFault true if a fault is being mediated
     */
    private void aggregate(Message message, boolean isFault) {
        AddressingPropertiesImpl maps = assembleGeneric(message);
        boolean isRequestor = ContextUtils.isRequestor(message);
        addRoleSpecific(maps, message, isRequestor, isFault);
        // outbound property always used to store MAPs, as this handler 
        // aggregates only when either:
        // a) message really is outbound
        // b) message is currently inbound, but we are about to abort dispatch
        //    due to an incoming MAPs validation failure, so the dispatch
        //    will shortly traverse the outbound path
        ContextUtils.storeMAPs(maps, message, true, isRequestor);
    }

    /**
     * Assemble the generic MAPs (for both requests and responses).
     *
     * @param message the current message
     * @return AddressingProperties containing the generic MAPs
     */
    private AddressingPropertiesImpl assembleGeneric(Message message) {
        AddressingPropertiesImpl maps = getMAPs(message, true, true);
        // MessageID        
        if (maps.getMessageID() == null) {
            String messageID = ContextUtils.generateUUID();
            maps.setMessageID(ContextUtils.getAttributedURI(messageID));
        }
        // Action
        if (ContextUtils.hasEmptyAction(maps)) {
            maps.setAction(ContextUtils.getAction(message));
        }
        return maps;
    }

    /**
     * Add MAPs which are specific to the requestor or responder role.
     *
     * @param maps the MAPs being assembled
     * @param message the current message
     * @param isRequestor true iff the current messaging role is that of 
     * requestor 
     * @param isFault true if a fault is being mediated
     */
    private void addRoleSpecific(AddressingPropertiesImpl maps, 
                                 Message message,
                                 boolean isRequestor,
                                 boolean isFault) {
        if (isRequestor) {
            Exchange exchange = message.getExchange();
            
            // add request-specific MAPs
            boolean isOneway = exchange.isOneWay();
            boolean isOutbound = ContextUtils.isOutbound(message);
            Conduit conduit = null;
            
            // To
            if (maps.getTo() == null) {
                if (isOutbound) {
                    conduit = ContextUtils.getConduit(conduit, message);
                }
                EndpointReferenceType reference = conduit != null
                                                  ? conduit.getTarget()
                                                  : ContextUtils.getNoneEndpointReference();
                maps.setTo(reference);
            }

            // ReplyTo, set if null in MAPs or if set to a generic address
            // (anonymous or none) that may not be appropriate for the
            // current invocation
            EndpointReferenceType replyTo = maps.getReplyTo();
            if (ContextUtils.isGenericAddress(replyTo)) {
                conduit = ContextUtils.getConduit(conduit, message);
                if (conduit != null) {
                    Destination backChannel = conduit.getBackChannel();
                    if (backChannel != null) {
                        replyTo = backChannel.getAddress();
                    }
                }
                if (replyTo == null || isOneway) {
                    AttributedURIType address =
                        ContextUtils.getAttributedURI(isOneway
                                                      ? Names.WSA_NONE_ADDRESS
                                                      : Names.WSA_ANONYMOUS_ADDRESS);
                    replyTo =
                        ContextUtils.WSA_OBJECT_FACTORY.createEndpointReferenceType();
                    replyTo.setAddress(address);
                }
                maps.setReplyTo(replyTo);
            }
            if (!isOneway) {
                // REVISIT FaultTo if cached by transport in message
            }
        } else {
            // add response-specific MAPs
            AddressingPropertiesImpl inMAPs = getMAPs(message, false, false);
            maps.exposeAs(inMAPs.getNamespaceURI());
            // To taken from ReplyTo in incoming MAPs
            if (maps.getTo() == null && inMAPs.getReplyTo() != null) {
                maps.setTo(inMAPs.getReplyTo());
            }
            // RelatesTo taken from MessageID in incoming MAPs
            if (inMAPs.getMessageID() != null
                && !Boolean.TRUE.equals(message.get(Message.PARTIAL_RESPONSE_MESSAGE))) {
                String inMessageID = inMAPs.getMessageID().getValue();
                maps.setRelatesTo(ContextUtils.getRelatesTo(inMessageID));
            }

            if (isFault
                && !ContextUtils.isGenericAddress(inMAPs.getFaultTo())) {
                ContextUtils.rebaseResponse(inMAPs.getFaultTo(),
                                            inMAPs,
                                            message);
            }
        }
    }

    /**
     * Get the starting point MAPs (either empty or those set explicitly
     * by the application on the binding provider request context).
     *
     * @param message the current message
     * @param isProviderContext true if the binding provider request context
     * available to the client application as opposed to the message context
     * visible to handlers
     * @param isOutbound true iff the message is outbound
     * @return AddressingProperties retrieved MAPs
     */
    private AddressingPropertiesImpl getMAPs(Message message,
                                             boolean isProviderContext,
                                             boolean isOutbound) {

        AddressingPropertiesImpl maps = null;
        maps = ContextUtils.retrieveMAPs(message, 
                                         isProviderContext,
                                         isOutbound);
        LOG.log(Level.INFO, "MAPs retrieved from message {0}", maps);

        if (maps == null && isProviderContext) {
            maps = new AddressingPropertiesImpl();
        }
        return maps;
    }

    /**
     * Validate incoming MAPs
     * @param maps the incoming MAPs
     * @param message the current message
     * @return true if incoming MAPs are valid
     * @pre inbound message, not requestor
     */
    private boolean validateIncomingMAPs(AddressingProperties maps,
                                         Message message) {
        boolean valid = true;
        if (!allowDuplicates && maps != null) {
            AttributedURIType messageID = maps.getMessageID();
            if (messageID != null
                && messageIDs.put(messageID.getValue(), 
                                  messageID.getValue()) != null) {
                LOG.log(Level.WARNING,
                        "DUPLICATE_MESSAGE_ID_MSG",
                        messageID.getValue());
                String reason =
                    BUNDLE.getString("DUPLICATE_MESSAGE_ID_MSG");
                String l7dReason = 
                    MessageFormat.format(reason, messageID.getValue());
                ContextUtils.storeMAPFaultName(Names.DUPLICATE_MESSAGE_ID_NAME,
                                               message);
                ContextUtils.storeMAPFaultReason(l7dReason, message);
                valid = false;
            }
        }
        return valid;
    }
}

