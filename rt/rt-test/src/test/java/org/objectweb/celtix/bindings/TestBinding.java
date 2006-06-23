package org.objectweb.celtix.bindings;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bus.handlers.HandlerChainInvoker;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;

public class TestBinding  extends AbstractBindingImpl {  
    public static final String TEST_BINDING = "http://celtix.objectweb.org/bindings/test";
    
    private final TestClientBinding clientBinding;
    private final TestServerBinding serverBinding;
    
    public TestBinding(TestClientBinding tcb) {
        clientBinding = tcb; 
        serverBinding = null;
    }
    
    public TestBinding(TestServerBinding tsb) {
        serverBinding = tsb; 
        clientBinding = null;
    }
    
    public TestClientBinding getClientBinding() {
        return clientBinding;
    }
    
    public TestServerBinding getServerBinding() {
        return serverBinding;
    }
    
    public HandlerInvoker createHandlerInvoker() {
        return new HandlerChainInvoker(getHandlerChain(true)); 
    }
    
    public MessageContext createBindingMessageContext(MessageContext orig) {
        MessageContext bindingCtx = new GenericMessageContext();
        if (null != orig) {
            bindingCtx.putAll(orig);
        }
        return bindingCtx;
    }

    public void marshal(ObjectMessageContext objContext,
                            MessageContext context,
                            DataBindingCallback callback) {
    }
    
    public void marshalFault(ObjectMessageContext objContext,
                            MessageContext context,
                            DataBindingCallback callback) {
    }
    
    public void unmarshal(MessageContext context,
                              ObjectMessageContext objContext,
                              DataBindingCallback callback) {
    }
    
    public void unmarshalFault(MessageContext context,
                                   ObjectMessageContext objContext,
                                   DataBindingCallback callback) {
    }

    public void read(InputStreamMessageContext inContext, MessageContext msgContext) 
        throws IOException {    
    }

    public void write(MessageContext msgContext, OutputStreamMessageContext outContext) 
        throws IOException {       
    }

    public boolean hasFault(MessageContext msgContext) {
        return false;
    }

    public void updateMessageContext(MessageContext msgContext) {
    }
    
    
    
}
