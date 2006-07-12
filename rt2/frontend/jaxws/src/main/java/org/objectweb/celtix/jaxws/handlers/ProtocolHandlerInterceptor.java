package org.objectweb.celtix.jaxws.handlers;

import org.objectweb.celtix.message.Message;

public class ProtocolHandlerInterceptor extends AbstractJAXWSHandlerInterceptor {

    public ProtocolHandlerInterceptor(HandlerChainInvoker invoker) {
        super(invoker);
    }
    
    public void intercept(Message message) {
        invoker.invokeProtocolHandlers(isRequestor(message), message);            
    }
}
