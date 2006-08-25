package org.apache.cxf.transport;

/**
 * Allows Observers to register for notification on incoming messages.
 */
public interface Observable {
    /**
     * Register a message observer for incoming messages.
     * 
     * @param observer the observer to notify on receipt of incoming
     */
    void setMessageObserver(MessageObserver observer);
}
