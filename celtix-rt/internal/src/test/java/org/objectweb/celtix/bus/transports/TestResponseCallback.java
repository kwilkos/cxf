package org.objectweb.celtix.bus.transports;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.context.InputStreamMessageContext;

public class TestResponseCallback implements ResponseCallback {

    private List<InputStreamMessageContext> responseContexts = 
        new ArrayList<InputStreamMessageContext>();
    
    public synchronized void dispatch(InputStreamMessageContext respCtx) {
        responseContexts.add(respCtx);
        notify();
    }
    
    public synchronized InputStreamMessageContext waitForNextResponse() {
        while (responseContexts.size() == 0) {
            try {
                wait();
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        return responseContexts.remove(0);
    }

}
