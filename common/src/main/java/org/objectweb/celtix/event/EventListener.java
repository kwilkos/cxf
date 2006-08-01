package org.objectweb.celtix.event;



/**
 * Should be implemented by an object that wants to receive events.
 */
interface EventListener extends java.util.EventListener {
    /**
     * Invoked when an event occurs.
     * The implementation of this method should return as soon as possible,
     * to avoid blocking its event processor.
     * @param e The <code>Event</code> to be processed.
     */
    void processEvent(Event e);
}
