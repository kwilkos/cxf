package org.objectweb.celtix.bindings;

import javax.xml.ws.Binding;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public abstract class AbstractBindingBase implements BindingBase {

    protected final Bus bus;
    protected final EndpointReferenceType reference;
    
    protected AbstractBindingBase(Bus b, EndpointReferenceType r) {
        bus = b;
        reference = r;
    }
    
    public Bus getBus() {
        return bus;
    }
    
    public EndpointReferenceType getEndpointReference() {
        return reference;
    }
    
    public Binding getBinding() {
        return getBindingImpl();
    }

    public ObjectMessageContext createObjectContext() {
        return new ObjectMessageContextImpl();
    }

    public HandlerInvoker createHandlerInvoker() {
        return getBindingImpl().createHandlerInvoker();
    }

    public void configureSystemHandlers(Configuration endpointConfiguration) {
        getBindingImpl().configureSystemHandlers(endpointConfiguration);
    }

    public abstract AbstractBindingImpl getBindingImpl();


}
