package org.apache.cxf.messaging;

import org.apache.cxf.message.Message;

/**
 * Observer for incoming messages.
 */
public interface MessageObserver {

    /**
     * Called for an incoming message, i.e. where the content format(s)
     * is/are source(s).
     * 
     * @param message
     */
    void onMessage(Message message);
}
