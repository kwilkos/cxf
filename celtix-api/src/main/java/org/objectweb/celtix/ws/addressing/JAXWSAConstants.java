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
     * Used by binding to cache itseld in the context
     */
    public static final String BINDING_PROPERTY =
        "org.objectweb.celtix.ws.addressing.binding";
    
    /**
     * Used by binding to cache Transport in the context
     */
    public static final String TRANSPORT_PROPERTY =
        "org.objectweb.celtix.ws.addressing.transport";
    
    /**
     * Used by AddressingBuilder factory method.
     */
    public static final String DEFAULT_ADDRESSING_BUILDER =
        "org.objectweb.celtix.bus.ws.addressing.AddressingBuilderImpl";

    /**
     * Prevents instantiation. 
     */
    private JAXWSAConstants() {
    }
}
