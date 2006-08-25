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
    
    
    public void startInProcess() throws Exception {
        LOG.info("running server");
        run();
        LOG.info("signal ready");
        ready();
    }
    
    public boolean stopInProcess() throws Exception {
        boolean ret = true;
        if (verify(LOG)) {
            LOG.info("server passed");
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
            LOG.info("running server");
            run();
            LOG.info("signal ready");
            ready();
            
            // wait for a key press then shut 
            // down the server
            //
            System.in.read(); 
            LOG.info("stopping bus");
        } catch (Exception ex) {
            ex.printStackTrace();
            startFailed();
        } finally {
            if (verify(LOG)) {
                LOG.info("server passed");
            }
            LOG.info("server stopped");
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
        System.out.println("server startup failed");
        System.exit(-1);        
    }

    /**
     * Used to facilitate assertions on server-side behaviour.
     *
     * @param log logger to use for diagnostics if assertions fail
     * @return true if assertions hold
     */
    protected boolean verify(Logger log) {
        return true;
    }    
}
