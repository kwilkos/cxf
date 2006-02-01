package org.objectweb.celtix.bus.ws.rm;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.handlers.SystemHandler;

public class RMHandler implements LogicalHandler<LogicalMessageContext>, SystemHandler {
    
    private RMSource source;
    private RMDestination destination;
    private Configuration configuration;

    public void close(MessageContext context) {
        // TODO commit transaction        
    }

    public boolean handleFault(LogicalMessageContext context) {
        
        open(context);
        return false;
    }

    public boolean handleMessage(LogicalMessageContext context) {
        
        open(context);
        
        return false;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    private void open(LogicalMessageContext context) {
        if (ContextUtils.isOutbound(context)) {
            if (ContextUtils.isRequestor(context) && null == source) {
                source = new RMSource(this);
            }
        } else {
            if (!ContextUtils.isRequestor(context) && null == destination) {
                destination = new RMDestination(this);
            }
        } 
        
        if (null == configuration) {
            // get reference to bus, service and port name from context 
            // and create rm handler configuration
        }
        
        // TODO begin transaction
    }

}
