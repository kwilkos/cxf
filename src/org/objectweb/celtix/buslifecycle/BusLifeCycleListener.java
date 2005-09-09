package org.objectweb.celtix.buslifecycle;

/**
 * The listener interface for receiving notification of <code>Bus</code>
 * lifecycle events.
 *
 * A class that implements this interface will have its methods called
 * when the associated lifecycle events occur.  An implementing class
 * must register itself with the Bus through the
 * <code>BusLifeCycleManager</code> interface.
 */
public interface BusLifeCycleListener {

    /**
     * Invoked when the <code>Bus</code> has been initialized.
     *
     */
    void initComplete();
    
    /**
     * Invoked before the <code>Bus</code> is shutdown.
     *
     */
    void preShutdown();

    /**
     * Invoked after the <code>Bus</code> is shutdown.
     *
     */
    void postShutdown();
}
