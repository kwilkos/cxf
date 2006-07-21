package org.objectweb.celtix.messaging;

import java.io.InputStream;

/**
 * Represents an inbound message.
 */
public interface InMessage {

    /**
     * @return the associated Destination (may be anonymous)
     */
    Destination getDestination();
    
    /**
     * @return an input stream containing the message payload
     */
    InputStream getInputStream();
}
