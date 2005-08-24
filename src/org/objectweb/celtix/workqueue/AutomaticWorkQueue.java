package org.objectweb.celtix.workqueue;

public interface AutomaticWorkQueue extends WorkQueue {
    /**
     * Initiates an orderly shutdown. 
     * If <code>processRemainingWorkItems</code>
     * is true, waits for all active items to finish execution before returning, otherwise returns 
     * immediately after removing all non active items from the queue.
     * 
     * @param processRemainingWorkItems
     */
    void shutdown(boolean processRemainingWorkItems);
}
