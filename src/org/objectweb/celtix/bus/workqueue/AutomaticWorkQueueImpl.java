package org.objectweb.celtix.bus.workqueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.objectweb.celtix.workqueue.AutomaticWorkQueue;
import org.objectweb.celtix.workqueue.WorkItem;

public class AutomaticWorkQueueImpl implements AutomaticWorkQueue {

    static final int DEFAULT_MAX_QUEUE_SIZE = 128;
    private static Logger logger = Logger.getLogger(AutomaticWorkQueueImpl.class.getPackage().getName());
    
    int maxQueueSize;
    BlockingQueue<Runnable> queue;
    ThreadPoolExecutor executor;

    AutomaticWorkQueueImpl(int maxSize, int initialThreads, int highWaterMark, int lowWaterMark,
                           long dequeueTimeout) {
        StringBuffer buf = new StringBuffer();
        buf.append("Constructing automatic work queue with:\n");
        buf.append("max queue size: " + maxSize + "\n");
        buf.append("initialThreads: " + initialThreads + "\n");
        buf.append("lowWaterMark: " + lowWaterMark + "\n");
        buf.append("highWaterMark: " + highWaterMark + "\n");
        logger.info(buf.toString());

        maxQueueSize = maxSize;
        if (-1 == maxQueueSize) {
            /*
            queue = new LinkedBlockingQueue<Runnable>();
            logger.info("Constructed LinkedBlockingQueue");
            */
            maxQueueSize = DEFAULT_MAX_QUEUE_SIZE; 
        } 
        queue = new ArrayBlockingQueue<Runnable>(maxQueueSize, true);
        logger.info("Constructed ArrayBlockingQueue");


        int maximumPoolSize = highWaterMark;
        if (-1 == maximumPoolSize) {
            maximumPoolSize = Integer.MAX_VALUE;
        }
        int corePoolSize = lowWaterMark;
        if (-1 == corePoolSize) {
            corePoolSize = 0;
        }

        if (initialThreads > highWaterMark) {
            initialThreads = highWaterMark;
        }

        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, dequeueTimeout,
                                          TimeUnit.MILLISECONDS, queue);
        buf.setLength(0);
        buf.append("Constructed ThreadPoolExecutor with\n");
        buf.append("corePoolSize: " + corePoolSize + "\n");
        buf.append("maximumPoolSize: " + maximumPoolSize + "\n");
        buf.append("dequeueTimeout: " + dequeueTimeout + "\n");
        logger.info(buf.toString());

        // as we cannot prestart more core than corePoolSize initial threads, set the 
        // corePoolSize to the nu,ber of initial threads and decrease it after the 
        // initial threads have been created
        
        /*
        for (int i = 0; i < initialThreads; i++) {
            if (!executor.prestartCoreThread()) {
                logger.warning("Could not start required number of initial threads (only started " + i
                               + " out of " + initialThreads + ").");
                break;
            }
        }
        */
        
        if (initialThreads < Integer.MAX_VALUE && initialThreads > 0) {
            executor.setCorePoolSize(initialThreads);
            int started = executor.prestartAllCoreThreads();
            if (started < initialThreads) {
                logger.warning("Could not start required number of initial threads (only started " + started
                               + " out of " + initialThreads + ").");
            }
            executor.setCorePoolSize(corePoolSize);
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        buf.append(" [queue size: ");
        buf.append(queue.size());
        buf.append(", max size: ");
        buf.append(maxQueueSize);
        buf.append(", threads: ");
        buf.append(getThreadCount());
        buf.append(", active threads: ");
        buf.append(getWorkingThreadCount());
        buf.append(", low water mark: ");
        buf.append(getLowWaterMark());
        buf.append(", high water mark: ");
        buf.append(getHighWaterMark());
        buf.append("]");
        return buf.toString();

    }

    // WorkQueue interface

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueue#enqueue(org.objectweb.celtix.workqueue.WorkItem,
     *      long)
     */
    public boolean enqueue(WorkItem w, long t) {
        boolean enqueued = false;
        Runnable r = new RunnableWorkItem(w);
        enqueued = enqueueImmediate(w);
        if (!enqueued) {
            try {
                enqueued = queue.offer(r, t, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                logger.warning("Attempt to enqueue workitem was interrupted.");
            }  
        }
        return enqueued;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueue#enqueueImmediate(org.objectweb.celtix.workqueue.WorkItem)
     */
    public boolean enqueueImmediate(WorkItem work) {

        // return queue.offer(new RunnableWorkItem(work));
        try {
            executor.execute(new RunnableWorkItem(work));
        } catch (RejectedExecutionException ex) {
            logger.info(ex.getMessage());
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueue#flush()
     */
    public void flush() {
        queue.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueue#getMaxSize()
     */
    public synchronized long getMaxSize() {
        return maxQueueSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueue#getSize()
     */
    public long getSize() {
        return queue.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueue#isEmpty()
     */
    public boolean isEmpty() {
        return queue.size() == 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueue#isFull()
     */
    public boolean isFull() {
        return queue.remainingCapacity() == 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueue#ownsCurrentThread()
     */
    public boolean ownsCurrentThread() {
        // TODO Auto-generated method stub
        return false;
    }

    // AutomaticWorkQueue interface

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.AutomaticWorkQueue#getHighWaterMark()
     */
    public int getHighWaterMark() {
        int hwm = executor.getMaximumPoolSize();
        return hwm == Integer.MAX_VALUE ? -1 : hwm;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.AutomaticWorkQueue#getLowWaterMark()
     */
    public int getLowWaterMark() {
        int lwm = executor.getCorePoolSize();
        return lwm == 0 ? -1 : lwm;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.AutomaticWorkQueue#getThreadCount()
     */
    public int getThreadCount() {
        return executor.getPoolSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.AutomaticWorkQueue#getWorkingThreadCount()
     */
    public int getWorkingThreadCount() {
        return executor.getActiveCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.AutomaticWorkQueue#setHighWaterMark()
     */
    public void setHighWaterMark(int hwm) {
        executor.setMaximumPoolSize(hwm < 0 ? Integer.MAX_VALUE : hwm);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.AutomaticWorkQueue#setLowWaterMark()
     */
    public void setLowWaterMark(int lwm) {
        executor.setCorePoolSize(lwm < 0 ? 0 : lwm);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.AutomaticWorkQueue#shutdown(boolean)
     */
    public void shutdown(boolean processRemainingWorkItems) {
        if (!processRemainingWorkItems) {
            queue.clear();
        }
        executor.shutdown();
    }

    /**
     * Utility class to wrap WorkItems as Runnables
     * 
     * @author asmyth
     */
    class RunnableWorkItem implements Runnable {
        WorkItem work;

        RunnableWorkItem(WorkItem w) {
            work = w;
        }

        public void run() {
            work.execute();
        }

        public WorkItem getWorkItem() {
            return work;
        }
    }

}
