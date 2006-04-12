package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusEventFilter;

public class ConfigurationEventFilter implements BusEventFilter {
    public boolean isEventEnabled(BusEvent e) {
        boolean result = false;
        if (e.getID().equals(ConfigurationEvent.RECONFIGURED)) {
            result = true;
        }
        return result;
    }

}
