package org.apache.cxf.jaxws.handler;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

public abstract class AbstractProtocolHandlerInterceptor<T extends Message> 
    extends AbstractJAXWSHandlerInterceptor<T> {
    
    protected AbstractProtocolHandlerInterceptor(Binding binding) {
        super(binding);
        setPhase(Phase.USER_PROTOCOL);
    }
    
    public void handleMessage(T message) {
        MessageContext context = createProtocolMessageContext(message);
        getInvoker(message).invokeProtocolHandlers(isRequestor(message), context);            
    }
    
    protected MessageContext createProtocolMessageContext(Message message) {
        return new WrappedMessageContext(message);
    }
}
