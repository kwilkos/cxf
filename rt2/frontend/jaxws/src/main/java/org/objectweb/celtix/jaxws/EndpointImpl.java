package org.objectweb.celtix.jaxws;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.jaxws.support.JaxWsServiceFactoryBean;
import org.objectweb.celtix.service.Service;

public class EndpointImpl extends javax.xml.ws.Endpoint {
    
    Bus bus;
    String bindingURI;
    Object implementor;
    org.objectweb.celtix.endpoint.EndpointImpl endpoint;
    javax.xml.ws.Binding binding;
    Service service;
    boolean published;
    
    public EndpointImpl(Bus b, Object i, String uri) {
        bus = b;
        implementor = i;
        bindingURI = uri;
        
        // build up the Service model
        JaxWsServiceFactoryBean serviceFactory = new JaxWsServiceFactoryBean();
        serviceFactory.setServiceClass(implementor.getClass());
        serviceFactory.setBus(bus);
        service = serviceFactory.create();
        
        // use service's endpoint factory to create the celtix endpoint - this
        // creates the celtix binding etc. also.
        
        
        // create JAX-WS Binding, i.e. handler chain and add JAX-WS handler interceptors to 
        // celtix binding (even if the handler chain is empty)
        
        // ...
        
    }
    
    
    public Binding getBinding() {
        // TODO Auto-generated method stub
        return null;
    }

    // TO DO: verify that on the server side we can have a 1:1 relationship ebtween Service and Endpoint
    
    public void setExecutor(Executor executor) {
        endpoint.getService().setExecutor(executor);        
    }
    
    public Executor getExecutor() {
        return endpoint.getService().getExecutor();
    }

    @Override
    public Object getImplementor() {
        return implementor;
    }

    @Override
    public List<Source> getMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isPublished() {
        return published;
    }

    @Override
    public void publish(Object arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void publish(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public void setMetadata(List<Source> arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setProperties(Map<String, Object> arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        
    }
    
    public Endpoint getEndpoint() {
        return endpoint;
    }
    
    
    
}
