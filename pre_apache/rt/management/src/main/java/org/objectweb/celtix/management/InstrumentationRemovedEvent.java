package org.objectweb.celtix.management;


/**
 * A <code>InstrumenationEvent</code> indicating that the specified
 * <code>Instrumentation</code> related managed component needs to be deregistered from the mbean server.
 */
public class InstrumentationRemovedEvent extends InstrumentationEvent {
    
    /**
     * Constructs a <code>Instrumentation</code> object.
     *
     * @param source The Instrumentation object associated with this event.
     */
    public InstrumentationRemovedEvent(Instrumentation source) {
        super(source);
    }
}
