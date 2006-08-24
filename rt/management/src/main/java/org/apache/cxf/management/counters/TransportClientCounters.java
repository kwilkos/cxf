package org.apache.cxf.management.counters;

public class TransportClientCounters {
    private static final String[] COUNTER_NAMES = {"Invoke",
                                                   "InvokeOneWay",
                                                   "InvokeAsync",
                                                   "InvokeError"};
    private Counter[] counters;
    
    
    private String owner;
    
    public TransportClientCounters(String o) {
        owner = o;
        counters = new Counter[COUNTER_NAMES.length];
        initCounters();
    }
    public String getOwner() {
        return owner;
    }
           
    public Counter getInvoke() {
        return counters[0];
    }
    
    public Counter getInvokeOneWay() {
        return counters[1];        
    }
    
    public Counter getInvokeAsync() {
        return counters[2];
    
    }
    
    public Counter getInvokeError() {
        return counters[3];
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
