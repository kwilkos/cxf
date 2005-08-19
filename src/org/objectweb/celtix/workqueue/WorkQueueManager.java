package org.objectweb.celtix.workqueue;

public interface WorkQueueManager {

    enum ThreadingModel {
        SINGLE_THREADED, MULTI_THREADED
    };

    AutomaticWorkQueue getAutomaticWorkQueue();

    ManualWorkQueue getManualWorkQueue();

    ThreadingModel getThreadingModel();

    void setThreadingModel(ThreadingModel model);
    
    void shutdown(boolean processRemainingTasks);
}
