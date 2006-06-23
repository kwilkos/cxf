package org.objectweb.celtix.bus.context;

import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.MessageContextWrapper;

public class LogicalMessageContextImpl extends MessageContextWrapper implements LogicalMessageContext {

    public LogicalMessageContextImpl(MessageContext wrapped) {
        super(wrapped);
    }
    
    
    public LogicalMessage getMessage() {
        return new LogicalMessageImpl(this);
    }

}
