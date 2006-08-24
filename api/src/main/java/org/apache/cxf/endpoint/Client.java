package org.apache.cxf.endpoint;

import java.util.Map;

import org.apache.cxf.interceptors.InterceptorProvider;
import org.apache.cxf.service.model.BindingOperationInfo;

public interface Client extends InterceptorProvider {
    
    /**
     * Invokes an operation syncronously
     * @param oi  The operation to be invoked
     * @param params  The params that matches the parts of the input message of the operation
     * @param context  Optional (can be null) contextual information for the invocation
     * @return The return values that matche the parts of the output message of the operation
     */
    Object[] invoke(BindingOperationInfo oi,
                    Object[] params,
                    Map<String, Object> context);

    Endpoint getEndpoint();
   
}
