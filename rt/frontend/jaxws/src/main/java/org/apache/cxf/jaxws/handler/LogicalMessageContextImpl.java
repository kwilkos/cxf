package org.apache.cxf.jaxws.handler;

import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalMessageContext;

import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;


public class LogicalMessageContextImpl extends WrappedMessageContext implements LogicalMessageContext {

    public LogicalMessageContextImpl(Message wrapped) {
        super(wrapped);
    }
      
    public LogicalMessage getMessage() {
        return new LogicalMessageImpl(this);
    }

}
