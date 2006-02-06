package org.objectweb.celtix;

import java.util.*;


/**
 * Should be implemented by an object that wants to receive bus events.
 */
public interface BusEventListener extends EventListener {
    /**
     * Invoked when a bus event occurs.
     * The implementation of this method should return as soon as possible,
     * to avoid blocking its event processor.
     * @param e The <code>BusEvent</code> to be processed.
     * @throws BusException If there is an error processing event.
     */
    void processEvent(BusEvent e) throws BusException;
}
