package org.apache.cxf.management.counters;


public class TransportServerCounters {
    private static final String[] COUNTER_NAMES = {"RequestsTotal",
                                                   "RequestsOneWay",
                                                   "TotalError"};
    private Counter[] counters;
    
    
    private String owner;
    
    public TransportServerCounters(String o) {
        owner = o;
        counters = new Counter[COUNTER_NAMES.length];
        initCounters();
    }
    public String getOwner() {
        return owner;
    }
           
    public Counter getRequestTotal() {
        return counters[0];
    }
    
    public Counter getRequestOneWay() {
        return counters[1];        
    }
    
    public Counter getTotalError() {
        return counters[2];
    
    }
    
    private void initCounters() {
        for (int i = 0; i < COUNTER_NAMES.length; i++) {
            Counter c = new Counter(COUNTER_NAMES[i]);
            counters[i] = c;
        }   
    }
    
    public void resetCounters() {
        for (int i = 0; i < counters.length; i++) {
            counters[i].reset();
        }
    } 
    
    public void stopCounters() {
        for (int i = 0; i < counters.length; i++) {
            counters[i].stop();
        }
    }
    
    
}
