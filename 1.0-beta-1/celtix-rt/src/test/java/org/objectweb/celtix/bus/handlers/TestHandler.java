package org.objectweb.celtix.bus.handlers;

import java.util.Map;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

public class TestHandler<T extends LogicalMessageContext> implements LogicalHandler<T> {

    public void close(MessageContext arg0) {
    }

    public void destroy() {
    }

    public boolean handleFault(T arg0) {
        return false;
    }

    public boolean handleMessage(T arg0) {
        return false;
    }

    public void init(Map<String, Object> arg0) {        
    }

}
