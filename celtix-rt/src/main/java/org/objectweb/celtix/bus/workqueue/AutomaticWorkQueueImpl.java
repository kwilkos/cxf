package org.objectweb.celtix.bus.workqueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.workqueue.AutomaticWorkQueue;

public class AutomaticWorkQueueImpl extends ThreadPoolExecutor implements AutomaticWorkQueue {

    static final int DEFAULT_MAX_QUEUE_SIZE = 128;
    private static final Logger LOG =
        LogUtils.getL7dLogger(AutomaticWorkQueueImpl.class);
    
    int maxQueueSize;

    AutomaticWorkQueueImpl(int mqs, int initialThreads, int highWaterMark, int lowWaterMark,
                           long dequeueTimeout) {
        
        super(-1 == lowWaterMark ? Integer.MAX_VALUE : lowWaterMark, 
            -1 == highWaterMark ? Integer.MAX_VALUE : highWaterMark,
                TimeUnit.MILLISECONDS.toMillis(dequeueTimeout), TimeUnit.MILLISECONDS, 
                mqs == -1 ? new ArrayBlockingQueue<Runnable>(DEFAULT_MAX_QUEUE_SIZE)
                    : new ArrayBlockingQueue<Runnable>(mqs));
        
        maxQueueSize = mqs == -1 ? DEFAULT_MAX_QUEUE_SIZE : mqs;
        lowWaterMark = -1 == lowWaterMark ? Integer.MAX_VALUE : lowWaterMark;
        highWaterMark = -1 == highWaterMark ? Integer.MAX_VALUE : highWaterMark;
                
        StringBuffer buf = new StringBuffer();
        buf.append("Constructing automatic work queue with:\n");
        buf.append("max queue size: " + maxQueueSize + "\n");
        buf.append("initialThreads: " + initialThreads + "\n");
        buf.append("lowWaterMark: " + lowWaterMark + "\n");
        buf.append("highWaterMark: " + highWaterMark + "\n");
        LOG.fine(buf.toString());

        if (initialThreads > highWaterMark) {
            initialThreads = highWaterMark;
        }

        // as we cannot prestart more core than corePoolSize initial threads, we temporarily
        // change the corePoolSize to the number of initial threads
        // this is important as otherwise these threads will be created only when the queue has filled up, 
        // potentially causing problems with starting up under heavy load
        if (initialThreads < Integer.MAX_VALUE && initialThreads > 0) {
            setCorePoolSize(initialThreads);
            int started = prestartAllCoreThreads();
            if (started < initialThreads) {
                LOG.log(Level.WARNING, "THREAD_START_FAILURE_MSG", new Object[] {started, initialThreads});
            }
            setCorePoolSize(lowWaterMark);
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        buf.append(" [queue size: ");
        buf.append(getSize());
        buf.append(", max size: ");
        buf.append(maxQueueSize);
        buf.append(", threads: ");
        buf.append(getPoolSize());
        buf.append(", active threads: ");
        buf.append(getActiveCount());
        buf.append(", low water mark: ");
        buf.append(getLowWaterMark());
        buf.append(", high water mark: ");
        buf.append(getHighWaterMark());
        buf.append("]");
        return buf.toString();
    }
    
    // WorkQueue interface
     
    /* (non-Javadoc)
     * @see org.objectweb.celtix.workqueue.WorkQueue#execute(java.lang.Runnable, long)
     */
    public void execute(Runnable work, long timeout) {
        try {
            execute(work);
        } catch (RejectedExecutionException ree) {
            try {
                getQueue().offer(work, timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                throw new RejectedExecutionException(ie);
            }
        }    
    }
    
   
    
    // AutomaticWorkQueue interface
    
    public void shutdown(boolean processRemainingWorkItems) {
        if (!processRemainingWorkItems) {
            getQueue().clear();
        }
        shutdown();    
    }

    /**
     * Gets the maximum size (capacity) of the backing queue.
     * @return the maximum size (capacity) of the backing queue.
     */
    long getMaxSize() {
        return maxQueueSize;
    }

    /**
     * Gets the current size of the backing queue.
     * @return the current size of the backing queue.
     */
    public long getSize() {
        return getQueue().size();
    }


    public boolean isEmpty() {
        return getQueue().size() == 0;
    }

    boolean isFull() {
        return getQueue().remainingCapacity() == 0;
    }

    int getHighWaterMark() {
        int hwm = getMaximumPoolSize();
        return hwm == Integer.MAX_VALUE ? -1 : hwm;
    }

    int getLowWaterMark() {
        int lwm = getCorePoolSize();
        return lwm == Integer.MAX_VALUE ? -1 : lwm;
    }

    void setHighWaterMark(int hwm) {
        setMaximumPoolSize(hwm < 0 ? Integer.MAX_VALUE : hwm);
    }

    void setLowWaterMark(int lwm) {
        setCorePoolSize(lwm < 0 ? 0 : lwm);
    }
}
