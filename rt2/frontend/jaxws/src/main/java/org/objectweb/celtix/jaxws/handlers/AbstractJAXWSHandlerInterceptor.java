package org.objectweb.celtix.jaxws.handlers;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;

public abstract class AbstractJAXWSHandlerInterceptor<T extends Message> extends AbstractPhaseInterceptor<T> {

    public static final String HANDLER_CHAIN_INVOKER = "org.objectweb.celtix.jaxws.handlers.invoker";
    
    boolean isOneway(T message) {
        return true;
    }
    
    boolean isOutbound(T message) {
        return message == message.getExchange().getOutMessage();
    }
    
    boolean isRequestor(T message) {
        return true;
    }
    
    protected HandlerChainInvoker getInvoker(T message) {
        return (HandlerChainInvoker)message.getExchange().get(HANDLER_CHAIN_INVOKER);
    }
}
