package org.objectweb.celtix.endpoint;

import java.util.concurrent.Executor;

import org.objectweb.celtix.bindings.Binding;

public interface Endpoint {
   
    Binding getBinding();
    
    void setExecutor(Executor executor);
    
    Executor getExecutor();
    
  
}
