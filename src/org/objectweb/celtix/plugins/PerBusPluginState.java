package org.objectweb.celtix.plugins;

public interface PerBusPluginState {
    
    void initComplete();
    
    boolean inInvocationContext();
    
    void shutdownServer();
    
    void shutdownClient();
    
    void sutdownComplete();
    
}
