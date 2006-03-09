package org.objectweb.celtix.bus.management.counters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransportClientCounters {
    private static final String[] COUNTER_NAMES = {"Invoke",
                                                   "InvokeOneWay",
                                                   "InvokeAsync",
                                                   "InvokeError"};
    private List<Counter> counters;
    
    
    private String owner;
    
    public TransportClientCounters(String o) {
        owner = o;
        counters = new ArrayList<Counter>();
        initCounters();
    }
    public String getOwner() {
        return owner;
    }
           
    public Counter getInvoke() {
        return counters.get(0);
    }
    
    public Counter getInvokeOneWay() {
        return counters.get(1);        
    }
    
    public Counter getInvokeAsync() {
        return counters.get(2);
    
    }
    
    public Counter getInvokeError() {
        return counters.get(3);
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
