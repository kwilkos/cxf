package org.objectweb.celtix.bus.jaxws;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.Oneway;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.spi.ServiceDelegate;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;


public final class EndpointInvocationHandler implements BindingProvider, InvocationHandler
{
    private static final Logger LOG = LogUtils.getL7dLogger(EndpointInvocationHandler.class);
    
    private final ClientBinding clientBinding;
    private Map<String, Object> requestContext;
    private Map<String, Object> responseContext;
    
    private final Class<?> portTypeInterface;
    private final Bus bus;
    private JAXBContext context;
    private final ServiceDelegate service;
    
    public EndpointInvocationHandler(Bus b, EndpointReferenceType reference,
                                     ServiceDelegate s, Configuration configuration, Class<?> portSEI) {
        bus = b;
        service = s;
        portTypeInterface = portSEI;
        clientBinding = createBinding(reference, configuration);
        try {
            context = JAXBEncoderDecoder.createJAXBContextForClass(portSEI);
        } catch (JAXBException ex1) {
            // TODO Auto-generated catch block
            ex1.printStackTrace();
            context = null;
        }
    }

    public Object invoke(Object proxy, Method method, Object args[]) throws Exception {
        
        LOG.info("EndpointInvocationHandler: invoke");

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
        objMsgContext.putAll(getRequestContext());
        
        objMsgContext.put(ObjectMessageContext.REQUEST_PROXY, proxy);
       
        objMsgContext.setMethod(method);
        objMsgContext.setMessageObjects(parameters);

        boolean isOneway = (method.getAnnotation(Oneway.class) != null) ? true : false;
        boolean isAsync = method.getName().endsWith("Async");

       
        if (isOneway) {
            clientBinding.invokeOneWay(objMsgContext,
                                       new JAXBDataBindingCallback(method,
                                                                   DataBindingCallback.Mode.PARTS,
                                                                   context));
        } else if (isAsync) {         
            Future<ObjectMessageContext> objMsgContextAsynch =
                clientBinding.invokeAsync(objMsgContext,
                                          new JAXBDataBindingCallback(method,
                                                                      DataBindingCallback.Mode.PARTS,
                                                                      context)
                                                                      , service.getExecutor()); 
            
            Response<?> r = new AsyncResponse<Object>(objMsgContextAsynch, Object.class);
            if (parameters.length > 0 && parameters[parameters.length - 1] instanceof AsyncHandler) {
                // callback style
                AsyncCallbackFuture f = new AsyncCallbackFuture(r, 
                    (AsyncHandler)parameters[parameters.length - 1]);
                // service must always have an executor associated with it
                service.getExecutor().execute(f);
                return f;
                
                
            } else {
                return r;
            }

            
        } else {
            objMsgContext = clientBinding.invoke(objMsgContext,
                                                 new JAXBDataBindingCallback(method,
                                                                             DataBindingCallback.Mode.PARTS,
                                                                             context));
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
        binding.configureSystemHandlers(c);
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
