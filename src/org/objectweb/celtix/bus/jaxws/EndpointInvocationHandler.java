package org.objectweb.celtix.bus.jaxws;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.Oneway;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.ObjectMessageContext;


public final class EndpointInvocationHandler implements BindingProvider, InvocationHandler
{
    private static final Logger LOG = LogUtils.getL7dLogger(EndpointInvocationHandler.class);
    
    private final ClientBinding clientBinding;
    private Map<String, Object> requestContext;
    private Map<String, Object> responseContext;
    
    private final Class<?> portTypeInterface;
    private final Bus bus;
    
    public EndpointInvocationHandler(Bus b, EndpointReferenceType reference,
                                     Configuration configuration, Class<?> portSEI) {
        bus = b;
        portTypeInterface = portSEI;
        clientBinding = createBinding(reference, configuration);
    }

    public Object invoke(Object proxy, Method method, Object args[]) throws Exception {

        if (portTypeInterface.equals(method.getDeclaringClass())) {
            return invokeSEIMethod(proxy, method, args);
        }             

        try {
            return method.invoke(this, args);
        } catch (InvocationTargetException ite) {
            LOG.log(Level.SEVERE, "BINDING_PROVIDER_METHOD_EXC", method.getName());
            if (WebServiceException.class.isAssignableFrom(ite.getCause().getClass())) {
                throw (WebServiceException)ite.getCause();
            }
            throw new WebServiceException(ite.getCause());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "BINDING_PROVIDER_METHOD_EXC", method.getName());
            throw new WebServiceException(ex);
        } 
    }

    public Binding getBinding() {
        return clientBinding.getBinding();
    }
    
    public Map<String, Object> getRequestContext() {
        if (requestContext == null) {
            //REVISIT Need to Create a Request/ResponseContext classs to derive out of a
            //ContextBase class.
            requestContext = new HashMap<String, Object>();
        }
        return requestContext;
    }
    
    public Map<String, Object> getResponseContext() {
        if (responseContext == null) {
            responseContext = new HashMap<String, Object>();
        }
        return responseContext;
    }
    
    private Object invokeSEIMethod(Object proxy, Method method, Object parameters[])
        throws Exception {

        ObjectMessageContext objMsgContext = clientBinding.createObjectContext();
        //TODO
        //RequestConetxts needed to be populated based on JAX-WS mandatory properties
        //Further copied into ObjectMessageContext so as to decouple context across invocations
        objMsgContext.putAll(getRequestContext());
        
        //REVISIT this property could be part of the requqest context.
        objMsgContext.put(ObjectMessageContext.REQUEST_PROXY, proxy);
        
        objMsgContext.setMethod(method);
        objMsgContext.setMessageObjects(parameters);

        boolean isOneway = (method.getAnnotation(Oneway.class) != null) ? true : false;

        if (isOneway) {
            clientBinding.invokeOneWay(objMsgContext,
                                       new JAXBDataBindingCallback(method,
                                                                   DataBindingCallback.Mode.PARTS));
        } else {
            objMsgContext = clientBinding.invoke(objMsgContext,
                                                 new JAXBDataBindingCallback(method,
                                                                             DataBindingCallback.Mode.PARTS));
        }

        if (objMsgContext.getException() != null) {
            LOG.log(Level.INFO, "ENDPOINT_INVOCATION_FAILED", method.getName());
            if (isValidException(objMsgContext)) {
                throw (Exception)objMsgContext.getException();
            } else {                
                throw new ProtocolException(objMsgContext.getException());
            }
        }
        
        return objMsgContext.getReturn();
    }
    
    protected ClientBinding createBinding(EndpointReferenceType ref, Configuration c) {

        ClientBinding binding = null;
        try {
            
            String bindingId = c.getString("bindingId");            
            BindingFactory factory = bus.getBindingManager().getBindingFactory(bindingId);
            assert factory != null : "unable to find binding factory for " + bindingId;
            binding = factory.createClientBinding(ref);
        } catch (Exception ex) {
            throw new WebServiceException(ex);
        }
        return binding;
    }
    
    private boolean isValidException(ObjectMessageContext objContext) {
        Method method = objContext.getMethod();
        Throwable t = objContext.getException();
        
        boolean val = ProtocolException.class.isAssignableFrom(t.getClass()) 
                   || WebServiceException.class.isAssignableFrom(t.getClass());
        
        if (!val) {
            for (Class<?> clazz : method.getExceptionTypes()) {
                if (clazz.isAssignableFrom(t.getClass())) {
                    val = true;
                    break;
                }
            }
        }
        
        return val;
    }
}
