package org.objectweb.celtix.workqueue;

public interface WorkQueueManager {

    enum ThreadingModel {
        SINGLE_THREADED, MULTI_THREADED
    };

    AutomaticWorkQueue getAutomaticWorkQueue();

    ThreadingModel getThreadingModel();

    void setThreadingModel(ThreadingModel model);
    
    void shutdown(boolean processRemainingTasks);
    
    /**
     * Only returns after workqueue has been shutdown.
     *
     */
    void run();
}
