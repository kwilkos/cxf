package org.objectweb.celtix.bus.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;

public class HandlerChainBuilder {

    /**
     * sorts the handlers into correct order.  All of the logical handlers first
     * followed by the protocol handlers
     * 
     * @param handlers
     * @return sorted list of handlers
     */
    public List<Handler> sortHandlers(List<Handler> handlers) {
        
        List<LogicalHandler> logicalHandlers = new ArrayList<LogicalHandler>(); 
        List<Handler> protocolHandlers = new ArrayList<Handler>(); 
        
        for (Handler handler : handlers) {
            if (handler instanceof LogicalHandler) {
                logicalHandlers.add((LogicalHandler)handler);                
            } else { 
                protocolHandlers.add(handler);
            }
        }
        
        List<Handler> sortedHandlers = new ArrayList<Handler>();
        sortedHandlers.addAll(logicalHandlers);
        sortedHandlers.addAll(protocolHandlers);
        return sortedHandlers;
    }
}
