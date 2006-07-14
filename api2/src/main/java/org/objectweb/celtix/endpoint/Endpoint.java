package org.objectweb.celtix.endpoint;

import java.util.concurrent.Executor;

import org.objectweb.celtix.bindings.Binding;
import org.objectweb.celtix.servicemodel.PortInfo;

public interface Endpoint {
   
    Binding getBinding();
    
    void setExecutor(Executor executor);
    
    Executor getExecutor();
    
    PortInfo getPortInfo();
  
}
