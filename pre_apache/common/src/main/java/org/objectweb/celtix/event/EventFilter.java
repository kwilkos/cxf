package org.objectweb.celtix.event;


/**
 * Interface to be implemented by any class acting as an event filter.
 * It allows a event listener to filter the events of interest.
 */

interface EventFilter {
    /**
     * Invoked before sending the specified event to the listener.
     * @param e The event to be sent
     * @return boolean If <code>true</code>, the event is to be sent to the listener,
     * otherwise <code>false</code>.
     */
    boolean isEventEnabled(Event e);
}
