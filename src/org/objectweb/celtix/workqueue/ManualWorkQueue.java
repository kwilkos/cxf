package org.objectweb.celtix.workqueue;

public interface ManualWorkQueue extends WorkQueue {

    boolean dequeue(WorkItem work, long timeout);

    boolean doWork(long workItemCount, long timeout);

    void shutdown(boolean processRemainingWorkItems);

}
