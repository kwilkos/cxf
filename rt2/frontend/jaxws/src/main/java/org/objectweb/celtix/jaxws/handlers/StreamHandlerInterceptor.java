package org.objectweb.celtix.jaxws.handlers;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.Phase;

public class StreamHandlerInterceptor extends AbstractJAXWSHandlerInterceptor {

    public StreamHandlerInterceptor() {
        setPhase(Phase.USER_STREAM);
    }

    @SuppressWarnings("unchecked")
    public void handleMessage(Message message) {
        StreamMessageContextImpl sctx = new StreamMessageContextImpl(message);
        getInvoker(message).invokeStreamHandlers(sctx);
    } 
    
    public void handleFault(Message message) {
    }
    
    
}
