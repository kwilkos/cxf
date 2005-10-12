package org.objectweb.celtix.bus.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.context.LogicalMessageContextImpl;
import org.objectweb.celtix.context.GenericMessageContext;

public class HandlerChainInvokerTest extends TestCase {
    
    private static final int HANDLER_COUNT = 2; 
    
    HandlerChainInvoker invoker;
    
    LogicalMessageContextImpl ctx = new LogicalMessageContextImpl(new GenericMessageContext());

    
    TestLogicalHandler[] logicalHandlers = new TestLogicalHandler[HANDLER_COUNT];
    TestProtocolHandler[] protocolHandlers = new TestProtocolHandler[HANDLER_COUNT];
    
    public void setUp() {
        List<Handler> handlers = new ArrayList<Handler>();
        for (int i = 0; i < logicalHandlers.length; i++) {
            logicalHandlers[i] = new TestLogicalHandler();
            handlers.add(logicalHandlers[i]);
        }
        for (int i = 0; i < protocolHandlers.length; i++) {
            protocolHandlers[i] = new TestProtocolHandler();
            handlers.add(protocolHandlers[i]);
        }
        invoker = new HandlerChainInvoker(handlers);
    }
    
    public void testInvokeEmptyHandlerChain() {
        invoker = new HandlerChainInvoker(new ArrayList<Handler>());
        assertTrue(invoker.invokeLogicalHandlers(ctx));
        assertTrue(invoker.invokeProtocolHandlers(ctx));
        assertTrue(invoker.invokeStreamHandlers(ctx));
    }
    
    public void testInvokeHandlersOutbound() {

        assertEquals(0, invoker.getInvokedHandlers().size());
        assertTrue(invoker.isOutbound());

        checkLogicalHandlersInvoked(true);
        assertTrue(invoker.isOutbound());
        assertEquals(2, invoker.getInvokedHandlers().size());
        checkProtocolHandlersInvoked(true);
        assertTrue(invoker.isOutbound());
        assertEquals(4, invoker.getInvokedHandlers().size());

        assertTrue(logicalHandlers[0].getInvokedOrder() < logicalHandlers[1].getInvokedOrder());
        assertTrue(logicalHandlers[1].getInvokedOrder() < protocolHandlers[0].getInvokedOrder());
        assertTrue(protocolHandlers[0].getInvokedOrder() < protocolHandlers[1].getInvokedOrder());
    }

    public void testInvokeHandlersInbound() {

        invoker.setInbound();
        assertTrue(invoker.isInbound());
        checkProtocolHandlersInvoked(false);
        assertEquals(2, invoker.getInvokedHandlers().size());
        assertTrue(invoker.isInbound());
        checkLogicalHandlersInvoked(false); 
        assertEquals(4, invoker.getInvokedHandlers().size());
        assertTrue(invoker.isInbound());
        
        assertTrue(logicalHandlers[0].getInvokedOrder() > logicalHandlers[1].getInvokedOrder());
        assertTrue(logicalHandlers[1].getInvokedOrder() > protocolHandlers[0].getInvokedOrder());
        assertTrue(protocolHandlers[0].getInvokedOrder() > protocolHandlers[1].getInvokedOrder());
    }

    public void testLogicalHandlerOutboundProcessingStoppedResponseExpected() { 

        assertEquals(0, logicalHandlers[0].getHandleMessageCount());
        assertEquals(0, logicalHandlers[1].getHandleMessageCount());

        assertTrue(invoker.isOutbound());
        invoker.responseExpected(true);
         
        // invoke the handlers.  when a handler returns false, processing
        // of handlers is stopped and message direction is  reversed.
        //
        logicalHandlers[0].setHandleMessageRet(false);        
        boolean ret = invoker.invokeLogicalHandlers(ctx);
                
        assertEquals(false, ret); 
        assertEquals(1, logicalHandlers[0].getHandleMessageCount());
        assertEquals(0, logicalHandlers[1].getHandleMessageCount());
        assertTrue(invoker.isInbound());
        
        // the next time invokeHandler is invoked, the 'next' handler is invoked.
        // As message direction has been reversed this means the that the previous
        // one on the list is actually invoked.
        logicalHandlers[0].setHandleMessageRet(true);        
        
        ret = invoker.invokeLogicalHandlers(ctx);
        assertTrue(ret);
        assertEquals(2, logicalHandlers[0].getHandleMessageCount());
        assertEquals(0, logicalHandlers[1].getHandleMessageCount());
        assertTrue(invoker.isInbound());
        
    }
    
    public void testLogicalHandlerInboundProcessingStoppedResponseExpected() { 

        assertEquals(0, logicalHandlers[0].getHandleMessageCount());
        assertEquals(0, logicalHandlers[1].getHandleMessageCount());

        invoker.setInbound();
        invoker.responseExpected(true);
         
        logicalHandlers[1].setHandleMessageRet(false);        
        boolean ret = invoker.invokeLogicalHandlers(ctx);
                
        assertEquals(false, ret); 
        assertEquals(0, logicalHandlers[0].getHandleMessageCount());
        assertEquals(1, logicalHandlers[1].getHandleMessageCount());
        assertTrue(invoker.isOutbound());
    }

    
    
    protected void checkLogicalHandlersInvoked(boolean outboundProperty) { 

        invoker.invokeLogicalHandlers(ctx);

        assertNotNull(ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        assertEquals(outboundProperty, ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        assertTrue("handler not invoked", logicalHandlers[0].isHandleMessageInvoked());
        assertTrue("handler not invoked", logicalHandlers[1].isHandleMessageInvoked());
        assertTrue(invoker.getInvokedHandlers().contains(logicalHandlers[0])); 
        assertTrue(invoker.getInvokedHandlers().contains(logicalHandlers[1])); 
    }
    
    protected void checkProtocolHandlersInvoked(boolean outboundProperty) { 
        
        invoker.invokeProtocolHandlers(ctx);
        
        assertTrue("handler not invoked", protocolHandlers[0].isHandleMessageInvoked());
        assertTrue("handler not invoked", protocolHandlers[1].isHandleMessageInvoked());
        assertTrue(invoker.getInvokedHandlers().contains(protocolHandlers[0])); 
        assertTrue(invoker.getInvokedHandlers().contains(protocolHandlers[1])); 
    }
    
    
    
    static class TestProtocolHandler extends AbstractHandlerBase {
        
    }
    
    

    static class  TestLogicalHandler extends AbstractHandlerBase<LogicalMessageContextImpl>
        implements LogicalHandler<LogicalMessageContextImpl> {
        
    }    
    
    static class AbstractHandlerBase<T extends MessageContext> implements Handler<T> {
        
        private static int sinvokedOrder; 
        
        private int invokeOrder; 
        
        private int handleMessageInvoked;
        private boolean handleMessageRet = true; 
        
        public void reset() {
            handleMessageInvoked = 0; 
            handleMessageRet = true; 
        }
        
        public boolean handleMessage(T arg0) {
            invokeOrder = ++sinvokedOrder; 
            
            handleMessageInvoked++;
            return handleMessageRet;
        }

        public boolean handleFault(T arg0) {
            // TODO Auto-generated method stub
            return false;
        }

        public void close(MessageContext arg0) {
            // TODO Auto-generated method stub
            
        }

        
        public void init(Map<String, Object> arg0) {
            // TODO Auto-generated method stub
            
        }

        
        public void destroy() {
            // TODO Auto-generated method stub
            
        }

        public int getHandleMessageCount() {
            return handleMessageInvoked;
        }
        
        public boolean isHandleMessageInvoked() {
            return handleMessageInvoked > 0;
        }

        public int getInvokedOrder() {
            return invokeOrder;            
        }
        
        public void setHandleMessageRet(boolean ret) {
            handleMessageRet = ret; 
        }
       
    }
}
