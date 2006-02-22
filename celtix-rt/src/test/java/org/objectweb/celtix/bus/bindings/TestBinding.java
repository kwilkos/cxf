package org.objectweb.celtix.bus.bindings;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bus.handlers.HandlerChainInvoker;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;

public class TestBinding  extends AbstractBindingImpl {  
    public static final String TEST_BINDING = "http://celtix.objectweb.org/bindings/test";
    
    public HandlerInvoker createHandlerInvoker() {
        return new HandlerChainInvoker(getHandlerChain(true)); 
    }
    
    public MessageContext createBindingMessageContext(MessageContext orig) {
        return new GenericMessageContext();
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

    public void updateMessageContext(MessageContext msgContext) 
        throws IOException {
    }
    
    
    
}
