package org.objectweb.celtix.jaxws.handlers;

import org.objectweb.celtix.message.Message;

public abstract class AbstractProtocolHandlerInterceptor extends AbstractJAXWSHandlerInterceptor {

    protected AbstractProtocolHandlerInterceptor(HandlerChainInvoker invoker) {
        super(invoker);
    }
    
    public void intercept(Message message) {
        invoker.invokeProtocolHandlers(isRequestor(message), message);            
    }
}
