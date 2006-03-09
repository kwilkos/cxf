package org.objectweb.celtix.bus.ws.addressing;


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;


/**
 * Logical Handler responsible for aggregating the Message Addressing 
 * Properties for outgoing messages.
 */
public class MAPAggregator implements LogicalHandler<LogicalMessageContext> {

    private static final Logger LOG = 
        LogUtils.getL7dLogger(MAPAggregator.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    /**
     * Used to fabricate a Uniform Resource Name from a UUID string
     */
    private static final String URN_UUID = "urn:uuid:";

    protected final Map<String, String> messageIDs = 
        new HashMap<String, String>();

    /**
     * Whether the endpoint supports WS-Addressing.
     */
    private final AtomicBoolean usingAddressingDetermined = new AtomicBoolean(false);
    private final AtomicBoolean usingAddressing = new AtomicBoolean(false);

    /**
     * Constructor.
     */
    public MAPAggregator() {
    } 

    /**
     * Initialize the handler.
     */
    public void init(Map<String, Object> map) {
    }
    
    /**
     * Invoked for normal processing of inbound and outbound messages.
     *
     * @param context the messsage context
     */
    public boolean handleMessage(LogicalMessageContext context) {
        return mediate(context);
    }

    /**
     * Invoked for fault processing.
     *
     * @param context the messsage context
     */
    public boolean handleFault(LogicalMessageContext context) {
        return mediate(context);
    }

    /**
     * Called at the conclusion of a message exchange pattern just prior to
     * the JAX-WS runtime dispatching a message, fault or exception.
     *
     * @param context the message context
     */
    public void close(MessageContext context) {
    }

    /**
     * Release handler resources.
     */
    public void destroy() {
    }

    /**
     * Determine if addressing is being used
     *
     * @param context the messsage context
     * @pre message is outbound
     */
    private boolean usingAddressing(LogicalMessageContext context) {
        boolean ret = false;
        if (ContextUtils.isRequestor(context)) {
            if (!usingAddressingDetermined.get()) {
                Port port = ContextUtils.retrievePort(context);
                if (port != null) {
                    Iterator<?> portExts =
                        port.getExtensibilityElements().iterator();
                    Iterator<?> bindingExts = 
                        port.getBinding().getExtensibilityElements().iterator();
                    ret = hasUsingAddressing(portExts)
                        || hasUsingAddressing(bindingExts);
                }
                setUsingAddressing(ret);
            } else {
                ret = usingAddressing.get();
            }
        } else {
            ret = getMAPs(context, false, false) != null;
        }
        return ret;
    }

    /**
     * @param extensionElements iterator over extension elements
     * @return true iff the UsingAddressing element is found
     */
    private boolean hasUsingAddressing(Iterator<?> extensionElements) {
        boolean found = false;
        while (extensionElements.hasNext() && !found) {
            ExtensibilityElement ext = 
                (ExtensibilityElement)extensionElements.next();
            found = Names.WSAW_USING_ADDRESSING_QNAME.equals(ext.getElementType());

        } 
        return found;
    }

    /**
     * Mediate message flow.
     *
     * @param context the messsage context
     * @return true if processing should continue on dispatch path 
     */
    private boolean mediate(LogicalMessageContext context) {    
        boolean continueProcessing = true;
        if (ContextUtils.isOutbound(context)) {
            if (usingAddressing(context)) {
                // request/response MAPs must be aggregated
                aggregate(context);
            }
        } else if (!ContextUtils.isRequestor(context)) {
            // responder validates incoming MAPs
            AddressingPropertiesImpl maps = getMAPs(context, false, false);
            setUsingAddressing(true);
            continueProcessing = validateIncomingMAPs(maps, context); 
            if (continueProcessing) {
                if (ContextUtils.isOneway(context)
                    || !ContextUtils.isGenericAddress(maps.getReplyTo())) {
                    ContextUtils.rebaseTransport(maps, context);
                }            
            } else {
                // validation failure => dispatch is aborted, response MAPs 
                // must be aggregated
                aggregate(context);
            }
        }
        return continueProcessing;
    }

    /**
     * Perform MAP aggregation.
     *
     * @param context the messsage context
     */
    private void aggregate(LogicalMessageContext context) {
        AddressingPropertiesImpl maps = assembleGeneric(context);
        boolean isRequestor = ContextUtils.isRequestor(context);
        addRoleSpecific(maps, isRequestor, context);
        // outbound property always used to store MAPs, as this handler 
        // aggregates only when either:
        // a) message really is outbound
        // b) message is currently inbound, but we are about to abort dispatch
        //    due to an incoming MAPs validation failure, so the dispatch
        //    will shortly traverse the outbound path
        ContextUtils.storeMAPs(maps, context, true, isRequestor, true);
    }

    /**
     * Assemble the generic MAPs (for both requests and responses).
     *
     * @param context the messsage context
     * @return AddressingProperties containing the generic MAPs
     */
    private AddressingPropertiesImpl assembleGeneric(MessageContext context) {
        AddressingPropertiesImpl maps = getMAPs(context, true, true);
        // MessageID
        if (maps.getMessageID() == null) {
            String messageID = URN_UUID + UUID.randomUUID();
            maps.setMessageID(ContextUtils.getAttributedURI(messageID));
        }
        // To
        if (maps.getTo() == null) {
            // To cached in context by transport
            EndpointReferenceType reference = ContextUtils.retrieveTo(context);
            maps.setTo(reference != null 
                       ? reference.getAddress()
                       : ContextUtils.getAttributedURI(Names.WSA_NONE_ADDRESS));
        }
        // Action
        if (ContextUtils.hasEmptyAction(maps)) {
            maps.setAction(ContextUtils.getAction(context));
        }
        return maps;
    }

    /**
     * Add MAPs which are specific to the requestor or responder role.
     *
     * @param maps the MAPs being assembled
     * @param isRequestor true iff the current messaging role is that of 
     * requestor 
     * @param context the messsage context
     */
    private void addRoleSpecific(AddressingPropertiesImpl maps, 
                                 boolean isRequestor,
                                 MessageContext context) {
        if (isRequestor) {
            // add request-specific MAPs
            boolean isOneway = ContextUtils.isOneway(context);
            // ReplyTo, set if null in MAPs or if set to a generic address
            // (anonymous or none) that may not be appropriate for the
            // current invocation
            EndpointReferenceType replyTo = maps.getReplyTo();
            if (ContextUtils.isGenericAddress(replyTo)) {
                // use ReplyTo provided by transport
                replyTo = ContextUtils.retrieveReplyTo(context);
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
                // REVISIT FaultTo if cached by transport in context
            }
            // cache correlation ID
            if (ContextUtils.isOutbound(context)) {
                ContextUtils.storeCorrelationID(maps.getMessageID(), true, context);
            }
        } else {
            // add response-specific MAPs
            AddressingPropertiesImpl inMAPs = getMAPs(context, false, false);
            maps.exposeAs(inMAPs.getNamespaceURI());
            // To taken from ReplyTo in incoming MAPs
            if (inMAPs.getReplyTo() != null) {
                maps.setTo(inMAPs.getReplyTo().getAddress());
            }
            // RelatesTo taken from MessageID in incoming MAPs
            if (inMAPs.getMessageID() != null) {
                String inMessageID = inMAPs.getMessageID().getValue();
                maps.setRelatesTo(ContextUtils.getRelatesTo(inMessageID));
            }
        }
    }

    /**
     * Get the starting point MAPs (either empty or those set explicitly
     * by the application on the binding provider request context).
     *
     * @param context the messsage context
     * @param isProviderContext true if the binding provider request context
     * available to the client application as opposed to the message context
     * visible to handlers
     * @param isOutbound true iff the message is outbound
     * @return AddressingProperties retrieved MAPs
     */
    private AddressingPropertiesImpl getMAPs(MessageContext context,
                                             boolean isProviderContext,
                                             boolean isOutbound) {

        AddressingPropertiesImpl maps = null;
        maps = ContextUtils.retrieveMAPs(context, 
                                         isProviderContext,
                                         isOutbound);
        LOG.log(Level.INFO, "MAPs retrieved from context {0}", maps);

        if (maps == null && isProviderContext) {
            maps = new AddressingPropertiesImpl();
        }
        return maps;
    }

    /**
     * Validate incoming MAPs
     * @param maps the incoming MAPs
     * @param context the messsage context
     * @return true if incoming MAPs are valid
     * @pre inbound message, not requestor
     */
    private boolean validateIncomingMAPs(AddressingProperties maps, MessageContext context) {
        boolean valid = true;
        if (maps != null) {
            AttributedURIType messageID = maps.getMessageID();
            if (messageID != null
                && messageIDs.put(messageID.getValue(), 
                                  messageID.getValue()) != null) {
                String reason =
                    BUNDLE.getString("DUPLICATE_MESSAGE_ID_MSG");
                String l7dReason = 
                    MessageFormat.format(reason, messageID.getValue());
                ContextUtils.storeMAPFaultName(Names.DUPLICATE_MESSAGE_ID_NAME,
                                               context);
                ContextUtils.storeMAPFaultReason(l7dReason, context);
                valid = false;
            }
        }
        return valid;
    }
    
    /**
     * Set using addressing flag.
     * 
     * @param using true if addressing in use.
     */
    private void setUsingAddressing(boolean using) {
        usingAddressing.set(using);
        usingAddressingDetermined.set(true);
    }
}

