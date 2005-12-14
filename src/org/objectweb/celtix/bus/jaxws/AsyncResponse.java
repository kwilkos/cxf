package org.objectweb.celtix.bus.jaxws;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;



import javax.xml.ws.Response;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;

public class AsyncResponse implements Response {
    
    private static final Logger LOG = LogUtils.getL7dLogger(AsyncResponse.class);
    private final Future<ObjectMessageContext> fObjMsgContext;
    private Object result;
    
    public AsyncResponse(Future<ObjectMessageContext> futureObjMsgContext) {
        fObjMsgContext = futureObjMsgContext;     
    }
    
    public boolean cancel(boolean interrupt) {
        return fObjMsgContext.cancel(interrupt);     
    }
    
    public boolean isCancelled() {
        return fObjMsgContext.isCancelled(); 
    }

    public boolean isDone() {
        return fObjMsgContext.isDone();
    }

    public synchronized Object get() throws InterruptedException, ExecutionException {
        if (result == null) {
            result = fObjMsgContext.get().getReturn();
        } 
        return result;
    }

    
    public Object get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return fObjMsgContext.get(timeout, unit).getReturn();
    }
    
    public Map getContext() {
        try {
            return fObjMsgContext.get();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Exception occured getting context", ex);
            return null;
        }
        
    }

}
