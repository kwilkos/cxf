package org.objectweb.celtix.endpoint;

import java.util.Map;

import org.objectweb.celtix.service.model.OperationInfo;

public interface Client {
    
    /**
     * Invokes an operation syncronously
     * @param oi  The operation to be invoked
     * @param params  The params that matches the parts of the input message of the operation
     * @param context  Optional (can be null) contextual information for the invocation
     * @return The return values that matche the parts of the output message of the operation
     */
    Object[] invoke(OperationInfo oi,
                    Object[] params,
                    Map<String, Object> context);

    Endpoint getEndpoint();
   
}
