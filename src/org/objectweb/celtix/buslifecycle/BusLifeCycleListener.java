package org.objectweb.celtix.buslifecycle;

public interface BusLifeCycleListener {

    void initComplete();
    
    void preShutdown();
    void postShutdown();
}
