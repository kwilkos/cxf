package org.objectweb.celtix.jaxws.handlers;

import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.message.Message;

public abstract class AbstractJAXWSHandlerInterceptor implements Interceptor {
    protected HandlerChainInvoker invoker;
    
    AbstractJAXWSHandlerInterceptor(HandlerChainInvoker i) {
        invoker = i;
    }
    
    boolean isOneway(Message message) {
        return true;
    }
    
    boolean isOutbound(Message message) {
        return message == message.getExchange().getOutMessage();
    }
    
    boolean isRequestor(Message message) {
        return true;
    }
}
