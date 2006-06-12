package org.objectweb.celtix.ws.addressing;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.transports.ClientTransport;

/**
 * Holder for utility methods relating to contexts.
 */

public final class WSAContextUtils {

    private static final String TO_PROPERTY =
        "org.objectweb.celtix.ws.addressing.to";
    private static final String REPLYTO_PROPERTY =
        "org.objectweb.celtix.ws.addressing.replyto";
    private static final String USING_PROPERTY =
        "org.objectweb.celtix.ws.addressing.using";    

    /**
     * Prevents instantiation.
     */
    private WSAContextUtils() {
    }

    /**
     * Store UsingAddressing override flag in the context
     *
     * @param override true if UsingAddressing should be overridden
     * @param context the message context
     */   
    public static void storeUsingAddressing(boolean override, MessageContext context) {
        context.put(USING_PROPERTY, Boolean.valueOf(override));
        context.setScope(USING_PROPERTY, MessageContext.Scope.APPLICATION);
    }
    
    /**
     * Retrieve UsingAddressing override flag from the context
     *
     * @param override true if UsingAddressing should be overridden
     * @param context the message context
     */   
    public static boolean retrieveUsingAddressing(MessageContext context) {
        Boolean override = (Boolean)context.get(USING_PROPERTY);
        return override != null && override.booleanValue();
    }

    /**
     * Store To EPR in the context
     *
     * @param to the To EPR
     * @param context the message context
     */   
    public static void storeTo(EndpointReferenceType to,
                               MessageContext context) {
        context.put(TO_PROPERTY, to);
        context.setScope(TO_PROPERTY, MessageContext.Scope.APPLICATION);
    }
    
    /**
     * Retrieve To EPR from the context.
     *
     * @param transport the ClientTransport if available
     * @param context the message context
     * @returned the retrieved EPR
     */
    public static EndpointReferenceType retrieveTo(ClientTransport transport,
                                                   MessageContext context) {
        EndpointReferenceType to = null;
        if (transport != null) {
            to = transport.getTargetEndpoint();
        } else {
            to = (EndpointReferenceType)context.get(TO_PROPERTY);
        }
        return to;
    }
    
    /**
     * Store ReplyTo EPR in the context
     *
     * @param replyTo the ReplyTo EPR
     * @param context the message context
     */   
    public static void storeReplyTo(EndpointReferenceType replyTo,
                                    MessageContext context) {
        context.put(REPLYTO_PROPERTY, replyTo);
        context.setScope(REPLYTO_PROPERTY, MessageContext.Scope.APPLICATION);
    }

    /**
     * Retrieve ReplyTo EPR from the context.
     *
     * @param transport the ClientTransport if available
     * @param context the message context
     * @returned the retrieved EPR
     */
    public static EndpointReferenceType retrieveReplyTo(ClientTransport transport,
                                                        MessageContext context) {
        EndpointReferenceType replyTo = null;
        if (transport != null) {
            try {
                replyTo = transport.getDecoupledEndpoint();
            } catch (IOException ioe) {
                // ignore
            }
        } else {
            replyTo = (EndpointReferenceType)context.get(REPLYTO_PROPERTY);
        }
        return replyTo;
    }
}
