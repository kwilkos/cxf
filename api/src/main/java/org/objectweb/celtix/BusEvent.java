package org.objectweb.celtix;

import java.util.*;


/**
 * Base class for all the Bus Events.
 */
public class BusEvent extends EventObject {
    /**
     * Constant used for generic Bus Event ID.
     */
    public static final String BUS_EVENT = "org.objectweb.celtix.bus.event";
    public static final String COMPONENT_CREATED_EVENT = "COMPONENT_CREATED_EVENT";
    public static final String COMPONENT_REMOVED_EVENT = "COMPONENT_REMOVED_EVENT";
    
    private String eventId;

    /**
     * Constructs a <code>BusEvent</code> with the event source and a unique event id.
     * This id is used for querying for the events.
     * @param source The <code>Object</code> representing the event information.
     * @param id A string containing the event id.
     */
    public BusEvent(Object source, String id) {
        super(source);
        eventId = id;
    }

    /**
     * Returns the unique event id for this particular bus event.
     * @return String The event id.
     */
    public String getID() {
        return eventId;
    }
}
