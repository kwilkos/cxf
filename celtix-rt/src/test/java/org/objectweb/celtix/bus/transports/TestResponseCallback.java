package org.objectweb.celtix.bus.transports;

import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.context.InputStreamMessageContext;

public class TestResponseCallback implements ResponseCallback {

    private InputStreamMessageContext responseContext;
    private boolean retreived;
    
    public synchronized void dispatch(InputStreamMessageContext respCtx) {
        this.responseContext = respCtx;
        retreived = false;
        notify();
    }
    
    public synchronized InputStreamMessageContext waitForNextResponse() {
        while (responseContext == null || retreived) {
            try {
                wait();
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        retreived = true;
        return responseContext;
    }

}
