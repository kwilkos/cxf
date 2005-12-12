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

    public Object get() throws InterruptedException, ExecutionException {
        
        
        ObjectMessageContext omc = fObjMsgContext.get();
        LOG.info("AsyncResponse get ObjectMessageContext: " + omc.toString());
        LOG.info("AsyncResponse get ObjectMessageContext Method name: " + omc.getMethod().getName());
        Object o = omc.getReturn();
        LOG.info("AsyncResponse get: " + o);
        return o;
        //return fObjMsgContext.get();
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
