package org.objectweb.celtix.bus.ws.rm;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.ws.rm.JAXWSRMConstants;
import org.objectweb.celtix.ws.rm.RMProperties;

/**
 * Holder for utility methods relating to contexts.
 */

public final class RMContextUtils {

    /**
     * Prevents instantiation.
     */
    private RMContextUtils() {
    }
    
    public static RMProperties retrieveRMProperties(MessageContext context, boolean outbound) {
        return (RMProperties)context.get(getRMPropertiesKey(outbound));
    }
    
    public static void storeRMProperties(MessageContext context, RMProperties rmps, boolean outbound) {
        String key = getRMPropertiesKey(outbound);
        context.put(key, rmps);
        context.setScope(key, MessageContext.Scope.HANDLER);
    }
    
    private static String getRMPropertiesKey(boolean outbound) {
        return outbound ? JAXWSRMConstants.RM_PROPERTIES_OUTBOUND : JAXWSRMConstants.RM_PROPERTIES_INBOUND;
    }
    
}
