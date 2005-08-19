package org.objectweb.celtix.workqueue;

public interface AutomaticWorkQueue extends WorkQueue {
    
    int getHighWaterMark();
    void setHighWaterMark(int hwm);
    
    int getLowWaterMark();
    void setLowWaterMark(int lwm);
    
    int getThreadCount();
    int getWorkingThreadCount();

    void shutdown(boolean processRemainingWorkItems);
}
