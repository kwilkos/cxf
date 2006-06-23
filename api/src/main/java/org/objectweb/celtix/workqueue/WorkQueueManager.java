package org.objectweb.celtix.workqueue;

public interface WorkQueueManager {

    enum ThreadingModel {
        SINGLE_THREADED, MULTI_THREADED
    };

    /**
     * Get the manager's work queue.
     * @return AutomaticWorkQueue
     */
    AutomaticWorkQueue getAutomaticWorkQueue();

    /**
     * Get the threading model.
     * @return ThreadingModel - either <code>SINGLE_THREADED</code>
     * or <code>MULTI_THREADED</code>.
     */
    ThreadingModel getThreadingModel();

    /**
     * Set the threading model.
     * @param model either <code>SINGLE_THREADED</code>
     * or <code>MULTI_THREADED</code>.
     */
    void setThreadingModel(ThreadingModel model);
    
    /**
     * Shuts down the manager's work queue. If
     * <code>processRemainingTasks</code> is true, waits for the work queue to
     * shutdown before returning.
     * @param processRemainingTasks - whether or not to wait for completion
     */
    void shutdown(boolean processRemainingTasks);
    
    /**
     * Only returns after workqueue has been shutdown.
     *
     */
    void run();
}
