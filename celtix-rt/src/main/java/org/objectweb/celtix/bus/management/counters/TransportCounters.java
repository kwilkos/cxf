package org.objectweb.celtix.bus.management.counters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class TransportCounters {
    private static final String[] COUNTER_NAMES = {"RequestsTotal",
                                                   "RequestsOneWay",
                                                   "TotalError"};
    private List<Counter> counters;
    
    
    private String owner;
    
    public TransportCounters(String o) {
        owner = o;
        counters = new ArrayList<Counter>();
        initCounters();
    }
    public String getOwner() {
        return owner;
    }
           
    public Counter getRequestTotal() {
        return counters.get(0);
    }
    
    public Counter getRequestOneWay() {
        return counters.get(1);        
    }
    
    public Counter getTotalError() {
        return counters.get(2);
    
    }
    
    private void initCounters() {
        for (int i = 0; i < COUNTER_NAMES.length; i++) {
            Counter c = new Counter(COUNTER_NAMES[i]);
            counters.add(c);
        }    
    }
    
    void resetCounters() {
        for (Iterator<Counter> i = counters.iterator(); i.hasNext();) {
            Counter c = i.next();
            c.reset();
        }
    }    
    
}
