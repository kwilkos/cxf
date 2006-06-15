package org.objectweb.celtix.bus.management;

import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusEventFilter;

public class ComponentEventFilter implements BusEventFilter {
    public boolean isEventEnabled(BusEvent e) {
        boolean result = false;
        if (e.getID().equals(BusEvent.COMPONENT_CREATED_EVENT)) {
            result = true;
        } else if (e.getID().equals(BusEvent.COMPONENT_REMOVED_EVENT)) {
            result = true;
        }
        return result;
    }

}
