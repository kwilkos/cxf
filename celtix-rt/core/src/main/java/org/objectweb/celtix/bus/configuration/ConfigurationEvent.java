package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.BusEvent;
/**
 * A <code>ComponentEvent</code> indicating that the specified
 * <code>ManagedComponent</code> needs to be deregistered from the mbean server.
 */
public class ConfigurationEvent extends BusEvent {
    public static final String RECONFIGURED = "RECONFIGURED";
    /**
     * Constructs a <code>ConfigurationEvent</code> object.
     *
     * @param source The managed component object associated with this event.
     * @param id The event id.
     */
    public ConfigurationEvent(Object source, String id) {
        super(source, id);
    }
}
