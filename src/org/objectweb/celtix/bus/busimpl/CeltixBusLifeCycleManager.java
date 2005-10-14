package org.objectweb.celtix.bus.busimpl;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;

public class CeltixBusLifeCycleManager implements BusLifeCycleManager {

    private List<BusLifeCycleListener> listeners;
    
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
        for (BusLifeCycleListener listener : listeners) {
            listener.preShutdown();
        }
    }
    
    void postShutdown() {
        for (BusLifeCycleListener listener : listeners) {
            listener.postShutdown();
        }
    }
        
}
