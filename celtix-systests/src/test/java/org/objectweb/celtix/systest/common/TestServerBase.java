package org.objectweb.celtix.systest.common;

import java.util.logging.Logger;

import junit.framework.Assert;

import org.objectweb.celtix.Bus;

public abstract class TestServerBase extends Assert {
    
    private Bus bus;
    
    /** 
     * template method implemented by test servers.  Initialise 
     * servants and publish endpoints etc.
     *
     */
    protected abstract void run();

    protected Logger getLog() {
        String loggerName = this.getClass().getName();
        return Logger.getLogger(loggerName);
    }
    
    
    public void startInProcess() throws Exception {
        System.out.println("running server");
        run();
        System.out.println("signal ready");
        ready();
    }
    
    public boolean stopInProcess() throws Exception {
        boolean ret = true;
        if (verify(getLog())) {
            System.out.println("server passed");
        } else {
            ret = false;
        }
        if (bus != null) {
            bus.shutdown(true);
        }
        return ret;
    }    
    
    public void start() {
        try { 
            System.out.println("running server");
            run();
            System.out.println("signal ready");
            ready();
            
            // wait for a key press then shut 
            // down the server
            //
            System.in.read(); 
            System.out.println("stopping bus");
            
        } catch (Throwable ex) {
            ex.printStackTrace();
            startFailed();
        } finally {
            if (verify(getLog())) {
                System.out.println("server passed");
            }
            System.out.println("server stopped");
            System.exit(0);
        }
    }
    
    public Bus getBus() {
        return bus; 
    }
    
    
    public void setUp() throws Exception {
        // emtpy
    }
    
    public void tearDown() throws Exception {
        // empty
    }
    
    protected void ready() {
        System.out.println("server ready");
    }
    
    protected void startFailed() {
        System.out.println("server startup failed (not a log message)");
        System.exit(-1);        
    }

    /**
     * Used to facilitate assertions on server-side behaviour.
     *
     * @param log logger to use for diagnostics if assertions fail
     * @return true if assertions hold
     */
    protected boolean verify(Logger l) {
        return true;
    }    
}
