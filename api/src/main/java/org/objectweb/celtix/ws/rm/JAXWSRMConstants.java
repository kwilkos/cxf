package org.objectweb.celtix.ws.rm;

/**
 * A container for WS-RM constants.
 */
public final class JAXWSRMConstants {
    
    /**
     * Used to cache outbound RM properties in context.
     */
    public static final String RM_PROPERTIES_OUTBOUND = 
        "org.objectweb.celtix.ws.rm.context.outbound";
    
    /**
     * Used to cache inbound RM properties in context.
     */
    public static final String RM_PROPERTIES_INBOUND = 
        "org.objectweb.celtix.ws.rm.context.inbound";
    
    /**
     * Prevents instantiation. 
     */
    private JAXWSRMConstants() {
    }
}
