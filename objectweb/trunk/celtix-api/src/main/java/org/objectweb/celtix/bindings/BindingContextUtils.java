package org.objectweb.celtix.bindings;


import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.OutputStreamMessageContext;

import static org.objectweb.celtix.bindings.JAXWSConstants.DATABINDING_CALLBACK_PROPERTY;
import static org.objectweb.celtix.bindings.JAXWSConstants.DISPATCH_PROPERTY;
import static org.objectweb.celtix.bindings.JAXWSConstants.SERVER_BINDING_ENDPOINT_CALLBACK_PROPERTY;
import static org.objectweb.celtix.context.InputStreamMessageContext.ASYNC_ONEWAY_DISPATCH;
import static org.objectweb.celtix.context.InputStreamMessageContext.DECOUPLED_RESPONSE;
import static org.objectweb.celtix.context.ObjectMessageContext.CORRELATION_IN;


/**
 * Holder for utility methods relating to contexts.
 */

public final class BindingContextUtils {

    /**
     * Prevents instantiation.
     */
    private BindingContextUtils() {
    }
    
    /**
     * Store data binding callback in message context.
     *
     * @param context the message context
     * @param callback the data binding callback
     */
    public static void storeDataBindingCallback(MessageContext context, DataBindingCallback callback) {
        context.put(DATABINDING_CALLBACK_PROPERTY, callback);
        context.setScope(DATABINDING_CALLBACK_PROPERTY, MessageContext.Scope.HANDLER); 
    }
    
    /**
     * Retrieve data binding callback from message context.
     *
     * @param context the message context
     * @return the data binding callback
     */
    public static DataBindingCallback retrieveDataBindingCallback(MessageContext context) {
        return (DataBindingCallback)context.get(DATABINDING_CALLBACK_PROPERTY);
    }
        
    /**
     * Store server binding endpoint callback in message context.
     *
     * @param context the message context
     * @param sbec the server binding endpoint callback
     */
    public static void storeServerBindingEndpointCallback(MessageContext context, 
                                                          ServerBindingEndpointCallback sbec) {
        context.put(SERVER_BINDING_ENDPOINT_CALLBACK_PROPERTY, sbec);
        context.setScope(SERVER_BINDING_ENDPOINT_CALLBACK_PROPERTY, MessageContext.Scope.HANDLER); 
    }
    
    /**
     * Retrieve server binding endpoint callback from message context.
     *
     * @param context the message context
     * @return the server binding endpoint callback
     */
    public static ServerBindingEndpointCallback retrieveServerBindingEndpointCallback(
        MessageContext context) {
        return (ServerBindingEndpointCallback)context.get(SERVER_BINDING_ENDPOINT_CALLBACK_PROPERTY);
    }
        
    /**
     * Checks if a method object is stored in the context and if it is annotated
     * with a Oneway annotation.
     * 
     * @param context
     * @return true if a method object is stored in the context and it has a Oneway annotation.
     */
    public static boolean isOnewayMethod(MessageContext context) {
        DataBindingCallback cb = retrieveDataBindingCallback(context);
        if (cb == null) {
            if (!context.containsKey(OutputStreamMessageContext.ONEWAY_MESSAGE_TF)) {
                return false;
            }
            return ((Boolean)context.get(OutputStreamMessageContext.ONEWAY_MESSAGE_TF)).booleanValue();
        }
        return cb.isOneWay();
    }
    
    /**
     * Checks if a the oneway property is set in the context if its value is true
     * (indicating that no response is expected from the transport).
     * 
     * @param context
     * @return true if a method object is stored in the context and it has a Oneway annotation.
     */
    public static boolean isOnewayTransport(MessageContext context) {
        Boolean b = (Boolean)context.get(OutputStreamMessageContext.ONEWAY_MESSAGE_TF);
        if (null != b) {
            return b.booleanValue();
        }
        return false;
    }
        
    /**
     * Retrieve correlation id from message context.
     *
     * @param context the message context
     * @return the correlation id
     */
    public static String retrieveCorrelationID(MessageContext context) {
        return (String)context.get(CORRELATION_IN);
    }
    
    /**
     * Store dispatch property in message context.
     *
     * @param context the message context
     * @param dispatch value of the dispatch property
     */
    public static void storeDispatch(MessageContext context, boolean dispatch) {
        context.put(DISPATCH_PROPERTY, dispatch ? Boolean.TRUE : Boolean.FALSE);
        context.setScope(DISPATCH_PROPERTY, MessageContext.Scope.HANDLER); 
    }
   
    /**
     * Retrieve value of dispatch property from message context.
     *
     * @param context the message context
     * @return the value of dispatch property
     */
    public static boolean retrieveDispatch(MessageContext context) {
        Boolean b = (Boolean)context.get(DISPATCH_PROPERTY);
        return null == b || b.booleanValue();
    }
    
    /**
     * Store async oneway dispatch property in message context.
     *
     * @param context the message context
     * @param async value of the dispatch property
     */
    public static void storeAsyncOnewayDispatch(MessageContext context, boolean async) {
        context.put(ASYNC_ONEWAY_DISPATCH, async ? Boolean.TRUE : Boolean.FALSE);
        context.setScope(ASYNC_ONEWAY_DISPATCH, MessageContext.Scope.HANDLER); 
    }
   
    /**
     * Retrieve value of async oneway dispatch property from message context.
     *
     * @param context the message context
     * @return the value of async oneway dispatch property
     */
    public static boolean retrieveAsyncOnewayDispatch(MessageContext context) {
        Boolean b = (Boolean)context.get(ASYNC_ONEWAY_DISPATCH);
        return b != null && b.booleanValue();
    }

    /**
     * Store decoupled response property in message context.
     *
     * @param context the message context
     * @param decoupled value of the decoupled response property
     */
    public static void storeDecoupledResponse(MessageContext context, boolean decoupled) {
        context.put(DECOUPLED_RESPONSE, decoupled ? Boolean.TRUE : Boolean.FALSE);
        context.setScope(DECOUPLED_RESPONSE, MessageContext.Scope.HANDLER); 
    }
   
    /**
     * Retrieve value of decoupled response property from message context.
     *
     * @param context the message context
     * @return the value of decoupled response property
     */
    public static boolean retrieveDecoupledResponse(MessageContext context) {
        Boolean b = (Boolean)context.get(DECOUPLED_RESPONSE);
        return b != null && b.booleanValue();
    }
    
}
