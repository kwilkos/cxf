package org.objectweb.celtix.bus.management;

import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusEventFilter;
import org.objectweb.celtix.bus.busimpl.ComponentCreatedEvent;
import org.objectweb.celtix.bus.busimpl.ComponentRemovedEvent;

public class ComponentEventFilter implements BusEventFilter {
    public boolean isEventEnabled(BusEvent e) {
        boolean result = false;
        if (e.getID().equals(ComponentCreatedEvent.COMPONENT_CREATED_EVENT)) {
            result = true;
        } else if (e.getID().equals(ComponentRemovedEvent.COMPONENT_REMOVED_EVENT)) {
            result = true;
        }
        return result;
    }

}
