package org.objectweb.celtix.jaxws.support;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Binding;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.soap2.SoapBinding;
import org.objectweb.celtix.endpoint.EndpointImpl;
import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.jaxws.bindings.BindingImpl;
import org.objectweb.celtix.jaxws.bindings.soap.SOAPBindingImpl;
import org.objectweb.celtix.jaxws.handlers.LogicalHandlerInterceptor;
import org.objectweb.celtix.jaxws.handlers.StreamHandlerInterceptor;
import org.objectweb.celtix.jaxws.handlers.soap.SOAPHandlerInterceptor;
import org.objectweb.celtix.jaxws.interceptors.WrapperClassInInterceptor;
import org.objectweb.celtix.jaxws.interceptors.WrapperClassOutInterceptor;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.EndpointInfo;

/**
 * A JAX-WS specific implementation of the Celtix {@link Endpoint} interface.
 * Extends the interceptor provider functionality of its base class by adding 
 * interceptors in which to execute the JAX-WS handlers.
 * Creates and owns an implementation of {@link Binding} in addition to the
 * Celtix {@link org.objectweb.celtix.bindings.Binding}. 
 *
 */
public class JaxwsEndpointImpl extends EndpointImpl {

    private Binding binding;
    
    public JaxwsEndpointImpl(Bus bus, Service s, EndpointInfo ei) {
        super(bus, s, ei);

        createJaxwsBinding();
        
        List<Interceptor> handlerInterceptors;
        
        handlerInterceptors = new ArrayList<Interceptor>();
        handlerInterceptors.add(new LogicalHandlerInterceptor(binding));
        if (getBinding() instanceof SoapBinding) {
            handlerInterceptors.add(new SOAPHandlerInterceptor(binding));
        } else {
             // TODO: what for non soap bindings?
        }
        handlerInterceptors.add(new StreamHandlerInterceptor(binding));
        
        List<Interceptor> fault = super.getFaultInterceptors();
        fault.addAll(handlerInterceptors);
        List<Interceptor> in = super.getInInterceptors();
        in.addAll(handlerInterceptors);
        in.add(new WrapperClassInInterceptor());
        
        List<Interceptor> out = super.getOutInterceptors();
        out.addAll(handlerInterceptors);
        out.add(new WrapperClassOutInterceptor());
    }
    
    public Binding getJaxwsBinding() {
        return binding;
    }
    
    final void createJaxwsBinding() {
        if (getBinding() instanceof SoapBinding) {
            binding = new SOAPBindingImpl((SoapBinding)getBinding());
        } else {
            binding = new BindingImpl();
        }
    }
}
