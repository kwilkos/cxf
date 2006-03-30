package org.objectweb.celtix.systest.ws.rm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bus.context.LogicalMessageContextImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.context.GenericMessageContext;

public class LogicalMessageContextRecorder implements LogicalHandler<LogicalMessageContext> {
    
    private static List<LogicalMessageContext> inbound;
    
    public LogicalMessageContextRecorder() {
        if (null == inbound) {
            inbound = new ArrayList<LogicalMessageContext>();
        }
    }
    
    public void init(Map<String, Object> map) {
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(LogicalMessageContext context) {
        record(context);
        return true;
    }

    public boolean handleFault(LogicalMessageContext context) {
        record(context);
        return true;
    }

    public void close(MessageContext arg0) {
    }
    
    protected List<LogicalMessageContext> getInboundContexts() {
        return inbound;
    }

    private void record(LogicalMessageContext context) {        
        if (!ContextUtils.isOutbound(context)) {  
            GenericMessageContext clone = new GenericMessageContext();
            clone.putAll(context);
            inbound.add(new LogicalMessageContextImpl(clone));
        }
    } 

}
