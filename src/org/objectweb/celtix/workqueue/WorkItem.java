package org.objectweb.celtix.workqueue;

public interface WorkItem {

    enum WorkItemStatus {
        STOP_WORKING, CONTINUE_WORKING
    };

    WorkItemStatus execute();

    void destroy();
}
