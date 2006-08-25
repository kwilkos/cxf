package org.objectweb.celtix.bus.busimpl;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusEventCache;


public class BusEventCacheImpl implements BusEventCache {

    public static final String MAX_BUS_EVENTS = "bus:events:cache:size";   
    private static final Logger LOG = Logger.getLogger(BusEventCacheImpl.class.getName());

    private List<BusEvent> events;
    
    private int maxEventsCache = 100;
    

    public BusEventCacheImpl(Bus b) {
        
        initCache();
    }

    private void initCache() {
        /* TODO read the EventCache configuration */
        events = new ArrayList<BusEvent>(maxEventsCache);
    }


    public synchronized void addEvent(BusEvent e) {
        if (events.size() == maxEventsCache) {
            BusEvent event = (BusEvent)events.get(0);
            LOG.log(Level.FINE, "Event "
                                + event.getID()
                                + " is removed because the event cache is full."
                                + " Maximum number of events stored : "
                                + maxEventsCache);
                                          
            events.remove(event);
        }

        events.add(e);
    }


    public synchronized List<BusEvent> flushEvents() {
        List<BusEvent> result = new ArrayList<BusEvent>(events.size());
        result.addAll(events);
        events.clear();
        return result;
    }


    public synchronized List<BusEvent> flushEvents(String eventID) {
        List<BusEvent> result = new ArrayList<BusEvent>();
        for (Iterator<BusEvent> i = events.iterator(); i.hasNext();) {
            BusEvent event = i.next();
            if (eventID.equals(event.getID())) {
                result.add(event);
                i.remove();
            }                
        }   

        
        return result;
    }

    public synchronized List<BusEvent> flushEvents(Class<?> eventClass) {
        List<BusEvent> result = new ArrayList<BusEvent>();
        for (Iterator<BusEvent> i = events.iterator(); i.hasNext();) {
            BusEvent event = i.next();
            if (eventClass.isAssignableFrom(event.getClass())) {
                result.add(event);
                i.remove();
            }
        }

        
        return result;
    }


    public List<BusEvent> getEvents() {
        return events;
    }


    public List<BusEvent> getEvents(String eventID) {
        List<BusEvent> result = new ArrayList<BusEvent>();

        for (int i = 0; i < events.size(); i++) {
            BusEvent event = (BusEvent)events.get(i);

            if (eventID.equals(event.getID())) {
                result.add(event);
            }
        }

        return result;
    }


    public void setCacheSize(int size) {
        maxEventsCache = size;
        ((ArrayList)events).ensureCapacity(maxEventsCache);
    }
}
