package org.objectweb.celtix.endpoint;

import java.util.concurrent.Executor;

import org.objectweb.celtix.bindings.Binding;
import org.objectweb.celtix.interceptors.InterceptorProvider;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.EndpointInfo;

/**
 * Represents an endpoint that receives messages. 
 *
 */
public interface Endpoint extends InterceptorProvider {

    EndpointInfo getEndpointInfo();
    
    Binding getBinding();
    
    Service getService();

    void setExecutor(Executor executor);
    
    Executor getExecutor();
}
