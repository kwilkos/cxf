package org.objectweb.celtix.jaxws.handlers;

import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalMessageContext;
import org.objectweb.celtix.jaxws.context.WrappedMessageContext;
import org.objectweb.celtix.message.Message;


public class LogicalMessageContextImpl extends WrappedMessageContext implements LogicalMessageContext {

    public LogicalMessageContextImpl(Message wrapped) {
        super(wrapped);
    }
      
    public LogicalMessage getMessage() {
        return new LogicalMessageImpl(this);
    }

}
