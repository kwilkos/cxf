package org.objectweb.celtix.jaxws.handlers;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.jaxws.context.WrappedMessageContext;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.Phase;

public abstract class AbstractProtocolHandlerInterceptor<T extends Message> 
    extends AbstractJAXWSHandlerInterceptor<T> {

    protected AbstractProtocolHandlerInterceptor(HandlerChainInvoker invoker) {
        super(invoker);
        setPhase(Phase.USER_PROTOCOL);
    }
    
    public void handleMessage(T message) {
        MessageContext context = createProtocolMessageContext(message);
        invoker.invokeProtocolHandlers(isRequestor(message), context);            
    }
    
    protected MessageContext createProtocolMessageContext(Message message) {
        return new WrappedMessageContext(message);
    }
}
