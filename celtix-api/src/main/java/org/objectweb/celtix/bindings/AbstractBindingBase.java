package org.objectweb.celtix.bindings;

import javax.xml.ws.Binding;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.handlers.HandlerInvoker;

public abstract class AbstractBindingBase implements BindingBase {

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

    protected abstract AbstractBindingImpl getBindingImpl();


}
