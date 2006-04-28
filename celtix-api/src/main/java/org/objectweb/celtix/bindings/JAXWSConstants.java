package org.objectweb.celtix.bindings;

/**
 * A container for JAXWS constants.
 */
public final class JAXWSConstants {
    
    /**
     * Used to cache data binding callback in context.
     */
    public static final String DATABINDING_CALLBACK_PROPERTY = 
        "org.objectweb.celtix.bindings.databinding.callback";
    
    /**
     * Used to cache server binding endpoint callback in context.
     */
    public static final String SERVER_BINDING_ENDPOINT_CALLBACK_PROPERTY = 
        "org.objectweb.celtix.bindings.server.endpoint.callback";
    
    /**
     * Used to cache endpoint in context.
     */
    public static final String ENDPOINT_PROPERTY  = 
        "org.objectweb.celtix.bindings.endpoint";
    
    /**
     * Used to vcache bus in context.
     */
    public static final String BUS_PROPERTY  = 
        "org.objectweb.celtix.bindings.bus";
    
    /**
     * Used to cache dispatch flag in context.
     */
    public static final String DISPATCH_PROPERTY  = 
        "org.objectweb.celtix.bindings.dispatch";
    

    /**
     * Used by binding to cache itself in the context
     */
    public static final String BINDING_PROPERTY =
        "org.objectweb.celtix.bindings.binding";
    
    /**
     * Used by binding to cache itself in the context
     */
    public static final String CLIENT_BINDING_PROPERTY =
        "org.objectweb.celtix.bindings.client.binding";
    
    /**
     * Used by binding to cache itself in the context
     */
    public static final String SERVER_BINDING_PROPERTY =
        "org.objectweb.celtix.bindings.server.binding";
    
    /**
     * Used by binding to cache Transport in the context
     */
    public static final String TRANSPORT_PROPERTY =
        "org.objectweb.celtix.bindings.transport";

    /**
     * Used by binding to cache Client Transport in the context
     */
    public static final String CLIENT_TRANSPORT_PROPERTY =
        "org.objectweb.celtix.bindings.client.transport";
    
    /**
     * Used by binding to cache Server Transport in the context
     */
    public static final String SERVER_TRANSPORT_PROPERTY =
        "org.objectweb.celtix.bindings.server.transport";
    
    /**
     * Prevents instantiation. 
     */
    private JAXWSConstants() {
    }
}
