package org.objectweb.celtix.bus.management;

import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusEventListener;
import org.objectweb.celtix.BusException;



/**
 * An interface that should be implemented by an object
 * that processes <code>InstrumentationEvents</code>.
 */
public interface InstrumentationEventListener extends BusEventListener {
    /**
     * Invoked when a <code>InstrumentationEvent</code> is received by this listener.
     *
     * @param event The <code>BusEvent</code> to process.
     *
     * @throws BusException If there is an error while handling the event.
     */
    void processEvent(BusEvent event) throws BusException;
}
