package org.apache.cxf.endpoint;

import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.xml.ws.WebServiceException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.bindings.Binding;
import org.apache.cxf.bindings.BindingFactory;
import org.apache.cxf.bindings.BindingFactoryManager;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptors.AbstractBasicInterceptorProvider;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;

public class EndpointImpl extends AbstractBasicInterceptorProvider implements Endpoint {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointImpl.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    private Service service;
    private Binding binding;
    private EndpointInfo endpointInfo;
    private Executor executor;
    private Bus bus;
    
    public EndpointImpl(Bus bus, Service s, EndpointInfo ei) {
        this.bus = bus;
        service = s;
        endpointInfo = ei;
        createBinding(endpointInfo.getBinding());
    }

    public EndpointInfo getEndpointInfo() {
        return endpointInfo;
    }

    public Service getService() {
        return service;
    }

    public Binding getBinding() {
        return binding;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor e) {
        executor = e;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    final void createBinding(BindingInfo bi) {
        String namespace = bi.getBindingId();
        BindingFactory bf = null;
        try {
            bf = bus.getExtension(BindingFactoryManager.class).getBindingFactory(namespace);
            binding = bf.createBinding(bi);
        } catch (BusException ex) {
            throw new WebServiceException(ex);
        }
        if (null == bf) {
            Message msg = new Message("NO_BINDING_FACTORY", BUNDLE, namespace);
            throw new WebServiceException(msg.toString());
        }
    }

}
