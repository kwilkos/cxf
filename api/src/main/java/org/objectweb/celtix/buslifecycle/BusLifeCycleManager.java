package org.objectweb.celtix.buslifecycle;

/**
 * The manager interface for registering <code>BusLifeCycleListener</code>s.
 *
 * A class that implements the BusLifeCycleListener interface can be
 * registered or unregistered to receive notification of <code>Bus</code>
 * lifecycle events.
 */
public interface BusLifeCycleManager {

    /**
     * Register a listener to receive <code>Bus</code> lifecycle notification.
     *
     * @param listener The <code>BusLifeCycleListener</code> that will
     * receive the events.
     */
    void registerLifeCycleListener(BusLifeCycleListener listener);

    /**
     * Unregister a listener so that it will no longer receive <code>Bus</code>
     * lifecycle events.
     *
     * @param listener The <code>BusLifeCycleListener</code> to unregister.
     */
    void unregisterLifeCycleListener(BusLifeCycleListener listener);
}
