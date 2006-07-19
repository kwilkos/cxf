package org.objectweb.celtix.jaxws.handlers;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;

public abstract class AbstractJAXWSHandlerInterceptor<T extends Message> extends AbstractPhaseInterceptor<T> {
    protected HandlerChainInvoker invoker;
    
    AbstractJAXWSHandlerInterceptor(HandlerChainInvoker i) {
        invoker = i;
    }
    
    boolean isOneway(T message) {
        return true;
    }
    
    boolean isOutbound(T message) {
        return message == message.getExchange().getOutMessage();
    }
    
    boolean isRequestor(T message) {
        return true;
    }
}
