package org.objectweb.celtix.systest.common;

import java.util.logging.Logger;

import junit.framework.Assert;

import org.objectweb.celtix.Bus;

public abstract class TestServerBase extends Assert {
    
    private static final Logger LOG = Logger.getLogger(TestServerBase.class.getName());
    private Bus bus;
    
    /** 
     * template method implemented by test servers.  Initialise 
     * servants and publish endpoints etc.
     *
     */
    protected abstract void run(); 
    
    
    public void start() {
        try { 
            LOG.info("initialise bus");
            bus = Bus.init();
            runBus();
            LOG.info("running server");
            run();
            LOG.info("signal ready");
            ready();
            
            // wait for a key press then shut 
            // down the server
            //
            System.in.read(); 
            LOG.info("stopping bus");
            bus.shutdown(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            startFailed();
        } finally { 
            LOG.info("server stopped");
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
        System.out.println("server startup failed");
        System.exit(-1);        
    }
    
    private void runBus() { 
        Thread t = new Thread() { 
                public void run() { 
                    LOG.info("running bus");
                    bus.run();
                } 
            }; 
        t.start();
    } 
}
