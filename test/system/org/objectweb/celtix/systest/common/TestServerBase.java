package org.objectweb.celtix.systest.common;

import java.util.logging.Logger;

import junit.framework.Assert;

public abstract class TestServerBase extends Assert implements Runnable {
        
    private static final Logger LOG = Logger.getLogger(TestServerBase.class.getName());
    
    private static final long DEFAULT_STARTUP_TIMEOUT = 30 * 1000;
    private static final long DEFAULT_SERVER_TIMEOUT = 3 * 60 * 1000;
    
    private final Mutex readyMutex = new Mutex();
    private final Mutex runningMutex = new Mutex();

    private boolean serverIsReady;
    private boolean serverIsRunning;
    
    public void setUp() throws Exception {
        // emtpy
    }
    
    public void tearDown() throws Exception {
        // empty
    }
    
    protected void ready() {
        synchronized (readyMutex) {
            LOG.info("server is ready");
            serverIsReady = true;
            readyMutex.notifyAll();
        }
        synchronized (runningMutex) {
            serverIsRunning = true;
            TimeoutCounter tc = new TimeoutCounter(DEFAULT_SERVER_TIMEOUT);
            do { 
                try { 
                    runningMutex.wait(DEFAULT_SERVER_TIMEOUT);
                    if (tc.isTimeoutExpired()) {
                        LOG.info("server timeout expired");
                        break;
                    }
                } catch (InterruptedException ex) { 
                    // emtpy
                }
            } while (serverIsRunning);
            LOG.info("server shutting down");
        }
    }
    
   
    protected void startFailed() {
        synchronized (readyMutex) {
            LOG.info("server startup failed");
            serverIsReady = false; 
            readyMutex.notifyAll();
        }
    }
    
   
    public void stopServer() { 
        synchronized (runningMutex) {
            LOG.info("stopping server");
            serverIsRunning = false; 
            runningMutex.notifyAll();
        }
    }
    
    
    public boolean waitForReady() {
        LOG.info("waiting for server");

        synchronized (readyMutex) {
            TimeoutCounter tc = new TimeoutCounter(DEFAULT_STARTUP_TIMEOUT);        
            do { 
                try {            
                    readyMutex.wait(DEFAULT_STARTUP_TIMEOUT);
                    if (tc.isTimeoutExpired()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    // emtpy
                }
            } while (!serverIsReady);
            LOG.info("server has started: " + serverIsReady);
            return serverIsReady; 
        }
    }
    
    static class TimeoutCounter {
        private final long expectedEndTime; 
        
        public TimeoutCounter(long theExpectedTimeout) { 
            expectedEndTime = System.currentTimeMillis() + theExpectedTimeout;
        }
        
        public boolean isTimeoutExpired() {
            return System.currentTimeMillis() > expectedEndTime;
        }
    }
    
    class Mutex {
        // emtpy
    }
}
