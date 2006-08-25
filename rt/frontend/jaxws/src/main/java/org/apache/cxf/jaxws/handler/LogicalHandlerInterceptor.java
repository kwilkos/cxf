package org.apache.cxf.jaxws.handler;

import javax.xml.ws.Binding;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

public class LogicalHandlerInterceptor<T extends Message> extends AbstractJAXWSHandlerInterceptor<T> {
    
    public LogicalHandlerInterceptor(Binding binding) {
        super(binding);
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
