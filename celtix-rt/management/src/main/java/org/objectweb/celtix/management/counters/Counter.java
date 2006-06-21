package org.objectweb.celtix.management.counters;

/** class for the performance couter */
public class Counter {
    
    private String discription;
    private int value;
    private float rate;
    private Boolean runFlag;
    
    Counter(String disc) {
        discription = disc;
        runFlag = false;
    }
    
    public void reset() {
        value = 0;
        runFlag = true;
    }
    
    public int add(int i) {
        value = value + i;
        return value;
    }
    
    public final void increase() {
        if (runFlag) {
            value++;
        }
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
    
    public void stop() {
        value = 0;
        runFlag = false;
    }
    
    void setRate(float r) {
        if (rate < 1 && rate > 0) {
            rate = r;
        }
        // else do nothing           
    }
    
    
}
