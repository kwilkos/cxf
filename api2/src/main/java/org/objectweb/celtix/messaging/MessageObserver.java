package org.objectweb.celtix.messaging;

/**
 * Observer for incoming messages.
 */
public interface MessageObserver {

    /**
     * Called for an incoming message.
     * 
     * @param message
     */
    void onMessage(InMessage message);
}
