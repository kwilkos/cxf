package org.objectweb.celtix.bus.handlers;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;

/**
 * invoke invoke the handlers in a registered handler chain
 *
 */
public class HandlerChainInvoker {

    private static final Logger LOG = Logger.getLogger(HandlerChainInvoker.class.getPackage().getName());
    
    private final List<Handler> protocolHandlers = new ArrayList<Handler>(); 
    private final List<Handler> logicalHandlers  = new ArrayList<Handler>(); 
    private final List<Handler> invokedHandlers  = new ArrayList<Handler>(); 

    private boolean outbound; 
    private boolean responseExpected; 
    private boolean handlerProcessingAborted; 
    
    public HandlerChainInvoker(List<Handler> hc) {
        LOG.log(Level.FINE, "invoker for chain size: ", hc != null ? hc.size() : 0);
        
        if (hc != null) { 
            for (Handler h : hc) { 
                if (h instanceof LogicalHandler) {                    
                    logicalHandlers.add(h);
                } else { 
                    protocolHandlers.add(h);
                }
            }
        }
        outbound = true;
    }

    public <T extends MessageContext> boolean invokeLogicalHandlers(MessageContext ctx) {        
        List<Handler> handlers = logicalHandlers; 
        // if the last time through, the handler processing was 
        // aborted, then just invoke the handlers that have already
        // been invoked.
        boolean ret = false; 
        if (handlerProcessingAborted) {
            return invokeHandlerChain(invokedHandlers, ctx);
        } else {
            return invokeHandlerChain(logicalHandlers, ctx);
        }
    }
        
    public <T extends MessageContext> boolean invokeProtocolHandlers(T ctx) { 
        
        return invokeHandlerChain(protocolHandlers, ctx);
    }    
    
    
    public boolean invokeStreamHandlers(MessageContext ctx) {
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

    public void mepComplete() {
        // TODO Auto-generated method stub
    }

    List getInvokedHandlers() { 
        return Collections.unmodifiableList(invokedHandlers);
    }
    
    private  boolean invokeHandlerChain(List<Handler> handlerChain, MessageContext ctx) { 
        if (handlerChain.isEmpty()) {
            LOG.log(Level.FINEST, "no handlers registered");        
            return true;
        }
        
        LOG.log(Level.FINE, "invoking handlers, direction: ", outbound ? "outbound" : "inbound");        
        setMessageOutboundProperty(ctx);

        if (!outbound) {
            handlerChain = reverseHandlerChain(handlerChain);
        }
        
        boolean continueProcessing = true; 
        
        for (Handler h : handlerChain) {
            invokedHandlers.add(h);
            continueProcessing = h.handleMessage(ctx);

            if (!continueProcessing) {
                // stop processing handlers, change direction and return 
                // control to the bindng.  Then the binding the will 
                // invoke on the next set on handlers and they will be processing 
                // in the correct direction.  It would be good refactor it and 
                // control all of the processing here.
                changeMessageDirection(ctx); 
                handlerProcessingAborted = true;
                break;
            }
        }      
        return continueProcessing;        
    }    
    
    private <T extends MessageContext> void setMessageOutboundProperty(T ctx) {
        
        Boolean outboundProp = (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outboundProp != null && outboundProp != outbound) {
            LOG.log(Level.WARNING, MessageContext.MESSAGE_OUTBOUND_PROPERTY + " incorrect, setting to " 
                    + outbound);
        }
        ctx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, this.outbound);
    }

    private <T extends MessageContext> void changeMessageDirection(T ctx) { 
        outbound = !outbound;
        setMessageOutboundProperty(ctx);
        ctx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);   

    }
    
    private List<Handler> reverseHandlerChain(List<Handler> handlerChain) {
        List<Handler> reversedHandlerChain = new ArrayList<Handler>();
        reversedHandlerChain.addAll(handlerChain);
        Collections.reverse(reversedHandlerChain);
        return reversedHandlerChain;
    }

}
