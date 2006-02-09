package org.objectweb.celtix.systest.common;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.testutil.common.AbstractTestServerBase;

public abstract class TestServerBase extends AbstractTestServerBase {
    
    private Bus bus;
    
    public boolean stopInProcess() throws Exception {
        boolean ret = super.stopInProcess();
        if (bus != null) {
            bus.shutdown(true);
        }
        return ret;
    }    
    
    public Bus getBus() {
        return bus; 
    }
    
}
