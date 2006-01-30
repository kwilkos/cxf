package org.objectweb.celtix.bus.jaxws;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;

import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class DispatchImpl<T> implements Dispatch<T> {

    private Map<String, Object> requestContext;
    private Map<String, Object> responseContext;

    private Bus bus;
    private ClientBinding cb;
    private EndpointReferenceType ref;
    private Mode mode;
    private DynamicDataBindingCallback callback;
    private Class<T> cl;


    DispatchImpl(Bus b, EndpointReferenceType r, Service.Mode m, Class<T> clazz) {
        bus = b;
        ref = r;
        mode = Mode.fromServiceMode(m);        
        cl = clazz;
        cb = createClientBinding();
        callback = new DynamicDataBindingCallback(cl, mode);
    }

    public Binding getBinding() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Object> getRequestContext() {
        if (requestContext == null) {
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

    public T invoke(T obj) {

        ObjectMessageContext objMsgContext = cb.createObjectContext();
        // TODO
        // RequestConetxts needed to be populated based on JAX-WS mandatory
        // properties
        // Further copied into ObjectMessageContext so as to decouple context
        // across invocations
        objMsgContext.putAll(getRequestContext());
        //objMsgContext.setMessageObjects(obj);
        if (mode == Mode.MESSAGE) {
            objMsgContext.setMessage(obj);
        } else if (mode == Mode.PAYLOAD) {
            objMsgContext.setPayload(obj);
        }
        
        try {
            objMsgContext = cb.invoke(objMsgContext, callback);
           
            if (mode == Mode.MESSAGE) {
                return cl.cast(objMsgContext.getMessage());
            } else if (mode == Mode.PAYLOAD) {
                return cl.cast(objMsgContext.getPayload());
            }
            //return cl.cast(objMsgContext.getMessageObjects()[0]);
           
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return null;
    }

    public Future<?> invokeAsync(T arg0, AsyncHandler<T> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Response<T> invokeAsync(T arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void invokeOneWay(T arg0) {
        // TODO Auto-generated method stub

    }

    private ClientBinding createClientBinding() {
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

}
