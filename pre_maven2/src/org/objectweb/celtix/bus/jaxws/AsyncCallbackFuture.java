package org.objectweb.celtix.bus.jaxws;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.objectweb.celtix.common.i18n.Message;
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
   
    @SuppressWarnings("unchecked")
    public synchronized void run() { 
        try {
            callback.handleResponse(response);            
        } finally {
            done = true;
            notifyAll();
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
        waitForCallbackExecutionToFinish();      
        return null;
    }

    
    public Object get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        long ms = TimeUnit.MILLISECONDS.convert(timeout, unit);
        waitForCallbackExecutionToFinish(ms);
        return null;
    }
    
    private synchronized void waitForCallbackExecutionToFinish() {       
        while (!done) {
            LOG.fine("waiting for callback to finish execution.");
            try {
                wait();
            } catch (InterruptedException ex) {
                // deliberately ignore 
            }
        }
    }
    
    private synchronized void waitForCallbackExecutionToFinish(long millis) throws TimeoutException {       
        while (!done && millis > 0) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("waiting (max " + millis
                         + " milliseconds for callback to finish execution (max .");
            }
            long startedAt = System.currentTimeMillis(); 
            try {
                wait(millis);
            } catch (InterruptedException ex) {
                // deliberately ignore
                millis -= System.currentTimeMillis() - startedAt;
            }
        }
        if (!done) {
            throw new TimeoutException(new Message("ASYNC_HANDLER_TIMEDOUT_EXC",
                                                                    LOG).toString());
        }
    }
}
