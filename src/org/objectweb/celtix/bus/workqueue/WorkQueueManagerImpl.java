package org.objectweb.celtix.bus.workqueue;

import java.util.logging.Logger;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.workqueue.AutomaticWorkQueue;
import org.objectweb.celtix.workqueue.WorkQueueManager;

public class WorkQueueManagerImpl implements WorkQueueManager {

    private static final Logger LOG = 
        Logger.getLogger(WorkQueueManagerImpl.class.getName());

    ThreadingModel threadingModel = ThreadingModel.MULTI_THREADED;
    AutomaticWorkQueue autoQueue = createAutomaticWorkQueue();
    Bus bus;

    public WorkQueueManagerImpl(Bus b) {
        bus = b;
        autoQueue = createAutomaticWorkQueue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.workqueue.WorkQueueManager#getAutomaticWorkQueue()
     */
    public AutomaticWorkQueue getAutomaticWorkQueue() {
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
    public void shutdown(boolean processRemainingTasks) {
        if (null != autoQueue) {
            autoQueue.shutdown(processRemainingTasks);
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

        // int lwm = configuration.getInteger("threadpool:low_water_mark");
        int lwm = -1;

        // int hwm = configuration.getInteger("threadpool:high_water_mark");
        int hwm = -1;

        // int initialThreads =
        // configuration.getInteger("threadpool:initial_threads");
        int initialThreads = 5;
        if (initialThreads > hwm) {
            initialThreads = hwm;
        }

        // int maxSize =
        // configuration.getInteger("threadpool:max_queue_size");
        int maxQueueSize = -1;

        // configuration.getInteger("threadpool:dequeue_timeout");
        long dequeueTimeout = 2 * 60 * 1000L;

        return new AutomaticWorkQueueImpl(maxQueueSize, initialThreads, hwm, lwm, dequeueTimeout);
    }

}
