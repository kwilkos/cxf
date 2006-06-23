package org.objectweb.celtix.ws.rm;

import java.util.Collection;

import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.workqueue.WorkQueue;

public interface RetransmissionQueue {
    
    
    String DEFAULT_BASE_RETRANSMISSION_INTERVAL = "3000";
    String DEFAULT_EXPONENTIAL_BACKOFF = "2";
    
    /**
     * @param seq the sequence under consideration
     * @return the number of unacknowledged messages for that sequence
     */
    int countUnacknowledged(SourceSequence seq);
    
    /**
     * @return true if there are no unacknowledged messages in the queue
     */
    boolean isEmpty();
   
    /**
     * Accepts a new context for posible future retransmission.
     * 
     * @param ctx the message context.
     */
    void addUnacknowledged(ObjectMessageContext context);
    
    /**
     * Purge all candidates for the given sequence that have been acknowledged.
     * 
     * @param seq the sequence object.
     */
    void purgeAcknowledged(SourceSequence seq);
    
    /**
     * Initiate resends.
     * 
     * @param queue the work queue providing async execution
     */
    void start(WorkQueue wq);
    
    /**
     * Stops retransmission queue.
     */
    void stop();
    
    /**
     * Populates the retransmission queue with messages recovered from
     * persistent store.
     */
    void populate(Collection<SourceSequence> sss);
    
    
}
