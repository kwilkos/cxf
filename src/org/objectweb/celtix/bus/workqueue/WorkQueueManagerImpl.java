package org.objectweb.celtix.bus.workqueue;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.workqueue.AutomaticWorkQueue;
import org.objectweb.celtix.workqueue.WorkQueueManager;

public class WorkQueueManagerImpl implements WorkQueueManager {

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
            // REVISIT: asmyth
            // should we extend the Executor interface after all?
            if (autoQueue instanceof AutomaticWorkQueueImpl) {
                AutomaticWorkQueueImpl aq = (AutomaticWorkQueueImpl)autoQueue;
                aq.shutdown(processRemainingTasks);
            }
        }
    }

    public void start() {
        // what we do here will probably depend on the
        // the thread model - for now just create the automatic qork queue
        // (which will be able to perform work straight away)
        createAutomaticExecutor();
    }
    
    private void createAutomaticExecutor() {

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

        autoQueue = new AutomaticWorkQueueImpl(maxQueueSize, initialThreads, hwm, lwm, dequeueTimeout);
    }

}
