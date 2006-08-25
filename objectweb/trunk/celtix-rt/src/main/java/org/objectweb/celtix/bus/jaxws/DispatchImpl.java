package org.objectweb.celtix.bus.jaxws;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.Dispatch;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;

import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class DispatchImpl<T> extends BindingProviderImpl implements Dispatch<T> {
    private static final Logger LOG = LogUtils.getL7dLogger(EndpointInvocationHandler.class);
    
    protected ClientBinding cb;
    protected DynamicDataBindingCallback callback;
    
    private Bus bus;
    private EndpointReferenceType ref;
    private Mode mode;
    private Class<T> cl;
    private Executor executor;
    private JAXBContext context;
    private boolean initialised;
    
    


    DispatchImpl(Bus b, EndpointReferenceType r, Service.Mode m, Class<T> clazz, Executor e) {
        bus = b;
        ref = r;
        mode = Mode.fromServiceMode(m);        
        cl = clazz;
        cb = null;
        callback = null;
        executor = e;
        context = null;
        initialised = false;
    }
    
    DispatchImpl(Bus b, EndpointReferenceType r, Service.Mode m, 
                 JAXBContext ctx, Class<T> clazz, Executor e) {
        bus = b;
        ref = r;
        mode = Mode.fromServiceMode(m);        
        cb = null;
        callback = null;
        executor = e;
        context = ctx;
        cl = clazz;
        initialised = false;
    }
    
    protected void init() {
        cb = createClientBinding();
        setBinding(cb.getBinding());
        if (context == null) {
            callback = new DynamicDataBindingCallback(cl, mode);   
        } else {
            callback = new DynamicDataBindingCallback(context, mode);
        }
        initialised = true;
    }
    
    public T invoke(T obj) {
        
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Dispatch: invoke called");
        }
        
        if (!initialised) {
            init();
        }

        ObjectMessageContext objMsgContext = cb.createObjectContext();
        // TODO
        // RequestConetxts needed to be populated based on JAX-WS mandatory
        // properties
        // Further copied into ObjectMessageContext so as to decouple context
        // across invocations
        objMsgContext.putAll(getRequestContext());
        objMsgContext.setMessageObjects(obj);
        
        try {
            objMsgContext = cb.invoke(objMsgContext, callback);
        } catch (Exception ex) {
            throwWebServiceException(ex);
        } finally {
            //Update Response Context 
            throwProtocolException(objMsgContext.getException());
        }
        
        populateResponseContext(objMsgContext);

        return cl.cast(objMsgContext.getReturn());
    }

    public Future<?> invokeAsync(T obj, AsyncHandler<T> asyncHandler) {
        
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Dispatch: callback invokeAsync called");
        }
        
        if (!initialised) {
            init();
        }   

        ObjectMessageContext objMsgContext = cb.createObjectContext();
        objMsgContext.putAll(getRequestContext());
        objMsgContext.setMessageObjects(obj);
        
        AsyncCallbackFuture future = null;
        
        try {
            Future<ObjectMessageContext> objMsgContextAsynch =
                cb.invokeAsync(objMsgContext, callback, executor); 
            Response<T> r = new AsyncResponse<T>(objMsgContextAsynch, cl);
            future = new AsyncCallbackFuture(r, asyncHandler);
            executor.execute(future);
        } catch (Exception ex) {
            throwWebServiceException(ex);
        } 
        
        return future;
        
    }

    public Response<T> invokeAsync(T obj) {

        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Dispatch: polling invokeAsync called");
        }
        
        if (!initialised) {
            init();
        }
        
        ObjectMessageContext objMsgContext = cb.createObjectContext();
        objMsgContext.putAll(getRequestContext());
        objMsgContext.setMessageObjects(obj);
        
        Response<T> response = null;
        
        try {
            Future<ObjectMessageContext> objMsgContextAsynch =
                cb.invokeAsync(objMsgContext, callback, executor); 
            response = new AsyncResponse<T>(objMsgContextAsynch, cl);
        } catch (Exception ex) {
            throwWebServiceException(ex);
        } 
        
        return response;
    }

    public void invokeOneWay(T obj) {
        
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Dispatch: invokeOneWay called");
        }
        
        if (!initialised) {
            init();
        }

        ObjectMessageContext objMsgContext = cb.createObjectContext();
        objMsgContext.putAll(getRequestContext());
        objMsgContext.setMessageObjects(obj);
        
        try {
            cb.invokeOneWay(objMsgContext, callback);
        } catch (Exception ex) {
            throwWebServiceException(ex);
        } 

    }
    
    public Binding getBinding() {
        if (null == super.getBinding()) {
            init();
        }
        return super.getBinding();
    }

    private  ClientBinding createClientBinding() {
        // TODO: Get bindingId from wsdl via the ref
        String bindingId = "http://schemas.xmlsoap.org/wsdl/soap/";
        ClientBinding binding = null;
        try {
            BindingFactory factory = bus.getBindingManager().getBindingFactory(bindingId);
            assert factory != null : "unable to find binding factory for " + bindingId;
            binding = factory.createClientBinding(ref);
        } catch (Exception ex) {
            throw new WebServiceException(ex);
        }
        return binding;
    }

    private void throwWebServiceException(Throwable t) {
        if (null != t) {
            LOG.log(Level.SEVERE, "DISPATCH_INVOKE_EXC", cl.getSimpleName());
            throw isJAXWSException(t) 
                  ? (WebServiceException)t 
                  : new WebServiceException(t);
        }
    }
    
    private void throwProtocolException(Throwable t) {
        if (null != t) {
            LOG.log(Level.INFO, "DISPATCH_INVOKE_EXC", cl.getSimpleName());
            throw isJAXWSException(t) 
                  ? (WebServiceException)t 
                  : new ProtocolException(t);
        }
    }
    
    private boolean isJAXWSException(Throwable t) {
        return ProtocolException.class.isAssignableFrom(t.getClass()) 
               || WebServiceException.class.isAssignableFrom(t.getClass());
    }
    
}
