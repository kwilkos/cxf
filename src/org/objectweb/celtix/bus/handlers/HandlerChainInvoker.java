package org.objectweb.celtix.bus.handlers;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.bus.context.LogicalMessageContextImpl;
import org.objectweb.celtix.bus.context.WebServiceContextImpl;
import org.objectweb.celtix.context.ObjectMessageContext;

/**
 * invoke invoke the handlers in a registered handler chain
 *
 */
public class HandlerChainInvoker {

    private static final Logger LOG = Logger.getLogger(HandlerChainInvoker.class.getPackage().getName());
    
    private final List<Handler> protocolHandlers = new ArrayList<Handler>(); 
    private final List<LogicalHandler> logicalHandlers  = new ArrayList<LogicalHandler>(); 
    private final List<Handler> invokedHandlers  = new ArrayList<Handler>(); 

    private boolean outbound; 
    private boolean responseExpected; 
    private boolean handlerProcessingAborted; 
    private final ObjectMessageContext context; 

    public HandlerChainInvoker(List<Handler> hc, ObjectMessageContext ctx) {
        this(hc, ctx, true);
    } 

    public HandlerChainInvoker(List<Handler> hc, ObjectMessageContext ctx, boolean isOutbound) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "invoker for chain size: ", hc != null ? hc.size() : 0);
        }
        
        if (hc != null) { 
            for (Handler h : hc) { 
                if (h instanceof LogicalHandler) {                    
                    logicalHandlers.add((LogicalHandler)h);
                } else { 
                    protocolHandlers.add(h);
                }
            }
        }
        outbound = isOutbound;
        context = ctx;
    }

    public boolean invokeLogicalHandlers() {        
        // if the last time through, the handler processing was 
        // aborted, then just invoke the handlers that have already
        // been invoked.
        LogicalMessageContextImpl logicalContext = new LogicalMessageContextImpl(context);

        if (handlerProcessingAborted) {
            return invokeHandlerChain(invokedHandlers, logicalContext);
        } else {
            return invokeHandlerChain(logicalHandlers, logicalContext);
        }
    }
        
    public boolean invokeProtocolHandlers() { 
        
        return invokeHandlerChain(protocolHandlers, context);
    }    
    
    
    public boolean invokeStreamHandlers() {
        return true; 
    }
        
    public void closeHandlers() {        
    }
    
    public void responseExpected(boolean expected) {
        responseExpected = expected; 
    }

    public boolean isResponseExpected() {
        return responseExpected;
    }


    public boolean isOutbound() {
        return outbound;
    }    
    
    public boolean isInbound() { 
        return !outbound;
    }
    
    public void setInbound() {
        outbound = false;
    }

    public void setOutbound() {
        outbound = true;
    }


    public void mepComplete() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "closing protocol handlers - handler count:", invokedHandlers.size());
        }
        //invokeClose(reverseHandlerChain(streamHandlers));
        // the handlers must be invoked in the reverse order that they
        // appear in the handler chain.  On the server side this will
        // not be the reverse order in which they were invoked so use
        // the handler chain directly and not simply the
        // invokedHandler list.
        //
        invokeClose(reverseHandlerChain(protocolHandlers));
        invokeClose(reverseHandlerChain(logicalHandlers));
    }


    List getInvokedHandlers() { 
        return Collections.unmodifiableList(invokedHandlers);
    }

    
    private <T extends Handler> void invokeClose(List<T> handlers) {

        for (Handler h : handlers) {
            if (invokedHandlers.contains(h)) {
                h.close(context);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Handler>
    boolean invokeHandlerChain(List<T> handlerChain, MessageContext ctx) { 
        if (handlerChain.isEmpty()) {
            LOG.log(Level.FINEST, "no handlers registered");        
            return true;
        }
        
        LOG.log(Level.FINE, "invoking handlers, direction: ", outbound ? "outbound" : "inbound");        
        setMessageOutboundProperty();

        if (!outbound) {
            handlerChain = reverseHandlerChain(handlerChain);
        }
        
        boolean continueProcessing = true; 
        
        WebServiceContextImpl.setMessageContext(context); 
        for (Handler h : handlerChain) {
            if (!invokedHandlers.contains(h)) { 
                invokedHandlers.add(h);
            }
            continueProcessing = h.handleMessage(ctx);

            if (!continueProcessing) {
                // stop processing handlers, change direction and return 
                // control to the bindng.  Then the binding the will 
                // invoke on the next set on handlers and they will be processing 
                // in the correct direction.  It would be good refactor it and 
                // control all of the processing here.
                changeMessageDirection(); 
                handlerProcessingAborted = true;
                break;
            }
        }      
        return continueProcessing;        
    }    
    
    private void setMessageOutboundProperty() {
        context.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, this.outbound);
    }

    private void changeMessageDirection() { 
        outbound = !outbound;
        setMessageOutboundProperty();
        context.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);   
    }
    
    private <T extends Handler> List<T> reverseHandlerChain(List<T> handlerChain) {
        List<T> reversedHandlerChain = new ArrayList<T>();
        reversedHandlerChain.addAll(handlerChain);
        Collections.reverse(reversedHandlerChain);
        return reversedHandlerChain;
    }
}

