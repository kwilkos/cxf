package org.objectweb.celtix.jaxws.handlers;

import org.objectweb.celtix.message.Message;

public abstract class AbstractProtocolHandlerInterceptor extends AbstractJAXWSHandlerInterceptor {

    protected AbstractProtocolHandlerInterceptor(HandlerChainInvoker invoker) {
        super(invoker);
    }
    
    public void handleMessage(Message message) {
        invoker.invokeProtocolHandlers(isRequestor(message), message);            
    }
    
    public void handleFault(Message message) {
        
    }
}
