package org.objectweb.celtix.bus.jaxws;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.objectweb.celtix.common.logging.LogUtils;

public class AsyncCallbackFuture implements Future, Runnable {
    
    private static final Logger LOG = LogUtils.getL7dLogger(AsyncCallbackFuture.class);
    private final Response response;
    private final AsyncHandler callback; 
    private boolean done;
    
    public AsyncCallbackFuture(Response r, AsyncHandler c) {
        response = r;
        callback = c;    
    }
    
    public void run() {
        try {
            get();
        } catch (InterruptedException ex) {
            LOG.severe(ex.getMessage());
        } catch (ExecutionException ex) {
            LOG.severe(ex.getMessage());
        }
    }
     
    public boolean cancel(boolean interrupt) {
        return response.cancel(interrupt);     
    }
    
    public boolean isCancelled() {
        return response.isCancelled(); 
    }

    public boolean isDone() {
        return done;
    }

    public Object get() throws InterruptedException, ExecutionException {
        callback.handleResponse(response);
        done = true;
        return null;
    }

    
    public Object get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        // TODO - we have to manually check if the callback has finished executing 
        callback.handleResponse(response);
        return null;
    }
}
