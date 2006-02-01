package org.objectweb.celtix.bus.ws.rm;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.ws.rm.RMProperties;

import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.RM_PROPERTIES;

/**
 * Holder for utility methods relating to contexts.
 */

public final class RMContextUtils {

    /**
     * Prevents instantiation.
     */
    private RMContextUtils() {
    }

    /**
     * Store RM Properties in the context.
     * 
     * @param context the message context
     */
    public static void storeRMPs(RMProperties rmps, MessageContext context) {
        context.put(RM_PROPERTIES, rmps);
        context.setScope(RM_PROPERTIES, MessageContext.Scope.HANDLER);
    }

    /**
     * Store RM Properties in the context.
     * 
     * @param context the message context
     */
    public static RMProperties retrieveRMPs(MessageContext context) {
        return (RMProperties)context.get(RM_PROPERTIES);
    }
}
