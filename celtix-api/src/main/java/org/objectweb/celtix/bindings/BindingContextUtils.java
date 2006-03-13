package org.objectweb.celtix.bindings;


import java.lang.reflect.Method;

import javax.jws.Oneway;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.Transport;

import static org.objectweb.celtix.bindings.JAXWSConstants.BINDING_PROPERTY;
import static org.objectweb.celtix.bindings.JAXWSConstants.BUS_PROPERTY;
import static org.objectweb.celtix.bindings.JAXWSConstants.DATABINDING_CALLBACK_PROPERTY;
import static org.objectweb.celtix.bindings.JAXWSConstants.DISPATCH_PROPERTY;
import static org.objectweb.celtix.bindings.JAXWSConstants.ENDPOINT_PROPERTY;
import static org.objectweb.celtix.bindings.JAXWSConstants.SERVER_BINDING_ENDPOINT_CALLBACK_PROPERTY;
import static org.objectweb.celtix.bindings.JAXWSConstants.TRANSPORT_PROPERTY;
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
     * @returned the data binding callback
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
     * @returned the server binding endpoint callback
     */
    public static ServerBindingEndpointCallback retrieveServerBindingEndpointCallback(
        MessageContext context) {
        return (ServerBindingEndpointCallback)context.get(SERVER_BINDING_ENDPOINT_CALLBACK_PROPERTY);
    }
    
    /**
     * Store method in message context.
     *
     * @param context the message context
     * @param method the method
     */
    public static void storeMethod(MessageContext context, Method method) {
        context.put(ObjectMessageContext.METHOD_OBJ, method);
        context.setScope(ObjectMessageContext.METHOD_OBJ, MessageContext.Scope.HANDLER); 
    }
    
    /**
     * Retrieve method from message context.
     *
     * @param context the message context
     * @returned the method
     */
    public static Method retrieveMethod(MessageContext context) {
        return (Method)context.get(ObjectMessageContext.METHOD_OBJ);
    }
    
    /**
     * Checks if a method object is stored in the context and if it is annotated
     * with a Oneway annotation.
     * 
     * @param context
     * @return true if a method objetc is stored in the context and it has a Oneway annotation.
     */
    public static boolean isOneway(MessageContext context) {
        Boolean b = (Boolean)context.get(OutputStreamMessageContext.ONEWAY_MESSAGE_TF);
        if (null != b) {
            return b.booleanValue();
        }
        Method method = BindingContextUtils.retrieveMethod(context);
        if (method != null) {
            return method.getAnnotation(Oneway.class) != null;
        }
        return false;
    }
    
    
    /**
     * Store endpoint in message context.
     *
     * @param context the message context
     * @param endpoint the endpoint
     */
    public static void storeEndpoint(MessageContext context, Endpoint endpoint) {
        context.put(ENDPOINT_PROPERTY, endpoint);
        context.setScope(ENDPOINT_PROPERTY, MessageContext.Scope.HANDLER); 
    }
    
    /**
     * Retrieve endpoint from message context.
     *
     * @param context the message context
     * @returned the endpoint
     */
    public static Endpoint retrieveEndpoint(MessageContext context) {
        return (Endpoint)context.get(ENDPOINT_PROPERTY);
    }
   
    /**
     * Retrieve correlation id from message context.
     *
     * @param context the message context
     * @returned the correlation id
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
     * @returned the value of dispatch property
     */
    public static boolean retrieveDispatch(MessageContext context) {
        Boolean b = (Boolean)context.get(DISPATCH_PROPERTY);
        return null == b || b.booleanValue();
    }
    
    /**
     * Store transport in message context.
     *
     * @param context the message context
     * @param transport the transport
     */
    public static void storeTransport(MessageContext context, Transport transport) {
        context.put(TRANSPORT_PROPERTY, transport);
        context.setScope(TRANSPORT_PROPERTY, MessageContext.Scope.HANDLER);
    }
    
    /**
     * Retrieve transport from message context.
     *
     * @param context the message context
     * @returned the transport
     */
    public static Transport retrieveTransport(MessageContext context) {
        return (Transport)context.get(TRANSPORT_PROPERTY);
    }
    
    /**
     * Retrieve ClientTransport from the context.
     *
     * @param context the message context
     * @returned the retrieved ClientTransport
     */
    public static ClientTransport retrieveClientTransport(MessageContext context) {
        Object transport = context.get(TRANSPORT_PROPERTY);
        return transport instanceof ClientTransport
               ? (ClientTransport)transport
               : null;
    }

    /**
     * Retrieve ServerTransport from the context.
     *
     * @param context the message context
     * @returned the retrieved ServerTransport
     */
    public static ServerTransport retrieveServerTransport(MessageContext context) {
        return (ServerTransport)context.get(TRANSPORT_PROPERTY);
    }
    
    /**
     * Store binding in message context.
     *
     * @param context the message context
     * @param binding the binding
     */
    public static void storeBinding(MessageContext context, BindingBase binding) {
        context.put(BINDING_PROPERTY, binding);
        context.setScope(BINDING_PROPERTY, MessageContext.Scope.HANDLER);
    }
    
    /**
     * Retrieve binding from message context.
     *
     * @param context the message context
     * @returned the binding
     */
    public static BindingBase retrieveBinding(MessageContext context) {
        return (BindingBase)context.get(BINDING_PROPERTY);
    }
    
    /**
     * Retrieve ClientBinding from the context.
     *
     * @param context the message context
     * @returned the retrieved ClientBinding
     */
    public static ClientBinding retrieveClientBinding(MessageContext context) {
        return (ClientBinding)context.get(BINDING_PROPERTY);
    }

    /**
     * Retrieve ServerBinding from the context.
     *
     * @param context the message context
     * @returned the retrieved ServerBinding
     */
    public static ServerBinding retrieveServerBinding(MessageContext context) {
        return (ServerBinding)context.get(BINDING_PROPERTY);
    }
    
    /**
     * Store bus in message context.
     *
     * @param context the message context
     * @param bus the bus
     */
    public static void storeBus(MessageContext context, Bus bus) {
        context.put(BUS_PROPERTY, bus);
        context.setScope(BUS_PROPERTY, MessageContext.Scope.HANDLER);
    }
    
    /**
     * Retrieve bus from message context.
     *
     * @param context the message context
     * @returned the bus
     */
    public static Bus retrieveBus(MessageContext context) {
        return (Bus)context.get(BUS_PROPERTY);
    }
}
