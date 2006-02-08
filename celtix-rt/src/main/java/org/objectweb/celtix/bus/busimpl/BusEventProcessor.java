package org.objectweb.celtix.bus.busimpl;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusEventCache;
import org.objectweb.celtix.BusEventFilter;
import org.objectweb.celtix.BusEventListener;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;


public class BusEventProcessor {
    private static final Logger LOG = LogUtils.getL7dLogger(BusEventProcessor.class);    
    protected Bus theBus;    
    protected List<BusEventListenerInfo> listenerList;
    protected BusEventCache cache;
    

    public BusEventProcessor(Bus bus, BusEventCache eventCache) {
        theBus = bus;
        listenerList = new ArrayList<BusEventListenerInfo>();
        cache = eventCache;
    }

    public void addListener(BusEventListener l, BusEventFilter filter) throws BusException {
        if (l == null) {
            throw new BusException(new Message("Listener can't be null", LOG));
        }

        synchronized (listenerList) {
            listenerList.add(new BusEventListenerInfo(l, filter));
        }
    }


    public void removeListener(BusEventListener l) throws BusException {
        boolean found = false;
        BusEventListenerInfo li;
        synchronized (listenerList) {            
            for (Iterator<BusEventListenerInfo> i = listenerList.iterator(); i.hasNext();) {
                li = i.next();
                if (li.listener == l) {
                    i.remove();
                    found = true;
                }
            }           
        }

        if (!found) {
            throw new BusException(
                      new Message("Error while removing listener. Specified listener is not found.",
                                  LOG));
        }
    }


    public void processEvent(BusEvent e) {
        if (e == null) {
            return;
        }

        BusEventListenerInfo li;
        boolean eventProcessed = false;

        synchronized (listenerList) {
            for (int i = 0; i < listenerList.size(); i++) {
                li = (BusEventListenerInfo) listenerList.get(i);

                if ((li.filter == null) || (li.filter.isEventEnabled(e))) {
                    eventProcessed = true;

                    try {
                        li.listener.processEvent(e);
                    } catch (BusException ex) {
                        //NOTE now just log the exception and not throw the exception to bus
                        LOG.log(Level.WARNING, "PROCESS_EVENT_FAILURE_MSG", 
                                new Object[] {li.getClass().getName(), e.getID(), ex}); 
                    }
                }
            }
        }

        if (!eventProcessed) {
            cache.addEvent(e);
        }
    }

    class BusEventListenerInfo {
        BusEventListener listener;
        BusEventFilter filter;

        public BusEventListenerInfo(BusEventListener l, BusEventFilter f) {
            listener = l;
            filter = f;
        }
    }
}
