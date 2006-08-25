package org.objectweb.celtix.event;

import java.util.EventObject;

import javax.xml.namespace.QName;


/**
 * Base class for all the Celtix Events.
 */
public class Event extends EventObject {

    /*
    public static final String BUS_EVENT = "org.objectweb.celtix.bus.event";
    public static final String COMPONENT_CREATED_EVENT = "COMPONENT_CREATED_EVENT";
    public static final String COMPONENT_REMOVED_EVENT = "COMPONENT_REMOVED_EVENT";
    */
    
    private QName eventId;

    /**
     * Constructs a <code>Event</code> with the event source and a unique event id.
     * This id is used to identify the event type.
     * @param source The <code>Object</code> representing the event information.
     * @param id the QName identifying the event type
     */
    public Event(Object source, QName id) {
        super(source);
        eventId = id;
    }

    /**
     * Returns the unique event id for this particular bus event.
     * @return String The event id.
     */
    public QName getID() {
        return eventId;
    }
}
