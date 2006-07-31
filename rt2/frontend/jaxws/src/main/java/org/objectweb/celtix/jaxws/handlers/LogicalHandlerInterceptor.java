package org.objectweb.celtix.jaxws.handlers;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.Phase;

public class LogicalHandlerInterceptor<T extends Message> extends AbstractJAXWSHandlerInterceptor<T> {
    
    public LogicalHandlerInterceptor() {
        setPhase(Phase.USER_LOGICAL);
    }
    
    public void handleMessage(T message) {
        LogicalMessageContextImpl lctx = new LogicalMessageContextImpl(message);
        if (!getInvoker(message).invokeLogicalHandlers(isRequestor(message), lctx)) {
            // need to abort - not sure how to do this:
            // we have access to the interceptor chain via the message but 
            // there is no support for terminating the chain yet
        }
    }
    
    public void handleFault(T message) {
        // TODO
    }
    
    
    public void onCompletion(T message) {
        if (isRequestor(message) && (isOneway(message) || !isOutbound(message))) {
            getInvoker(message).mepComplete(message);
        }
    }
   
}
