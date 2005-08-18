package org.objectweb.celtix.buslifecycle;

public interface BusLifeCycleManager {
    void registerLifeCycleListener(BusLifeCycleListener listener);
    void unregisterLifeCycleListener(BusLifeCycleListener listener);
}
