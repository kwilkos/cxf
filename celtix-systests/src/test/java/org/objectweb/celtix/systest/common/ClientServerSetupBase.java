package org.objectweb.celtix.systest.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import junit.extensions.TestSetup;
import junit.framework.Test;

import org.objectweb.celtix.Bus;

public abstract class ClientServerSetupBase extends TestSetup {
    private final List<ServerLauncher> launchers = new ArrayList<ServerLauncher>();  
    private Bus bus; 

    public ClientServerSetupBase(Test arg0) {
        super(arg0);
    }

    public void setUp() throws Exception {
        bus = Bus.init();
        Bus.setCurrent(bus);
        startServers();
    }
    
    public abstract void startServers() throws Exception;
    
    public void tearDown() throws Exception {
        stopAllServers();
        bus.shutdown(true);
        Bus.setCurrent(null);
        bus = null;
        launchers.clear();
        System.gc();
        System.gc();
    } 
    
    protected boolean stopAllServers() {
        boolean passed = true;
        for (ServerLauncher sl : launchers) {
            try { 
                passed = passed && sl.stopServer(); 
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        launchers.clear();
        return passed;
    }
    
    public boolean launchServer(Class<?> clz) {
        boolean ok = false;
        try { 
            ServerLauncher sl = new ServerLauncher(clz.getName());
            ok = sl.launchServer();
            assertTrue("server failed to launch", ok);
            launchers.add(sl);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("failed to launch server " + clz);
        }
        
        return ok;
    }
    
}
