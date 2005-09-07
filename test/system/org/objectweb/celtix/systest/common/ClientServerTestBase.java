package org.objectweb.celtix.systest.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

public abstract class ClientServerTestBase extends TestCase {
    
    static { 
        System.setProperty("javax.xml.ws.EndpointFactory", "org.objectweb.celtix.bus.EndpointFactoryImpl");
        System.setProperty("javax.xml.ws.ServiceFactory", "org.objectweb.celtix.bus.ServiceFactoryImpl");
    }
    
    
    private Bus bus; 
    private List<ServerLauncher> launchers = new ArrayList<ServerLauncher>();  
    
    public void setUp() throws BusException {        
        bus = Bus.init();
    }
    
    public void tearDown() throws BusException {
        stopAllServers(); 
        if (bus != null) { 
            bus.shutdown(true);
        }
    }
    
    protected void stopAllServers() {
        for    (ServerLauncher sl : launchers) {
            try { 
                sl.stopServer(); 
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    protected void launchServer(Class<?> clz) {
        try { 
            ServerLauncher sl = new ServerLauncher(clz.getName());
            sl.launchServer();
            launchers.add(sl);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("failed to launch server " + clz);
        }
    }
}
