/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.event;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

public class EventCacheImpl implements EventCache {

    public static final String MAX_EVENTS = "bus:events:cache:size";   
    private static final Logger LOG = Logger.getLogger(EventCacheImpl.class.getName());

    private List<Event> events;
    
    private int maxEventsCache = 100;
    

    public EventCacheImpl() {
        
        initCache();
    }

    private void initCache() {
        /* TODO read the EventCache configuration */
        events = new ArrayList<Event>(maxEventsCache);
    }


    public synchronized void addEvent(Event e) {
        if (events.size() == maxEventsCache) {
            Event event = (Event)events.get(0);
            LOG.log(Level.FINE, "Event "
                                + event.getID()
                                + " is removed because the event cache is full."
                                + " Maximum number of events stored : "
                                + maxEventsCache);
                                          
            events.remove(event);
        }

        events.add(e);
    }


    public synchronized List<Event> flushEvents() {
        List<Event> result = new ArrayList<Event>(events.size());
        result.addAll(events);
        events.clear();
        return result;
    }

    public synchronized List<Event> flushEvents(QName eventType) {
        List<Event> result = new ArrayList<Event>();
        for (Iterator<Event> i = events.iterator(); i.hasNext();) {
            Event event = i.next();
            if (eventType.equals(event.getID())) {
                result.add(event);
                i.remove();
            }                
        }      
        return result;
    }
    
    public synchronized List<Event> flushEvents(String namespaceURI) {
        List<Event> result = new ArrayList<Event>();
        for (Iterator<Event> i = events.iterator(); i.hasNext();) {
            Event event = i.next();
            if (namespaceURI.equals(event.getID().getNamespaceURI())) {
                result.add(event);
                i.remove();
            }                
        }      
        return result;
    }
    
    public List<Event> getEvents() {
        return events;
    }

    public List<Event> getEvents(QName eventType) {
        List<Event> result = new ArrayList<Event>();

        for (int i = 0; i < events.size(); i++) {
            Event event = (Event)events.get(i);

            if (eventType.equals(event.getID())) {
                result.add(event);
            }
        }
        return result;
    }
    
    public List<Event> getEvents(String namespaceURI) {
        List<Event> result = new ArrayList<Event>();

        for (int i = 0; i < events.size(); i++) {
            Event event = (Event)events.get(i);

            if (namespaceURI.equals(event.getID().getNamespaceURI())) {
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
