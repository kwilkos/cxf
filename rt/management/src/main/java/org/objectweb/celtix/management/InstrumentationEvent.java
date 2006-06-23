package org.objectweb.celtix.management;

import org.objectweb.celtix.BusEvent;


/**
 * The <code>InstrumentationEvent</code> class, which is the base
 * class for all Instrumentation events.  
 */
public class InstrumentationEvent extends BusEvent {
    
    /**
     * Constant representing the Managed Bus Event ID.
     */
    public static final String MANAGED_BUS_EVENT = "org.objectweb.celtix.bus.managed.event";

        
    /**
     * Constructs a <code>InstrumentationEvent</code> object.
     * 
     * @param source The instrumentation object that originated this event.
     */
    public InstrumentationEvent(Instrumentation source) {
        super(source, MANAGED_BUS_EVENT);
    }
    
}
