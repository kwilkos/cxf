package org.objectweb.celtix.jaxws.handlers;

import org.objectweb.celtix.message.Message;

public class StreamHandlerInterceptor extends AbstractJAXWSHandlerInterceptor {

    public StreamHandlerInterceptor(HandlerChainInvoker invoker) {
        super(invoker);
    }

    public void handleMessage(Message message) {
        StreamMessageContextImpl sctx = new StreamMessageContextImpl(message);
        invoker.invokeStreamHandlers(sctx);
    } 
    
    public void handleFault(Message message) {
        
    }
    
    
}
