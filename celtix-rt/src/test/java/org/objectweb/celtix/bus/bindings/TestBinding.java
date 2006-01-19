package org.objectweb.celtix.bus.bindings;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bus.handlers.HandlerChainInvoker;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;

public class TestBinding  extends AbstractBindingImpl {  
    public static final String TEST_BINDING = "http://celtix.objectweb.org/bindings/test";
    
    protected HandlerInvoker createHandlerInvoker() {
        return new HandlerChainInvoker(getHandlerChain()); 
    }
    
    protected MessageContext createBindingMessageContext(MessageContext orig) {
        return new GenericMessageContext();
    }
}
