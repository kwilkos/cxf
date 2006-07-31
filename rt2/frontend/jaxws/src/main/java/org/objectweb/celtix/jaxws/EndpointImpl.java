package org.objectweb.celtix.jaxws;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.endpoint.Endpoint;

public class EndpointImpl extends javax.xml.ws.Endpoint {
    
    Bus bus;
    String bindingURI;
    Object implementor;
    Endpoint endpoint;
    javax.xml.ws.Binding binding;

    public EndpointImpl(Bus b, Object i, String uri) {
        bus = b;
        implementor = i;
        bindingURI = uri;
        
        
        // build ServiceInfo and create celtix service
        
        // use service's endpoint factory to create the celtix endpoint - this
        // creates the celtix binding etc. also.
        
        // create JAX-WS Binding, i.e. handler chain and add JAX-WS handler interceptors to 
        // celtix binding (even if the handler chain is empty)
        
        // ...
        
    }
    
    
    @Override
    public Binding getBinding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Executor getExecutor() {
        return endpoint.getExecutor();
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void publish(Object arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void publish(String arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setExecutor(Executor executor) {
        endpoint.setExecutor(executor);        
    }

    @Override
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
