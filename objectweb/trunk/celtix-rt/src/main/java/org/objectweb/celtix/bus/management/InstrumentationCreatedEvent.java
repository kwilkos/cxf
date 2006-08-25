package org.objectweb.celtix.bus.management;

import org.objectweb.celtix.management.Instrumentation;



/**
 * A Instrumentation Created Event that indicates that 
 * the Instumentation relat ManagedComponent needs to be registered
 * as an MBean with the MBean server.
 */
public class InstrumentationCreatedEvent extends InstrumentationEvent {
   
   
    /**
     * Constructs a <code>InstrumenationCreatedEvent</code> object.
     *
     * @param source The instrumentation object that to register.
     */
    public InstrumentationCreatedEvent(Instrumentation source) {
        super(source);
    }
}
