package org.objectweb.celtix.buslifecycle;

import java.util.ArrayList;
import java.util.List;

public class CeltixBusLifeCycleManager implements BusLifeCycleManager {

    private final List<BusLifeCycleListener> listeners;
    
    public CeltixBusLifeCycleManager() {
        listeners = new ArrayList<BusLifeCycleListener>();
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.celtix.buslifecycle.BusLifeCycleManager#registerLifeCycleListener(
     * org.objectweb.celtix.buslifecycle.BusLifeCycleListener)
     */
    public void registerLifeCycleListener(BusLifeCycleListener listener) {
        listeners.add(listener);
        
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.buslifecycle.BusLifeCycleManager#unregisterLifeCycleListener(
     * org.objectweb.celtix.buslifecycle.BusLifeCycleListener)
     */
    public void unregisterLifeCycleListener(BusLifeCycleListener listener) {
        listeners.remove(listener);      
    }
    
    void initComplete() {
        for (BusLifeCycleListener listener : listeners) {
            listener.initComplete();
        }
    }
    
    void preShutdown() {
        // TODO inverse order of registration?
        for (BusLifeCycleListener listener : listeners) {
            listener.preShutdown();
        }
    }
    
    void postShutdown() {
        // TODO inverse order of registration?
        for (BusLifeCycleListener listener : listeners) {
            listener.postShutdown();
        }
    }
        
}
