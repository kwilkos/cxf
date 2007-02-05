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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.cxf.Bus;


public class EventProcessorImpl implements EventProcessor {      
    protected List<EventListenerInfo> listenerList;
    protected EventCache cache;
    private Bus bus;
    
    public EventProcessorImpl() {
        this(null);
    }
    
    public EventProcessorImpl(EventCache eventCache) {
        listenerList = new ArrayList<EventListenerInfo>();
        cache = eventCache == null ? new EventCacheImpl() : eventCache;
    }
    
    @Resource(name = "bus")
    public void setBus(Bus bus) {        
        this.bus = bus;
    }
    
    @PostConstruct
    public void register() {
        if (null != bus) {
            bus.setExtension(this, EventProcessor.class);
        }
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
