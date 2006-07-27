package org.objectweb.celtix.client;

import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.service.model.OperationInfo;

public interface Client {

    Endpoint getEndpoint();
    
    Object invoke(OperationInfo oi, Object[] params);
   
}
