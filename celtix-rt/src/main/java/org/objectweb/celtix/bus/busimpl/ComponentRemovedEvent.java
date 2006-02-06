package org.objectweb.celtix.bus.busimpl;

import org.objectweb.celtix.BusEvent;
/**
 * A <code>ComponentEvent</code> indicating that the specified
 * <code>ManagedComponent</code> needs to be deregistered from the mbean server.
 */
public class ComponentRemovedEvent extends BusEvent {
    public static final String COMPONENT_REMOVED_EVENT = "COMPONENT_REMOVED_EVENT";
    /**
     * Constructs a <code>ManagedComponentRemovedEvent</code> object.
     *
     * @param source The managed component object associated with this event.
     */
    public ComponentRemovedEvent(Object source) {
        super(source, COMPONENT_REMOVED_EVENT);
    }
}
