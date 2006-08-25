package org.objectweb.celtix.bus.workqueue;

import java.util.logging.Logger;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.workqueue.AutomaticWorkQueue;
import org.objectweb.celtix.workqueue.WorkQueueManager;

public class WorkQueueManagerImpl implements WorkQueueManager {

    private static final Logger LOG = 
        Logger.getLogger(WorkQueueManagerImpl.class.getName());

    ThreadingModel threadingModel = ThreadingModel.MULTI_THREADED;
    AutomaticWorkQueue autoQueue;
    Bus bus;

    public WorkQueueManagerImpl(Bus b) {
        bus = b;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueueManager#getAutomaticWorkQueue()
     */
    public synchronized AutomaticWorkQueue getAutomaticWorkQueue() {
        if (autoQueue == null) {
            autoQueue = createAutomaticWorkQueue();
        }
        return autoQueue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueueManager#getThreadingModel()
     */
    public ThreadingModel getThreadingModel() {
        return threadingModel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueueManager#setThreadingModel(
     *      org.objectweb.celtix.workqueue.WorkQueueManager.ThreadingModel)
     */
    public void setThreadingModel(ThreadingModel model) {
        threadingModel = model;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueueManager#shutdown(boolean)
     */
    public synchronized void shutdown(boolean processRemainingTasks) {
        if (null != autoQueue) {
            if (autoQueue != null) {
                autoQueue.shutdown(processRemainingTasks);
            }
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public void run() {
        synchronized (this) {
            while (!autoQueue.isShutdown()) {
                try {            
                    wait();
                } catch (InterruptedException ex) {
                    // ignore
                }
            }
        }
        for (java.util.logging.Handler h : LOG.getHandlers())  {
            h.flush();
        }
    }

    private AutomaticWorkQueue createAutomaticWorkQueue() {

        // Configuration configuration = bus.getConfiguration();

        // configuration.getInteger("threadpool:initial_threads");
        int initialThreads = 1;
        
        // int lwm = configuration.getInteger("threadpool:low_water_mark");
        int lwm = 5;

        // int hwm = configuration.getInteger("threadpool:high_water_mark");
        int hwm = 25;

        // configuration.getInteger("threadpool:max_queue_size");
        int maxQueueSize = 10 * hwm;

        // configuration.getInteger("threadpool:dequeue_timeout");
        long dequeueTimeout = 2 * 60 * 1000L;

        return new AutomaticWorkQueueImpl(maxQueueSize, initialThreads, hwm, lwm, dequeueTimeout);
    }

}
