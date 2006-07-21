package org.objectweb.celtix.messaging;

import java.io.OutputStream;

/**
 * Represents an inbound message.
 */
public interface OutMessage {

    /**
     * @return the associated Conduit (may be anonymous)
     */
    Conduit getConduit();

    /**
     * @return an output stream used to consume the message payload
     */
    OutputStream getOutputStream();
}
