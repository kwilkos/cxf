package org.objectweb.celtix.bus.busimpl;

import org.objectweb.celtix.BusEvent;

/**
 * A ManagedComponent Event that indicates that
 * the ManagedComponent needs to be registered
 * as an MBean with the MBean server.
 */
public class ComponentCreatedEvent extends BusEvent {
    public static final String COMPONENT_CREATED_EVENT = "COMPONENT_CREATED_EVENT";
    /**
     * Constructs a <code>ManagedComponentCreatedEvent</code> object.
     *
     * @param source The managed component object that to register.
     */
    public ComponentCreatedEvent(Object source) {
        super(source, COMPONENT_CREATED_EVENT);
    }
}
