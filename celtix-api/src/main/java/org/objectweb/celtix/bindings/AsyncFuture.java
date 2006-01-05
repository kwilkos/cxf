package org.objectweb.celtix.bindings;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.ws.ResponseWrapper;

import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;

public class AsyncFuture implements Future<ObjectMessageContext> {
    
    private final Future<InputStreamMessageContext> futureInputStreamMsgContext;
    private final AbstractClientBinding absClientBinding;
    private final HandlerInvoker handlerInvoker;
    private final DataBindingCallback callback;
    private final ObjectMessageContext context;
    private ObjectMessageContext replyCtx;
    
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
        if (replyCtx == null) {
            InputStreamMessageContext ins = futureInputStreamMsgContext.get();  
            replyCtx = 
                absClientBinding.getObjectMessageContextAsync(ins, handlerInvoker, 
                    callback, context);
                    
            // wrap the return value if required
            String returnType = replyCtx.getReturn().getClass().getName();
            Method m = ((JAXBDataBindingCallback)callback).getMethod();
            String requiredReturnType = m.getAnnotation(ResponseWrapper.class).className();
            if (!requiredReturnType.equals(returnType)) {
                Object wrappedRetVal = ((JAXBDataBindingCallback)callback).createWrapperType(replyCtx, true);
                replyCtx.setReturn(wrappedRetVal);
            }
        }
        return replyCtx;

        
        //return absClientBinding.getObjectMessageContextAsync(ins, handlerInvoker, callback, context);
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
