package org.apache.cxf.endpoint;

import java.util.concurrent.Executor;

import org.apache.cxf.bindings.Binding;
import org.apache.cxf.interceptors.InterceptorProvider;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;

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
