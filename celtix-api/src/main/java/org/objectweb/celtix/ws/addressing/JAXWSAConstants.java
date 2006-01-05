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
     * Used by client transport to cache To address in the context
     */
    public static final String CLIENT_TO_ADDRESS_PROPERTY =
        "org.objectweb.celtix.ws.addressing.client.to";

    /**
     * Used by client transport to cache WSDL Port in the context
     */
    public static final String CLIENT_WSDL_PORT_PROPERTY =
        "org.objectweb.celtix.ws.addressing.client.port";

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
