package org.objectweb.celtix.jaxws.handlers;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.Phase;

public class LogicalHandlerInterceptor extends AbstractJAXWSHandlerInterceptor {
    
    public LogicalHandlerInterceptor(HandlerChainInvoker invoker) {
        super(invoker);
        setPhase(Phase.USER_LOGICAL);
    }
    
    public void handleMessage(Message message) {
        LogicalMessageContextImpl lctx = new LogicalMessageContextImpl(message);
        if (!invoker.invokeLogicalHandlers(isRequestor(message), lctx)) {
            // need to abort - not sure how to do this:
            // we have access to the interceptor chain via the message but 
            // there is no support for terminating the chain yet
        }
    }
    
    public void handleFault(Message message) {
        // TODO
    }
    
    
    public void onCompletion(Message message) {
        if (isRequestor(message) && (isOneway(message) || !isOutbound(message))) {
            invoker.mepComplete(message);
        }
    }
   
}
