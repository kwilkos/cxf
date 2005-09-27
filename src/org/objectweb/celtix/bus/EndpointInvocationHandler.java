package org.objectweb.celtix.bus;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.Oneway;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;

import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.WSDLManager;


public class EndpointInvocationHandler implements BindingProvider, InvocationHandler
{
    protected ClientBinding clientBinding;
    protected Map<String, Object> requestContext;
    protected Map<String, Object> responseContext;
    
    private final Class<? extends Remote> portTypeInterface;
    private final Bus bus;
    
    public EndpointInvocationHandler(Bus b, EndpointReferenceType reference,
            Class<? extends Remote> portSEI) {
        bus = b;
        portTypeInterface = portSEI;
        clientBinding = createBinding(reference);
    }

    public Object invoke(Object proxy, Method method, Object args[])
        throws Throwable {
        
        if (portTypeInterface.equals(method.getDeclaringClass())) {
            return invokeSEIMethod(proxy, method, args);
        } else {
            return method.invoke(this, args);
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
        throws Throwable {

        ObjectMessageContext objMsgContext = clientBinding.createObjectContext();
        //TODO
        //RequestConetxts needed to be populated based on JAX-WS mandatory properties
        //Further copied into ObjectMessageContext so as to decouple context across invocations
        objMsgContext.put("org.objectweb.celtix.context.request", getRequestContext());
        
        //REVISIT this property could be part of the requqest context.
        objMsgContext.put("org.objectweb.celtix.proxy", proxy);
        
        objMsgContext.setMethod(method);
        objMsgContext.setMessageObjects(parameters);

        boolean isOneway = (method.getAnnotation(Oneway.class) != null) ? true : false;

        if (isOneway) {
            clientBinding.invokeOneWay(objMsgContext);
        } else {
            objMsgContext = clientBinding.invoke(objMsgContext);
        }
        
        return objMsgContext.getReturn();
    }
    
    protected ClientBinding createBinding(EndpointReferenceType ref) {

        WSDLManager wsdlManager = bus.getWSDLManager();
        ClientBinding binding = null;
        try {
            Port endpoint = EndpointReferenceUtils.getPort(wsdlManager, ref);
            
            assert endpoint != null : "unable to find endpoint for " + ref;
            String bindingId = getBindingId(endpoint.getBinding());

            BindingFactory factory = bus.getBindingManager().getBindingFactory(bindingId);
            assert factory != null : "unable to find binding factory for " + ref;
            binding = factory.createClientBinding(ref);
        } catch (Exception ex) {
            throw new WebServiceException(ex);
        }
        return binding;
    }

    private String getBindingId(javax.wsdl.Binding binding) {

        List list = binding.getExtensibilityElements();
        
        if (list.isEmpty()) {
            throw new WebServiceException("Could not get the extension element URI");
        }

        ExtensibilityElement extElement = (ExtensibilityElement) list.get(0);
        
        return extElement.getElementType().getNamespaceURI();
    }
}
