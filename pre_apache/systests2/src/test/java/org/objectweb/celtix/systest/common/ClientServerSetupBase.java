package org.objectweb.celtix.systest.common;

import junit.framework.Test;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.CeltixBusFactory;
import org.objectweb.celtix.testutil.common.AbstractClientServerSetupBase;

public abstract class ClientServerSetupBase extends AbstractClientServerSetupBase {
    protected String configFileName;
    private Bus bus; 

    public ClientServerSetupBase(Test arg0) {
        super(arg0);
    }

    public void setUp() throws Exception {
        if (configFileName != null) {
            System.setProperty("celtix.config.file", configFileName);
        }
        CeltixBusFactory bf = new CeltixBusFactory();
        bus = new CeltixBusFactory().createBus();
        bf.setDefaultBus(bus);
        super.setUp();
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        bus.shutdown(true);
        bus = null;
        if (configFileName != null) {
            System.clearProperty("celtix.config.file");
        }
    } 
    
}
