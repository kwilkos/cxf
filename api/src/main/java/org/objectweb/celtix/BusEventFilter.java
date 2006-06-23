package org.objectweb.celtix;


/**
 * Interface to be implemented by any class acting as a bus event filter.
 * It allows a registered bus listener to filter the events of interest.
 */
public interface BusEventFilter {
    /**
     * Invoked before sending the specified event to the listener.
     * @param e The bus event to be sent
     * @return boolean If <code>true</code>, the event is to be sent to the listener,
     * otherwise <code>false</code>.
     */
    boolean isEventEnabled(BusEvent e);
}
