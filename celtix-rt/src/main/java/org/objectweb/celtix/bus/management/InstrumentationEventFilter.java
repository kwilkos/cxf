package org.objectweb.celtix.bus.management;

import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusEventFilter;

/*
 * The event filter used to filter the management events
 * that are processed by the management listener.
 */
public class InstrumentationEventFilter implements BusEventFilter {  
    
    
    /**
     * Invoked before sending the specified event to the listener.
     *
     * @param e The <code>BusEvent</code> to be sent.
     *
     * @return boolean True if the event should be sent to the listener,
     * otherwise false.
     */
    public boolean isEventEnabled(BusEvent e) {
        boolean result = false;

        if (e.getID().equals(InstrumentationEvent.MANAGED_BUS_EVENT)) {
            result = true;
        }

        return result;
    }
    
    /**
     * Get InstrumentationCreatedEvent information from the Management Event
     *  
     * @param e The <code>BusEvent</code> to be sent.
     * 
     * @return boolean True if the event is the InstrumentationCreatedEvent
     */
    public boolean isCreateEvent(BusEvent e) {
        boolean result = false;
        if (e.getClass().equals(InstrumentationCreatedEvent.class)) {
            result = true;
        }
        return result;            
    }
    
    /**
     * Get InstrumentationRemovedEvent information from the Management Event
     *  
     * @param e The <code>BusEvent</code> to be sent.
     * 
     * @return boolean True if the event is the InstrumentationRemovedEvent
     */
    public boolean isRemovedEvent(BusEvent e) {
        boolean result = false;
        if (e.getClass().equals(InstrumentationRemovedEvent.class)) {
            result = true;
        }
        return result;        
    }
  
    
}
