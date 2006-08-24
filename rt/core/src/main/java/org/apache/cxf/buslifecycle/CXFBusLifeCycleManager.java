package org.apache.cxf.buslifecycle;

import java.util.ArrayList;
import java.util.List;

public class CXFBusLifeCycleManager implements BusLifeCycleManager {

    private final List<BusLifeCycleListener> listeners;
    
    public CXFBusLifeCycleManager() {
        listeners = new ArrayList<BusLifeCycleListener>();
    }
    
    /* (non-Javadoc)
     * @see org.apache.cxf.buslifecycle.BusLifeCycleManager#registerLifeCycleListener(
     * org.apache.cxf.buslifecycle.BusLifeCycleListener)
     */
    public void registerLifeCycleListener(BusLifeCycleListener listener) {
        listeners.add(listener);
        
    }

    /* (non-Javadoc)
     * @see org.apache.cxf.buslifecycle.BusLifeCycleManager#unregisterLifeCycleListener(
     * org.apache.cxf.buslifecycle.BusLifeCycleListener)
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
