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

public class AsyncResponse<T> implements Response<T> {
    
    private static final Logger LOG = LogUtils.getL7dLogger(AsyncResponse.class);
    private final Future<ObjectMessageContext> fObjMsgContext;
    private T result;
    private Class<T> cls;
    
    public AsyncResponse(Future<ObjectMessageContext> futureObjMsgContext, Class<T> c) {
        fObjMsgContext = futureObjMsgContext;
        cls = c;
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

    public synchronized T get() throws InterruptedException, ExecutionException {
        if (result == null) {
            result = cls.cast(fObjMsgContext.get().getReturn());
        } 
        return result;
    }

    
    public T get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return cls.cast(fObjMsgContext.get(timeout, unit).getReturn());
    }
    
    public Map<String, Object> getContext() {
        try {
            return fObjMsgContext.get();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Exception occured getting context", ex);
            return null;
        }        
    }

}
