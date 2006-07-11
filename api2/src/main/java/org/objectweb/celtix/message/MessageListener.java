package org.objectweb.celtix.message;

/**
 * Listens for a messages from a Channel.
 * @author Dan Diephouse
 */
public interface MessageListener {
    void onMessage(Message message);
}
