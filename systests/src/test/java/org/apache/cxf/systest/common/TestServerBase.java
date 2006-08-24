package org.apache.cxf.systest.common;

import org.apache.cxf.Bus;
import org.apache.cxf.testutil.common.AbstractTestServerBase;

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
