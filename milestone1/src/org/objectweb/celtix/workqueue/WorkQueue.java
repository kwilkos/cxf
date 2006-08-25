package org.objectweb.celtix.workqueue;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public interface WorkQueue extends Executor {
    /**
     * Submits a work item for execution at some time in the future, waiting for up to a 
     * specified amount of time for the item to be accepted.
     * 
     * @param work the workitem to submit for execution.
     * @param timeout the maximum amount of time (in milliseconds) to wait for it to be accepted.
     *
     * @throws <code>RejectedExecutionException</code> if this work item cannot be accepted for execution.
     * @throws <code>NullPointerException</code> if work item is null.
     */
    void execute(Runnable work, long timeout) throws RejectedExecutionException;
}
