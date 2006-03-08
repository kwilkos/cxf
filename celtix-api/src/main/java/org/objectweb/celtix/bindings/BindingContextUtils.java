package org.objectweb.celtix.bindings;


import java.lang.reflect.Method;

import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.Transport;

import static org.objectweb.celtix.context.ObjectMessageContext.CORRELATION_IN;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.TRANSPORT_PROPERTY;

/**
 * Holder for utility methods relating to contexts.
 */

public final class BindingContextUtils {
    
    public static final String DATABINDING_CALLBACK = 
        "org.objectweb.celtix.bindings.databinding.callback";
    public static final String SERVER_BINDING_ENDPOINT_CALLBACK = 
        "org.objectweb.celtix.server.binding.endpoint.callback";
    public static final String ENDPOINT  = 
        "org.objectweb.celtix.server.binding.endpoint";
    public static final String DISPATCH  = 
        "org.objectweb.celtix.bindings.dispatch";

    /**
     * Prevents instantiation.
     */
    private BindingContextUtils() {
    }
    
    public static void storeDataBindingCallback(MessageContext context, DataBindingCallback callback) {
        context.put(DATABINDING_CALLBACK, callback);
        context.setScope(DATABINDING_CALLBACK, MessageContext.Scope.HANDLER); 
    }
    
    public static DataBindingCallback retrieveDataBindingCallback(MessageContext context) {
        return (DataBindingCallback)context.get(DATABINDING_CALLBACK);
    }
    
    public static void storeServerBindingEndpointCallback(MessageContext context, 
                                                          ServerBindingEndpointCallback sbec) {
        context.put(SERVER_BINDING_ENDPOINT_CALLBACK, sbec);
        context.setScope(SERVER_BINDING_ENDPOINT_CALLBACK, MessageContext.Scope.HANDLER); 
    }
    
    public static ServerBindingEndpointCallback retrieveServerBindingEndpointCallback(
        MessageContext context) {
        return (ServerBindingEndpointCallback)context.get(SERVER_BINDING_ENDPOINT_CALLBACK);
    }
    
    public static void storeMethod(MessageContext context, Method method) {
        context.put(ObjectMessageContext.METHOD_OBJ, method);
        context.setScope(ObjectMessageContext.METHOD_OBJ, MessageContext.Scope.HANDLER); 
    }
    
    public static Method retrieveMethod(MessageContext context) {
        return (Method)context.get(ObjectMessageContext.METHOD_OBJ);
    }
    
    public static void storeEndpoint(MessageContext context, Endpoint endpoint) {
        context.put(ENDPOINT, endpoint);
        context.setScope(ENDPOINT, MessageContext.Scope.HANDLER); 
    }
    
    public static Endpoint retrieveEndpoint(MessageContext context) {
        return (Endpoint)context.get(ENDPOINT);
    }
   
    public static String retrieveCorrelationID(MessageContext context) {
        return (String)context.get(CORRELATION_IN);
    }
    
    public static void storeDispatch(MessageContext context, boolean dispatch) {
        context.put(DISPATCH, dispatch ? Boolean.TRUE : Boolean.FALSE);
        context.setScope(DISPATCH, MessageContext.Scope.HANDLER); 
    }
   
    public static boolean retrieveDispatch(MessageContext context) {
        Boolean b = (Boolean)context.get(DISPATCH);
        return null == b || b.booleanValue();
    }
    
    public static ServerTransport retrieveServerTransport(MessageContext context) {
        Transport t = (Transport)context.get(TRANSPORT_PROPERTY);
        if (t instanceof ServerTransport) {
            return (ServerTransport)t;
        }
        return null;
    }

    

}
