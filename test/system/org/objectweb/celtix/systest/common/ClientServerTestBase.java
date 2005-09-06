package org.objectweb.celtix.systest.common;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

public abstract class ClientServerTestBase extends TestCase {

    static { 
        System.setProperty("javax.xml.ws.EndpointFactory", "org.objectweb.celtix.bus.EndpointFactoryImpl");
        System.setProperty("javax.xml.ws.ServiceFactory", "org.objectweb.celtix.bus.ServiceFactoryImpl");
    }

  
    private Bus bus; 

    public void setUp() throws BusException {        
        bus = Bus.init();
    }

    public void tearDown() throws BusException {
        if (bus != null) { 
            bus.shutdown(true);
        }
    }
}
