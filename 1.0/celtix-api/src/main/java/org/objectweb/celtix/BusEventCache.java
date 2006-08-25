package org.objectweb.celtix;

import java.util.*;


/**
 * Caches all bus events that do not have a listener associated with them.
 * The bus events will be stored until the cache limit is reached.
 * After reaching the cache size, events will be discarded using first in,
 * first out semantics.
 */
public interface BusEventCache {
    /**
     * Add the <code>BusEvent</code> to the cache.
     * If the maximum size of the cache is reached, the first <code>BusEvent</code>
     * added will be removed from the cache(FIFO)
     * @param e The <code>BusEvent</code> to be added to the cache.
     */
    void addEvent(BusEvent e);


    /**
     * Flushes the cache of all the <code>BusEvent</code>.
     * @return List Containing all the <code>BusEvent</code>s cached.
     */
    List<BusEvent> flushEvents();


    /**
     * Flushes the <code>BusEvent</code> from the cache matching the event id.
     * @param eventID The unique event id that identifies the <code>BusEvent</code>.
     * @return List Containing all <code>BusEvent</code>s matching the event id.
     */
    List<BusEvent> flushEvents(String eventID);


    /**
     * Flushes the <code>BusEvent</code> from the cache matching the event class.
     * @param eventClass The class of the event that identifies the <code>BusEvent</code>.
     * @return List Containing all <code>BusEvent</code>s matching the event class.
     */
    List<BusEvent> flushEvents(Class<?> eventClass);

    /**
     * Returns all the bus events. This method doesn't remove the
     * events from the cache.
     * @return List Containing all bus events stored in the cache.
     */
    List<BusEvent> getEvents();


    /**
     * Returns all the bus events matching the event id. This method doesn't
     * remove the events from the cache.
     * @param eventID Unique bus event id that identifies the <code>BusEvent</code>.
     * @return List Containing all of <code>BusEvent</code>s matching the event id.
     */
    List<BusEvent> getEvents(String eventID);


    /**
     * Sets the cache size. This method can be used to dynamically change the
     * cache size from the configured size.
     * @param size Indicates the new size of the cache.
     */
    void setCacheSize(int size);
}
