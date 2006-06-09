package org.objectweb.celtix.bus.jaxws;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;



import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;

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
            ObjectMessageContext ctx = fObjMsgContext.get();
            result = cls.cast(ctx.getReturn());
            Throwable t = ctx.getException();
            if (t != null) {
                if (WebServiceException.class.isAssignableFrom(t.getClass())) {
                    throw new ExecutionException(t);
                } else {
                    throw new ExecutionException(new WebServiceException(t));
                }
            }
        } 
        return result;
    }

    
    public T get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        if (result == null) {
            ObjectMessageContext ctx = fObjMsgContext.get(timeout, unit);
            result = cls.cast(ctx.getReturn());
            Throwable t = ctx.getException();
            if (t != null) {
                if (WebServiceException.class.isAssignableFrom(t.getClass())) {
                    throw new ExecutionException(t);
                } else {
                    throw new ExecutionException(new WebServiceException(t));
                }
            }
        }
        return result;
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
