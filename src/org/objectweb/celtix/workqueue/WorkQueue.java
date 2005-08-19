package org.objectweb.celtix.workqueue;

public interface WorkQueue {

    long getMaxSize();
    
    long getSize();
    
    boolean isEmpty();
    
    boolean isFull();
      
    boolean enqueue(WorkItem work, long timeout);
    
    boolean enqueueImmediate(WorkItem work);
    
    void flush();
    
    boolean ownsCurrentThread();
}
