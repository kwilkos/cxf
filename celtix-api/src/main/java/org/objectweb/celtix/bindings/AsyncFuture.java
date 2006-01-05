package org.objectweb.celtix.bindings;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;

public class AsyncFuture implements Future<ObjectMessageContext> {
    
    private final Future<InputStreamMessageContext> futureInputStreamMsgContext;
    private final AbstractClientBinding absClientBinding;
    private final HandlerInvoker handlerInvoker;
    private final DataBindingCallback callback;
    private final ObjectMessageContext context;
    
    public AsyncFuture(Future<InputStreamMessageContext> fInputStreamMsgContext, 
                       AbstractClientBinding aClientBinding,
                       DataBindingCallback cback,
                       HandlerInvoker hInvoker,
                       ObjectMessageContext ctx) {
        futureInputStreamMsgContext = fInputStreamMsgContext;  
        absClientBinding = aClientBinding;
        handlerInvoker = hInvoker;
        callback = cback;
        context = ctx;
    }
    
    public synchronized ObjectMessageContext get() throws InterruptedException, ExecutionException {
        InputStreamMessageContext ins = futureInputStreamMsgContext.get();
        return absClientBinding.getObjectMessageContextAsync(ins, handlerInvoker, callback, context);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return futureInputStreamMsgContext.cancel(mayInterruptIfRunning);
    }

    public ObjectMessageContext get(long timeout, TimeUnit unit) 
        throws InterruptedException, ExecutionException, TimeoutException {
        InputStreamMessageContext ins = futureInputStreamMsgContext.get(timeout, unit);
        return absClientBinding.getObjectMessageContextAsync(ins, handlerInvoker, callback, context);
    }

    public boolean isCancelled() {
        return futureInputStreamMsgContext.isCancelled();
    }

    public boolean isDone() {
        return futureInputStreamMsgContext.isDone();
    }

}
