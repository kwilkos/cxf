package org.objectweb.celtix.endpoint;

import java.util.concurrent.Executor;

import org.objectweb.celtix.bindings.Binding;
import org.objectweb.celtix.interceptors.InterceptorProvider;
import org.objectweb.celtix.service.model.EndpointInfo;

public interface Endpoint extends InterceptorProvider {
   
    EndpointInfo getEndpointInfo();
    
    Binding getBinding();
    
    void setExecutor(Executor executor);
    
    Executor getExecutor();
    
    /*
    void start(String address);
    
    void stop();
    */

}
