package org.apache.cxf.event;

public interface EventProcessor {
    
    /**
     * Sends an event to the processor.
     * @param e the event
     */
    void sendEvent(Event e);
    
    /**
     * Registers an event listener with this event processor.
     * @param listener the event listener
     */
    void addEventListener(EventListener listener);
    
    /**
     * Registers an event listener with this event processor. The listener will
     * only be notified when the event passes through the specified filter.
     * @param listener the event listener
     * @param filter the event filter
     */
    void addEventListener(EventListener listener, EventFilter filter);
    

    /**
     * Unregisters an event listener.
     * @param listener the event listener
     */
    void removeEventListener(EventListener listener);
}
