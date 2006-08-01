package org.objectweb.celtix.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class EventProcessorImpl implements EventProcessor {      
    protected List<EventListenerInfo> listenerList;
    protected EventCache cache;
    
    public EventProcessorImpl() {
        this(null);
    }
    
    public EventProcessorImpl(EventCache eventCache) {
        listenerList = new ArrayList<EventListenerInfo>();
        cache = eventCache == null ? new EventCacheImpl() : eventCache;
    }
    
    public void addEventListener(EventListener l) {
        addEventListener(l, null);
    }

    public void addEventListener(EventListener l, EventFilter filter) {
        if (l == null) {
            return;
        }

        synchronized (listenerList) {
            listenerList.add(new EventListenerInfo(l, filter));
        }
    }


    public void removeEventListener(EventListener l) {
        EventListenerInfo li;
        synchronized (listenerList) {            
            for (Iterator<EventListenerInfo> i = listenerList.iterator(); i.hasNext();) {
                li = i.next();
                if (li.listener == l) {
                    i.remove();
                    return;
                }
            }           
        }
    }


    public void sendEvent(Event e) {
        if (e == null) {
            return;
        }

        EventListenerInfo li;
        boolean eventProcessed = false;

        synchronized (listenerList) {
            for (int i = 0; i < listenerList.size(); i++) {
                li = (EventListenerInfo)listenerList.get(i);

                if ((li.filter == null) || (li.filter.isEventEnabled(e))) {
                    eventProcessed = true;
                    li.listener.processEvent(e);
                }
            }
        }

        if (!eventProcessed) {
            cache.addEvent(e);
        }
    }

    class EventListenerInfo {
        EventListener listener;
        EventFilter filter;

        public EventListenerInfo(EventListener l, EventFilter f) {
            listener = l;
            filter = f;
        }
    }
}
