package org.objectweb.celtix.endpoint;

import org.objectweb.celtix.service.model.OperationInfo;

public interface Client {
    
    Object invoke(OperationInfo oi, Object[] params);
   
}
