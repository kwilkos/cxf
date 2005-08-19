package org.objectweb.celtix.bus.workqueue;

import junit.framework.TestCase;

import org.objectweb.celtix.workqueue.WorkItem;

public class AutomaticWorkQueueTest extends TestCase {

    public static final int UNBOUNDED_MAX_QUEUE_SIZE = -1;
    public static final int UNBOUNDED_HIGH_WATER_MARK = -1;
    public static final int UNBOUNDED_LOW_WATER_MARK = -1;

    public static final int INITIAL_SIZE = 2;
    public static final int DEFAULT_MAX_QUEUE_SIZE = 10;
    public static final int DEFAULT_HIGH_WATER_MARK = 10;
    public static final int DEFAULT_LOW_WATER_MARK = 1;
    public static final long DEFAULT_DEQUEUE_TIMEOUT = 2 * 60 * 1000L;

    public static final int TIMEOUT = 100;

    public void testUnboundedConstructor() {
        AutomaticWorkQueueImpl workqueue = new AutomaticWorkQueueImpl(UNBOUNDED_MAX_QUEUE_SIZE, INITIAL_SIZE,
                                                                      UNBOUNDED_HIGH_WATER_MARK,
                                                                      UNBOUNDED_LOW_WATER_MARK,
                                                                      DEFAULT_DEQUEUE_TIMEOUT);
        assertNotNull(workqueue);
        assertEquals(AutomaticWorkQueueImpl.DEFAULT_MAX_QUEUE_SIZE, workqueue.getMaxSize());
        assertEquals(UNBOUNDED_HIGH_WATER_MARK, workqueue.getHighWaterMark());
        assertEquals(UNBOUNDED_LOW_WATER_MARK, workqueue.getLowWaterMark());
    }

    public void testConstructor() {
        AutomaticWorkQueueImpl workqueue = new AutomaticWorkQueueImpl(DEFAULT_MAX_QUEUE_SIZE, INITIAL_SIZE,
                                                                      DEFAULT_HIGH_WATER_MARK,
                                                                      DEFAULT_LOW_WATER_MARK,
                                                                      DEFAULT_DEQUEUE_TIMEOUT);
        assertNotNull(workqueue);
        assertEquals(DEFAULT_MAX_QUEUE_SIZE, workqueue.getMaxSize());
        assertEquals(DEFAULT_HIGH_WATER_MARK, workqueue.getHighWaterMark());
        assertEquals(DEFAULT_LOW_WATER_MARK, workqueue.getLowWaterMark());
    }

    public void testEnqueue() {
        AutomaticWorkQueueImpl workqueue = new AutomaticWorkQueueImpl(DEFAULT_MAX_QUEUE_SIZE, INITIAL_SIZE,
                                                                      DEFAULT_HIGH_WATER_MARK,
                                                                      DEFAULT_LOW_WATER_MARK,
                                                                      DEFAULT_DEQUEUE_TIMEOUT);

        try {
            Thread.sleep(100);
        } catch (Exception e) {
            // ignore 
        }

        // We haven't enqueued anything yet, so should be zero
        assertEquals(0, workqueue.getSize());
        assertEquals(INITIAL_SIZE, workqueue.getThreadCount());

        // Check that no threads are working yet, as we haven't enqueued
        // anything yet.
        assertEquals(0, workqueue.getWorkingThreadCount());

        assertTrue(workqueue.enqueue(new TestWorkItem(), TIMEOUT));

        // Give threads a chance to dequeue (5sec max)
        int i = 0;
        while (workqueue.getSize() != 0 && i++ < 50) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        assertEquals(0, workqueue.getSize());
    }
    
    public void testEnqueueImmediate() {
        AutomaticWorkQueueImpl workqueue = new AutomaticWorkQueueImpl(DEFAULT_MAX_QUEUE_SIZE, INITIAL_SIZE,
                                                                      DEFAULT_HIGH_WATER_MARK,
                                                                      DEFAULT_LOW_WATER_MARK,
                                                                      DEFAULT_DEQUEUE_TIMEOUT);

        try {
            Thread.sleep(100);
        } catch (Exception e) {
            // ignore
        }

        // We haven't enqueued anything yet, so should there shouldn't be
        // any items on the queue, the thread pool should still be the
        // initial size and no threads should be working
        //
        assertEquals(0, workqueue.getSize());
        assertEquals(INITIAL_SIZE, workqueue.getThreadCount());
        assertEquals(0, workqueue.getWorkingThreadCount());

        BlockingWorkItem[] workItems = new BlockingWorkItem[DEFAULT_HIGH_WATER_MARK];
        BlockingWorkItem[] fillers = new BlockingWorkItem[DEFAULT_MAX_QUEUE_SIZE];
        
        try {
            // fill up the queue, then exhaust the thread pool
            //
            for (int i = 0; i < DEFAULT_HIGH_WATER_MARK; i++) {
                workItems[i] = new BlockingWorkItem();
                assertTrue(workqueue.toString(), workqueue.enqueueImmediate(workItems[i]));
            }
             
            for (int i = 0; i < DEFAULT_MAX_QUEUE_SIZE; i++) {
                fillers[i] = new BlockingWorkItem();
                assertTrue(workqueue.toString(), workqueue.enqueueImmediate(fillers[i]));
            }
            
            // give threads a chance to start executing the work items
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                // ignore
            }            
            
            assertTrue(workqueue.toString(), workqueue.isFull());
            assertEquals(workqueue.toString(), DEFAULT_HIGH_WATER_MARK, workqueue.getThreadCount());
            assertEquals(workqueue.toString(), DEFAULT_HIGH_WATER_MARK, workqueue.getWorkingThreadCount());
            
            // this attempt should now fail

            assertTrue(!workqueue.enqueueImmediate(new BlockingWorkItem()));

            // unblock one work item and allow thread to dequeue next item 
            
            workItems[0].unblock();
            boolean enqueued = false;
            workItems[0] = new BlockingWorkItem();
            
            for (int i = 0; i < 20 && !enqueued; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // ignore
                }
                enqueued = workqueue.enqueueImmediate(workItems[0]);  
            }
            assertTrue(enqueued);
        } finally {
            for (int i = 0; i < DEFAULT_HIGH_WATER_MARK; i++) {
                if (workItems[i] != null) {
                    workItems[i].unblock();
                }
            }
            for (int i = 0; i < DEFAULT_MAX_QUEUE_SIZE; i++) {
                if (fillers[i] != null) {
                    fillers[i].unblock();
                }
            }
        }
    }
    
    public void testDeadLockEnqueueLoads() {
        DeadLockThread dead = 
            new DeadLockThread(new AutomaticWorkQueueImpl(500, 1, 2, 2, 
                                                          DEFAULT_DEQUEUE_TIMEOUT), 200, 10L);

        assertTrue(checkDeadLock(dead));
    }

    public void testNonDeadLockEnqueueLoads() {
        DeadLockThread dead = 
            new DeadLockThread(new AutomaticWorkQueueImpl(UNBOUNDED_MAX_QUEUE_SIZE,
                                                          INITIAL_SIZE, UNBOUNDED_HIGH_WATER_MARK,
                                                          UNBOUNDED_LOW_WATER_MARK,
                                                          DEFAULT_DEQUEUE_TIMEOUT), 200);

        assertTrue(checkDeadLock(dead));
    }

    public void testThreadPoolShrink() {
        AutomaticWorkQueueImpl workqueue = 
            new AutomaticWorkQueueImpl(UNBOUNDED_MAX_QUEUE_SIZE, 20, 20, 10, 100L);

        DeadLockThread dead = new DeadLockThread(workqueue, 1000, 5L);

        assertTrue("Should be finished, probably deadlocked", checkDeadLock(dead));

        // Give threads a chance to dequeue (5sec max)
        int i = 0;
        while (workqueue.getThreadCount() != 10 && i++ < 50) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        assertEquals(workqueue.getLowWaterMark(), workqueue.getThreadCount());
    }

    public void testThreadPoolShrinkUnbounded() {
        AutomaticWorkQueueImpl workqueue = new AutomaticWorkQueueImpl(UNBOUNDED_MAX_QUEUE_SIZE, INITIAL_SIZE,
                                                                      UNBOUNDED_HIGH_WATER_MARK,
                                                                      UNBOUNDED_LOW_WATER_MARK, 100L);

        DeadLockThread dead = new DeadLockThread(workqueue, 1000, 5L);
        assertTrue("Should be finished, probably deadlocked", checkDeadLock(dead));

        // Give threads a chance to dequeue (5sec max)
        int i = 0;
        while (workqueue.getThreadCount() != 0 && i++ < 50) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        assertEquals("threads_total()", 0, workqueue.getThreadCount());
    }

    public void testShutdown() {
        AutomaticWorkQueueImpl workqueue = new AutomaticWorkQueueImpl(DEFAULT_MAX_QUEUE_SIZE, INITIAL_SIZE,
                                                                      INITIAL_SIZE, INITIAL_SIZE,
                                                                      250);

        assertEquals(0, workqueue.getSize());
        DeadLockThread dead = new DeadLockThread(workqueue, 100, 5L);
        dead.start();
        assertTrue(checkCompleted(dead));

        workqueue.shutdown(true);

        // Give threads a chance to shutdown (1 sec max)
        for (int i = 0; i < 20 && (workqueue.getSize() > 0 || workqueue.getThreadCount() > 0); i++) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        assertEquals(0, workqueue.getSize());
        assertEquals(0, workqueue.getThreadCount());
    }

    private boolean checkCompleted(DeadLockThread dead) {
        int oldCompleted = 0;
        int newCompleted = 0;
        int noProgressCount = 0;
        while (!dead.isFinished()) {
            newCompleted = dead.getWorkItemCompletedCount();
            if (newCompleted > oldCompleted) {
                oldCompleted = newCompleted;
            } else {
                // No reduction in the completion count so it may be deadlocked,
                // allow thread to make no progress for 5 time-slices before
                // assuming a deadlock has occurred
                //  
                if (oldCompleted != 0) {
                    if (++noProgressCount > 5) {
                        return false;
                    }
                }
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        return true;
    }

    private boolean checkDeadLock(DeadLockThread dead) {
        dead.start();
        return checkCompleted(dead);
    }

    public class TestWorkItem implements WorkItem {
        String name;
        long worktime;
        Callback callback;

        public TestWorkItem() {
            this("WI");
        }

        public TestWorkItem(String n) {
            this(n, DeadLockThread.DEFAULT_WORK_TIME);
        }

        public TestWorkItem(String n, long wt) {
            this(n, wt, null);
        }

        public TestWorkItem(String n, long wt, Callback c) {
            name = n;
            worktime = wt;
            callback = c;
        }

        public WorkItemStatus execute() {
            try {
                try {
                    Thread.sleep(worktime);
                } catch (InterruptedException ie) {
                    // ignore
                }
                return WorkItemStatus.CONTINUE_WORKING;
            } finally {
                if (callback != null) {
                    callback.workItemCompleted(name);
                }
            }
        }

        public void destroy() {
        }

        public String toString() {
            return "[TestWorkItem:name=" + name + "]";
        }
    }

    public class BlockingWorkItem implements WorkItem {
        private boolean unblocked;

        public WorkItemStatus execute() {
            synchronized (this) {
                while (!unblocked) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                        // ignore
                    }
                }
            }
            return WorkItemStatus.CONTINUE_WORKING;
        }

        public void destroy() {
        }

        void unblock() {
            synchronized (this) {
                unblocked = true;
                notify();
            }
        }
    }

    public interface Callback {
        void workItemCompleted(String name);
    }

    public class DeadLockThread extends Thread implements Callback {
        public static final long DEFAULT_WORK_TIME = 10L;
        public static final int DEFAULT_WORK_ITEMS = 200;

        AutomaticWorkQueueImpl workqueue;
        int nWorkItems = 0;
        int nWorkItemsCompleted = 0;
        long worktime = 0L;
        long finishTime = 0L;
        long startTime = 0L;

        public DeadLockThread(AutomaticWorkQueueImpl wq) {
            this(wq, DEFAULT_WORK_ITEMS, DEFAULT_WORK_TIME);
        }

        public DeadLockThread(AutomaticWorkQueueImpl wq, int nwi) {
            this(wq, nwi, DEFAULT_WORK_TIME);
        }

        public DeadLockThread(AutomaticWorkQueueImpl wq, int nwi, long wt) {
            workqueue = wq;
            nWorkItems = nwi;
            worktime = wt;
        }

        public synchronized boolean isFinished() {
            return nWorkItemsCompleted == nWorkItems;
        }

        public synchronized void workItemCompleted(String name) {
            nWorkItemsCompleted++;
            if (isFinished()) {
                finishTime = System.currentTimeMillis();
            }
        }

        public int getWorkItemCount() {
            return nWorkItems;
        }

        public long worktime() {
            return worktime;
        }

        public synchronized int getWorkItemCompletedCount() {
            return nWorkItemsCompleted;
        }

        public long finishTime() {
            return finishTime;
        }

        public long duration() {
            return finishTime - startTime;
        }

        public void run() {
            startTime = System.currentTimeMillis();

            for (int i = 0; i < nWorkItems; i++) {
                workqueue.enqueue(new TestWorkItem(String.valueOf(i), worktime, this), TIMEOUT);
            }
            while (!isFinished()) {
                try {
                    Thread.sleep((nWorkItems * worktime) + 50);
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }
    }

}
