package org.objectweb.celtix.bus.management.counters;

/** class for the performance couter */
public class Counter {
    
    private String discription;
    private int value;
    private float rate;
    
    Counter(String disc) {
        discription = disc;
    }
    
    public void reset() {
        value = 0;
    }
    
    public int add(int i) {
        value = value + i;
        return value;
    }
    
    public void increase() {
        value++;
    }
    
    public String getDiscription() {
        return discription;
    }
    
    float getRate() {
        return rate;
    }
    
    public int getValue() {
        return value;
    }
    
    void setRate(float r) {
        if (rate < 1 && rate > 0) {
            rate = r;
        }
        // else do nothing           
    }
    
}
