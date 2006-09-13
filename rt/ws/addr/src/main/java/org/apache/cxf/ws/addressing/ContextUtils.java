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


import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;

import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;

import static org.apache.cxf.message.Message.CORRELATION_IN;
import static org.apache.cxf.message.Message.CORRELATION_OUT;
import static org.apache.cxf.message.Message.ONEWAY_MESSAGE;
import static org.apache.cxf.message.Message.REQUESTOR_ROLE;

import static org.apache.cxf.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;


/**
 * Holder for utility methods relating to contexts.
 */
public final class ContextUtils {

    public static final ObjectFactory WSA_OBJECT_FACTORY = new ObjectFactory();

    private static final String WS_ADDRESSING_PACKAGE = 
        PackageUtils.getPackageName(EndpointReferenceType.class);
    private static final Logger LOG = LogUtils.getL7dLogger(ContextUtils.class);
    
    
    /**
     * Used to fabricate a Uniform Resource Name from a UUID string
     */
    private static final String URN_UUID = "urn:uuid:";
    
    private static JAXBContext jaxbContext;
     
    /**
     * Used by MAPAggregator to cache bad MAP fault name
     */
    private static final String MAP_FAULT_NAME_PROPERTY = 
        "org.apache.cxf.ws.addressing.map.fault.name";

    /**
     * Used by MAPAggregator to cache bad MAP fault reason
     */
    private static final String MAP_FAULT_REASON_PROPERTY = 
        "org.apache.cxf.ws.addressing.map.fault.reason";
    
    /**
     * Indicates a partial response has already been sent
     */
    private static final String PARTIAL_REPONSE_SENT_PROPERTY =
        "org.apache.cxf.ws.addressing.partial.response.sent";
 
   /**
    * Prevents instantiation.
    */
    private ContextUtils() {
    }

   /**
    * Determine if message is outbound.
    *
    * @param message the current Message
    * @return true iff the message direction is outbound
    */
    public static boolean isOutbound(Message message) {
        Boolean outbound = (Boolean)message.get(MESSAGE_OUTBOUND_PROPERTY);
        return outbound != null && outbound.booleanValue();
    }

   /**
    * Determine if current messaging role is that of requestor.
    *
    * @param message the current Message
    * @return true iff the current messaging role is that of requestor
    */
    public static boolean isRequestor(Message message) {
        Boolean requestor = (Boolean)message.get(REQUESTOR_ROLE);
        return requestor != null && requestor.booleanValue();
    }

    /**
     * Determine if current invocation is oneway.
     *
     * @param message the current Message
     * @return true iff the current invocation is oneway
     */
    public static boolean isOneway(Message message) {
        Boolean oneway = (Boolean)message.get(ONEWAY_MESSAGE);
        return oneway != null && oneway.booleanValue();
    }

    /**
     * Get appropriate property name for message addressing properties.
     *
     * @param isProviderContext true if the binding provider request context 
     * available to the client application as opposed to the message context 
     * visible to handlers
     * @param isRequestor true iff the current messaging role is that of
     * requestor
     * @param isOutbound true iff the message is outbound
     * @return the property name to use when caching the MAPs in the context
     */
    public static String getMAPProperty(boolean isRequestor, 
                                        boolean isProviderContext,
                                        boolean isOutbound) {
        return isRequestor
                ? isProviderContext
                 ? CLIENT_ADDRESSING_PROPERTIES
                 : isOutbound
                   ? CLIENT_ADDRESSING_PROPERTIES_OUTBOUND
                   : CLIENT_ADDRESSING_PROPERTIES_INBOUND
               : isOutbound
                 ? SERVER_ADDRESSING_PROPERTIES_OUTBOUND
                 : SERVER_ADDRESSING_PROPERTIES_INBOUND;
    }
    
    /**
     * Get appropriate context property name for correlation ID.
     *
     * @param isOutbound true iff the message is outbound
     * @return the property name to use when caching the 
     * correlation ID in the context
     */
    public static String getCorrelationIDProperty(boolean isOutbound) {
        return isOutbound ? CORRELATION_OUT : CORRELATION_IN;
    }


    /**
     * Store MAPs in the message.
     *
     * @param message the current message
     * @param isOutbound true iff the message is outbound
     */
    public static void storeMAPs(AddressingProperties maps,
                                 Message message,
                                 boolean isOutbound) {
        storeMAPs(maps, message, isOutbound, isRequestor(message), true, false);
    }

    /**
     * Store MAPs in the message.
     *
     * @param maps the MAPs to store
     * @param message the current message
     * @param isOutbound true iff the message is outbound
     * @param isRequestor true iff the current messaging role is that of
     * requestor
     * @param handler true if HANDLER scope, APPLICATION scope otherwise
     */
    public static void storeMAPs(AddressingProperties maps,
                                 Message message,
                                 boolean isOutbound, 
                                 boolean isRequestor,
                                 boolean handler) {
        storeMAPs(maps, message, isOutbound, isRequestor, handler, false);
    }
    
    /**
     * Store MAPs in the message.
     *
     * @param maps the MAPs to store
     * @param message the current message
     * @param isOutbound true iff the message is outbound
     * @param isRequestor true iff the current messaging role is that of
     * requestor
     * @param handler true if HANDLER scope, APPLICATION scope otherwise
     * @param isProviderContext true if the binding provider request context 
     */
    public static void storeMAPs(AddressingProperties maps,
                                 Message message,
                                 boolean isOutbound, 
                                 boolean isRequestor,
                                 boolean handler,
                                 boolean isProviderContext) {
        if (maps != null) {
            String mapProperty = getMAPProperty(isRequestor, isProviderContext, isOutbound);
            LOG.log(Level.INFO,
                    "associating MAPs with context property {0}",
                    mapProperty);
            message.put(mapProperty, maps);
        }
    }


    /**
     * @param message the current message
     * @param isProviderContext true if the binding provider request context
     * available to the client application as opposed to the message context
     * visible to handlers
     * @param isOutbound true iff the message is outbound
     * @return the current addressing properties
     */
    public static AddressingPropertiesImpl retrieveMAPs(
                                                   Message message, 
                                                   boolean isProviderContext,
                                                   boolean isOutbound) {
        boolean isRequestor = ContextUtils.isRequestor(message);
        String mapProperty =
            ContextUtils.getMAPProperty(isProviderContext, 
                                        isRequestor,
                                        isOutbound);
        LOG.log(Level.INFO,
                "retrieving MAPs from context property {0}",
                mapProperty);
        AddressingPropertiesImpl maps =
            (AddressingPropertiesImpl)message.get(mapProperty);
        if (maps != null) {
            LOG.log(Level.INFO, "current MAPs {0}", maps);
        } else if (!isProviderContext) {
            LOG.warning("MAPS_RETRIEVAL_FAILURE_MSG");
        }
        return maps;
    }

    /**
     * Helper method to get an attributed URI.
     *
     * @param uri the URI
     * @return an AttributedURIType encapsulating the URI
     */
    public static AttributedURIType getAttributedURI(String uri) {
        AttributedURIType attributedURI = 
            WSA_OBJECT_FACTORY.createAttributedURIType();
        attributedURI.setValue(uri);
        return attributedURI;
    }

    /**
     * Helper method to get a RealtesTo instance.
     *
     * @param uri the related URI
     * @return a RelatesToType encapsulating the URI
     */
    public static RelatesToType getRelatesTo(String uri) {
        RelatesToType relatesTo =
            WSA_OBJECT_FACTORY.createRelatesToType();
        relatesTo.setValue(uri);
        return relatesTo;
    }

    /**
     * Helper method to determine if an EPR address is generic (either null,
     * none or anonymous).
     *
     * @param ref the EPR under test
     * @return true iff the address is generic
     */
    public static boolean isGenericAddress(EndpointReferenceType ref) {
        return ref == null 
               || ref.getAddress() == null
               || Names.WSA_ANONYMOUS_ADDRESS.equals(ref.getAddress().getValue())
               || Names.WSA_NONE_ADDRESS.equals(ref.getAddress().getValue());
    }

    /**
     * Helper method to determine if an MAPs Action is empty (a null action
     * is considered empty, whereas a zero length action suppresses
     * the propogation of the Action property).
     *
     * @param ref the MAPs Action under test
     * @return true iff the Action is empty
     */
    public static boolean hasEmptyAction(AddressingProperties maps) {
        boolean empty = maps.getAction() == null;
        if (maps.getAction() != null 
            && maps.getAction().getValue().length() == 0) {
            maps.setAction(null);
            empty = false;
        } 
        return empty;
    }
    
    /**
     * Rebase server transport on replyTo
     * 
     * @param reference the replyTo reference
     * @param namespaceURI determines the WS-A version
     * @param inMessage the current message
     */
    public static void rebaseTransport(EndpointReferenceType reference,
                                       String namespaceURI,
                                       Message inMessage) {
        if (!retrievePartialResponseSent(inMessage)) {
            // ensure there is a MAPs instance available for the outbound
            // partial response that contains appropriate To and ReplyTo
            // properties (i.e. anonymous & none respectively)
            AddressingPropertiesImpl maps = new AddressingPropertiesImpl();
            maps.setTo(ContextUtils.getAttributedURI(Names.WSA_ANONYMOUS_ADDRESS));
            maps.setReplyTo(WSA_OBJECT_FACTORY.createEndpointReferenceType());
            maps.getReplyTo().setAddress(getAttributedURI(Names.WSA_NONE_ADDRESS));
            maps.setAction(getAttributedURI(""));
            maps.exposeAs(namespaceURI);
            Message partialResponse = new MessageImpl();
            storeMAPs(maps, partialResponse, true, true, true, true);

            try {
                Destination target = inMessage.getDestination();
                Conduit backChannel = target.getBackChannel(inMessage,
                                                            partialResponse,
                                                            reference);
                if (backChannel != null) {
                    // REVISIT set up interceptor chains and send message
                    
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "SERVER_TRANSPORT_REBASE_FAILURE_MSG", e);
            }
        } 
    }

    /**
     * Store bad MAP fault name in the message.
     *
     * @param faultName the fault name to store
     * @param message the current message
     */
    public static void storeMAPFaultName(String faultName, 
                                         Message message) {
        message.put(MAP_FAULT_NAME_PROPERTY, faultName);
    }

    /**
     * Retrieve MAP fault name from the message.
     *
     * @param message the current message
     * @returned the retrieved fault name
     */
    public static String retrieveMAPFaultName(Message message) {
        return (String)message.get(MAP_FAULT_NAME_PROPERTY);
    }

    /**
     * Store MAP fault reason in the message.
     *
     * @param reason the fault reason to store
     * @param message the current message
     */
    public static void storeMAPFaultReason(String reason, 
                                           Message message) {
        message.put(MAP_FAULT_REASON_PROPERTY, reason);
    }

    /**
     * Retrieve MAP fault reason from the message.
     *
     * @param message the current message
     * @returned the retrieved fault reason
     */
    public static String retrieveMAPFaultReason(Message message) {
        return (String)message.get(MAP_FAULT_REASON_PROPERTY);
    }

    /**
     * Store correlation ID in the context
     *
     * @param id the correlation ID
     * @param isOutbound true if message is outbound
     * @param message the current message
     */   
    public static void storeCorrelationID(RelatesToType id, 
                                          boolean isOutbound,
                                          Message message) {
        storeCorrelationID(id.getValue(), isOutbound, message);
    }
    
    /**
     * Store correlation ID in the context
     *
     * @param id the correlation ID
     * @param isOutbound true if message is outbound
     * @param message the current message
     */   
    public static void storeCorrelationID(AttributedURIType id, 
                                          boolean isOutbound,
                                          Message message) {
        storeCorrelationID(id.getValue(), isOutbound, message);
    }
    
    /**
     * Store correlation ID in the context
     *
     * @param id the correlation ID
     * @param isOutbound true if message is outbound
     * @param message the current message
     */   
    protected static void storeCorrelationID(String id, 
                                           boolean isOutbound,
                                           Message message) {
        message.put(getCorrelationIDProperty(isOutbound), id);
    }
    
    /**
     * Retrieve correlation ID from the message.
     *
     * @param message the current message
     * @param isOutbound true if message is outbound
     * @returned the retrieved correlation ID
     */
    public static String retrieveCorrelationID(Message message, 
                                               boolean isOutbound) {
        return (String)message.get(getCorrelationIDProperty(isOutbound));
    }
    
    /**
     * Store an indication that a partial response has been sent.
     * Relavant if *both* the replyTo & faultTo are decoupled,
     * and a fault occurs, then we would already have sent the
     * partial response (pre-dispatch) for the replyTo, so
     * no need to send again.
     *
     * @param message the current message
     */
    public static void storePartialResponseSent(Message message) {
        message.put(PARTIAL_REPONSE_SENT_PROPERTY, Boolean.TRUE);
    }

    /**
     * Retrieve indication that a partial response has been sent.
     *
     * @param message the current message
     * @returned the retrieved indication that a partial response
     * has been sent
     */
    public static boolean retrievePartialResponseSent(Message message) {
        Boolean ret = (Boolean)message.get(PARTIAL_REPONSE_SENT_PROPERTY);
        return ret != null && ret.booleanValue();
    }

    
    /**
     * Retrieve a JAXBContext for marshalling and unmarshalling JAXB generated
     * types.
     *
     * @return a JAXBContext 
     */
    public static JAXBContext getJAXBContext() throws JAXBException {
        synchronized (ContextUtils.class) {
            if (jaxbContext == null) {
                jaxbContext = JAXBContext.newInstance(WS_ADDRESSING_PACKAGE);
            }
        }
        return jaxbContext;
    }

    /**
     * Set the encapsulated JAXBContext (used by unit tests).
     * 
     * @param ctx JAXBContext 
     */
    public static void setJAXBContext(JAXBContext ctx) throws JAXBException {
        synchronized (ContextUtils.class) {
            jaxbContext = ctx;
        }
    }
    
    
    /**
     * @return a generated UUID
     */
    public static String generateUUID() {
        return URN_UUID + UUID.randomUUID();
    }
    
    /**
     * Construct the Action URI.
     * 
     * @param message the current message
     * @return the Action URI
     */
    public static AttributedURIType getAction(Message message) {
        String action = null;
        // REVISIT: add support for @{Fault}Action annotation (generated
        // from the wsaw:Action WSDL element)
        LOG.fine("Determining action");
        Exception fault = message.getContent(Exception.class);
        Method method = getMethod(message);
        LOG.fine("method: " + method + ", fault: " + fault);
        if (method != null) {
            if (fault != null) {
                WebFault webFault = fault.getClass().getAnnotation(WebFault.class);
                action = getAction(webFault.targetNamespace(),
                                   method, 
                                   webFault.name(),
                                   true);
            } else {
                if (ContextUtils.isRequestor(message)) {
                    RequestWrapper requestWrapper =
                        method.getAnnotation(RequestWrapper.class);
                    if (requestWrapper != null) {
                        action = getAction(requestWrapper.targetNamespace(),
                                           method,
                                           requestWrapper.localName(),
                                           false);
                    } else {
                        WebService wsAnnotation = method.getDeclaringClass().getAnnotation(WebService.class);
                        WebMethod wmAnnotation = method.getAnnotation(WebMethod.class);
                        
                        action = getAction(wsAnnotation.targetNamespace(),
                                           method,
                                           wmAnnotation.operationName(),
                                           false);
                    }
                        
                } else {
                    ResponseWrapper responseWrapper =
                        method.getAnnotation(ResponseWrapper.class);
                    if (responseWrapper != null) {
                        action = getAction(responseWrapper.targetNamespace(),
                                           method,
                                           responseWrapper.localName(),
                                          false);
                    } else {
                       //RPC-Literal case.
                        WebService wsAnnotation = method.getDeclaringClass().getAnnotation(WebService.class);
                        WebMethod wmAnnotation = method.getAnnotation(WebMethod.class);
                        
                        action = getAction(wsAnnotation.targetNamespace(),
                                           method,
                                           wmAnnotation.operationName(),
                                           false);
                    }
                }
            }
        }
        return action != null ? getAttributedURI(action) : null;
    }
        

    /**
     * Construct the Action string.
     *
     * @param targetNamespace the target namespace
     * @param method the method
     * @param localName the local name
     * @param isFault true if a fault
     * @return action string
     */
    private static String getAction(String targetNamespace, 
                                    Method method, 
                                    String localName,
                                    boolean isFault) {
        String action = null;
        action = targetNamespace;
        action += Names.WSA_ACTION_DELIMITER;
        action += method.getDeclaringClass().getSimpleName();
        if (isFault) {
            action += method.getName();
            action += Names.WSA_FAULT_DELIMITER;
        }
        action += Names.WSA_ACTION_DELIMITER;
        action += localName;
        return action;
    }
    
    /**
     * Get the current Method.
     * 
     * @param message the current message
     * @return the Method from the BindingOperationInfo
     */
    private static Method getMethod(Message message) {
        Method method = null;
        BindingOperationInfo bindingOpInfo =
            message.getExchange().get(BindingOperationInfo.class);
        if (bindingOpInfo != null) {
            OperationInfo opInfo = bindingOpInfo.getOperationInfo();
            if (opInfo != null) {
                method = (Method)opInfo.getProperty(Method.class.getName());
            }
        }
        return method;
    }
}











