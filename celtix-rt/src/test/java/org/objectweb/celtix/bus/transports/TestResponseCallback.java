package org.objectweb.celtix.bus.transports;

import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.context.InputStreamMessageContext;

public class TestResponseCallback implements ResponseCallback {

    private InputStreamMessageContext responseContext;
    private boolean retrieved;
    
    public synchronized void dispatch(InputStreamMessageContext respCtx) {
        this.responseContext = respCtx;
        retrieved = false;
        notify();
    }
    
    public synchronized InputStreamMessageContext waitForNextResponse() {
        while (responseContext == null || retrieved) {
            try {
                wait();
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        retrieved = true;
        return responseContext;
    }

}
