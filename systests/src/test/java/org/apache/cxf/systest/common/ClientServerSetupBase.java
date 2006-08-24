package org.apache.cxf.systest.common;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.CeltixBusFactory;
import org.apache.cxf.testutil.common.AbstractClientServerSetupBase;

public abstract class ClientServerSetupBase extends AbstractClientServerSetupBase {
    protected String configFileName;
    private Bus bus; 

    public ClientServerSetupBase(Test arg0) {
        super(arg0);
    }

    public void setUp() throws Exception {
        if (configFileName != null) {
            System.setProperty("cxf.config.file", configFileName);
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
            System.clearProperty("cxf.config.file");
        }
    } 
    
}
