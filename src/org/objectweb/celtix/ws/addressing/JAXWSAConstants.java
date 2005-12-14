package org.objectweb.celtix.ws.addressing;


/**
 * A container for WS-Addressing constants.
 */
public final class JAXWSAConstants {

    /**
     * Well-known Property names for AddressingProperties in BindingProvider
     * Context.
     */
    public static final String CLIENT_ADDRESSING_PROPERTIES = 
        "javax.xml.ws.addressing.context";
    
    /**
     * Well-known Property names for AddressingProperties in Handler
     * Context.
     */
    public static final String CLIENT_ADDRESSING_PROPERTIES_INBOUND = 
        "javax.xml.ws.addressing.context.inbound";
    public static final String CLIENT_ADDRESSING_PROPERTIES_OUTBOUND = 
        "javax.xml.ws.addressing.context.outbound";
    public static final String SERVER_ADDRESSING_PROPERTIES_INBOUND = 
        "javax.xml.ws.addressing.context.inbound";
    public static final String SERVER_ADDRESSING_PROPERTIES_OUTBOUND = 
        "javax.xml.ws.addressing.context.outbound";

    /**
     * Prevents instantiation. 
     */
    private JAXWSAConstants() {
    }
}
