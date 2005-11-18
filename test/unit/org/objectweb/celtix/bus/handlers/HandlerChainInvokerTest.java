package org.objectweb.celtix.bus.handlers;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.bus.context.LogicalMessageContextImpl;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.context.StreamMessageContext;
import org.objectweb.celtix.handlers.StreamHandler;
import org.objectweb.handler_test.PingException;
import org.objectweb.handler_test.types.PingFaultDetails;

public class HandlerChainInvokerTest extends TestCase {
    
    private static final int HANDLER_COUNT = 2; 
    
    HandlerChainInvoker invoker;
    
    ObjectMessageContextImpl ctx = new ObjectMessageContextImpl();
    SOAPMessageContext soapContext;
    
    TestLogicalHandler[] logicalHandlers = new TestLogicalHandler[HANDLER_COUNT];
    TestProtocolHandler[] protocolHandlers = new TestProtocolHandler[HANDLER_COUNT];
    TestStreamHandler[] streamHandlers = new TestStreamHandler[HANDLER_COUNT]; 

    public void setUp() {
        AbstractHandlerBase.clear(); 

        List<Handler> handlers = new ArrayList<Handler>();
        for (int i = 0; i < logicalHandlers.length; i++) {
            logicalHandlers[i] = new TestLogicalHandler();
            handlers.add(logicalHandlers[i]);
        }
        for (int i = 0; i < protocolHandlers.length; i++) {
            protocolHandlers[i] = new TestProtocolHandler();
            handlers.add(protocolHandlers[i]);
        }
        for (int i = 0; i < protocolHandlers.length; i++) {
            streamHandlers[i] = new TestStreamHandler();
            handlers.add(streamHandlers[i]);
        }
        invoker = new HandlerChainInvoker(handlers, ctx);
        
        soapContext = EasyMock.createNiceMock(SOAPMessageContext.class);
    }
    
    public void testInvokeEmptyHandlerChain() {
        invoker = new HandlerChainInvoker(new ArrayList<Handler>(), ctx);
        assertTrue(invoker.invokeLogicalHandlers(false));
        assertTrue(invoker.invokeProtocolHandlers(false, soapContext));
        assertTrue(invoker.invokeStreamHandlers(EasyMock.createMock(InputStreamMessageContext.class)));
    }

    
    public void testHandlerPartitioning() { 
        
        assertEquals(HANDLER_COUNT, invoker.getLogicalHandlers().size());
        for (Handler h : invoker.getLogicalHandlers()) {
            assertTrue(h instanceof LogicalHandler); 
        }

        assertEquals(HANDLER_COUNT, invoker.getStreamHandlers().size());
        for (Handler h : invoker.getStreamHandlers()) {
            assertTrue(h instanceof StreamHandler); 
        }

        assertEquals(HANDLER_COUNT, invoker.getProtocolHandlers().size());
        for (Handler h : invoker.getProtocolHandlers()) {
            assertTrue(!(h instanceof LogicalHandler)); 
        }

    } 

    
    public void testInvokeHandlersOutbound() {

        assertEquals(0, invoker.getInvokedHandlers().size());
        assertTrue(invoker.isOutbound());

        checkLogicalHandlersInvoked(true, false);
        
        assertTrue(invoker.isOutbound());
        assertEquals(2, invoker.getInvokedHandlers().size());
        checkProtocolHandlersInvoked(true);
        assertTrue(invoker.isOutbound());
        assertEquals(4, invoker.getInvokedHandlers().size());
        assertFalse(invoker.isClosed()); 

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

        checkLogicalHandlersInvoked(false, true); 
        assertEquals(4, invoker.getInvokedHandlers().size());
        assertTrue(invoker.isInbound());

        checkStreamHandlersInvoked(false, true); 

        assertFalse(invoker.isClosed()); 
        assertTrue(logicalHandlers[0].getInvokedOrder() > logicalHandlers[1].getInvokedOrder());
        assertTrue(logicalHandlers[1].getInvokedOrder() > protocolHandlers[0].getInvokedOrder());
        assertTrue(protocolHandlers[0].getInvokedOrder() > protocolHandlers[1].getInvokedOrder());
    }
        
    public void testLogicalHandlerOutboundProcessingStoppedResponseExpected() { 

        assertEquals(0, logicalHandlers[0].getHandleMessageCount());
        assertEquals(0, logicalHandlers[1].getHandleMessageCount());

        assertTrue(invoker.isOutbound());
         
        // invoke the handlers.  when a handler returns false, processing
        // of handlers is stopped and message direction is  reversed.
        logicalHandlers[0].setHandleMessageRet(false);        
        boolean ret = invoker.invokeLogicalHandlers(false);
                
        assertEquals(false, ret); 
        assertFalse(invoker.isClosed()); 
        assertEquals(1, logicalHandlers[0].getHandleMessageCount());
        assertEquals(0, logicalHandlers[1].getHandleMessageCount());
        assertTrue(invoker.isInbound());
        
        // the next time invokeHandler is invoked, the 'next' handler is invoked.
        // As message direction has been reversed this means the that the previous
        // one on the list is actually invoked.
        logicalHandlers[0].setHandleMessageRet(true);        
        
        ret = invoker.invokeLogicalHandlers(false);
        assertTrue(ret);
        assertFalse(invoker.isClosed()); 
        assertEquals(1, logicalHandlers[0].getHandleMessageCount());
        assertEquals(0, logicalHandlers[1].getHandleMessageCount());
        assertTrue(invoker.isInbound());
    }
    
    public void testLogicalHandlerInboundProcessingStoppedResponseExpected() { 

        assertEquals(0, logicalHandlers[0].getHandleMessageCount());
        assertEquals(0, logicalHandlers[1].getHandleMessageCount());

        invoker.setInbound();
         
        logicalHandlers[1].setHandleMessageRet(false);        
        boolean ret = invoker.invokeLogicalHandlers(false);
        assertFalse(invoker.isClosed()); 
                
        assertEquals(false, ret); 
        assertEquals(0, logicalHandlers[0].getHandleMessageCount());
        assertEquals(1, logicalHandlers[1].getHandleMessageCount());
        assertTrue(invoker.isOutbound());
    }


    public void testHandleMessageThrowsProtocolException() {

        assertFalse(invoker.faultRaised()); 
        
        ProtocolException pe = new ProtocolException("banzai");
        logicalHandlers[1].setException(pe);

        boolean continueProcessing = invoker.invokeLogicalHandlers(false); 
        assertFalse(continueProcessing);
        assertTrue(invoker.faultRaised()); 

        assertEquals(1, logicalHandlers[0].getHandleMessageCount()); 
        assertEquals(1, logicalHandlers[1].getHandleMessageCount()); 
        continueProcessing = invoker.invokeLogicalHandlers(false);
        assertTrue(continueProcessing);
        assertTrue(invoker.faultRaised()); 
        assertFalse(invoker.isClosed()); 
        assertSame(pe, ctx.getException()); 

        assertEquals(1, logicalHandlers[0].getHandleMessageCount()); 
        assertEquals(1, logicalHandlers[1].getHandleMessageCount()); 
        assertEquals(1, logicalHandlers[0].getHandleFaultCount());
        assertEquals(0, logicalHandlers[1].getHandleFaultCount());

        assertTrue(logicalHandlers[1].getInvokedOrder() 
                   < logicalHandlers[0].getInvokedOrder());
    }


    public void testHandleMessageThrowsRuntimeException() {

        assertFalse(invoker.faultRaised()); 
        
        RuntimeException re = new RuntimeException("banzai");
        logicalHandlers[1].setException(re);

        boolean continueProcessing = invoker.invokeLogicalHandlers(false); 
        assertFalse(continueProcessing);
        assertFalse(invoker.faultRaised()); 
        assertTrue(invoker.isClosed()); 

        assertEquals(1, logicalHandlers[0].getHandleMessageCount()); 
        assertEquals(1, logicalHandlers[1].getHandleMessageCount()); 

        // should this throw exception???
        continueProcessing = invoker.invokeLogicalHandlers(false);
        assertFalse(continueProcessing);

        assertEquals(1, logicalHandlers[0].getHandleMessageCount()); 
        assertEquals(1, logicalHandlers[1].getHandleMessageCount()); 
        assertEquals(0, logicalHandlers[0].getHandleFaultCount());
        assertEquals(0, logicalHandlers[1].getHandleFaultCount());
    }
    

    public void testHandleFault() { 

        // put invoker into fault state
        ProtocolException pe = new ProtocolException("banzai");
        invoker.setFault(pe); 

        boolean continueProcessing = invoker.invokeLogicalHandlers(false);
        assertTrue(continueProcessing); 
        assertEquals(0, logicalHandlers[0].getHandleMessageCount());
        assertEquals(0, logicalHandlers[1].getHandleMessageCount());
        assertEquals(1, logicalHandlers[0].getHandleFaultCount());
        assertEquals(1, logicalHandlers[1].getHandleFaultCount());

        assertTrue(logicalHandlers[0].getInvokedOrder() < logicalHandlers[1].getInvokedOrder());
    } 


    public void testHandleFaultThrowsProtocolException() {

        doHandleFaultExceptionTest(new ProtocolException("banzai"));
    }

    public void testHandleFaultThrowsRuntimeException() {

        doHandleFaultExceptionTest(new RuntimeException("banzai"));
    }


    public void testMEPComplete() { 

        invoker.invokeLogicalHandlers(false); 
        invoker.invokeProtocolHandlers(false, soapContext); 
        invoker.invokeStreamHandlers(EasyMock.createMock(InputStreamMessageContext.class));
        assertEquals(6, invoker.getInvokedHandlers().size()); 

        invoker.mepComplete(); 

        assertTrue("close not invoked on logicalHandlers", logicalHandlers[0].isCloseInvoked()); 
        assertTrue("close not invoked on logicalHandlers", logicalHandlers[1].isCloseInvoked()); 
        assertTrue("close not invoked on protocolHandlers", protocolHandlers[0].isCloseInvoked());
        assertTrue("close not invoked on protocolHandlers", protocolHandlers[1].isCloseInvoked());
        assertTrue("close not invoked on streamHandlers", streamHandlers[0].isCloseInvoked());
        assertTrue("close not invoked on streamHandlers", streamHandlers[1].isCloseInvoked());

        assertTrue("incorrect invocation order of close", protocolHandlers[1].getInvokedOrder() 
                   < protocolHandlers[0].getInvokedOrder());
        assertTrue("incorrect invocation order of close", protocolHandlers[0].getInvokedOrder() 
                   < logicalHandlers[1].getInvokedOrder());
        assertTrue("incorrect invocation order of close", logicalHandlers[1].getInvokedOrder() 
                   < logicalHandlers[0].getInvokedOrder());
    } 


    public void testResponseExpectedDefault() {
        assertTrue(invoker.isResponseExpected());
    }

    public void testFaultRaised() { 

        assertFalse(invoker.faultRaised());
        ctx.setException(new PingException("", new PingFaultDetails()));
        assertTrue(invoker.faultRaised());
        
    } 

    public void testSwitchContext() {

        assertSame(ctx, invoker.getContext());
        ObjectMessageContextImpl newCtx = new ObjectMessageContextImpl(); 
        invoker.setContext(newCtx);
        assertSame(newCtx, invoker.getContext());
    } 

    
    /* test invoking logical handlers when processing has been aborted
     * with both protocol and logical handlers in invokedHandlers list.
     *
     */
    public void testInvokedAlreadyInvokedMixed() { 

        // simulate an invocation being aborted by a logical handler
        //
        logicalHandlers[1].setHandleMessageRet(false);        
        invoker.setInbound();
        invoker.invokeProtocolHandlers(true, soapContext); 
        invoker.invokeLogicalHandlers(true); 

        assertEquals(2, invoker.getInvokedHandlers().size());
        assertTrue(!invoker.getInvokedHandlers().contains(logicalHandlers[1]));
        assertTrue(invoker.getInvokedHandlers().contains(protocolHandlers[0]));
        assertTrue(invoker.getInvokedHandlers().contains(protocolHandlers[1]));
        assertEquals(0, logicalHandlers[0].getHandleMessageCount()); 
        assertEquals(1, logicalHandlers[1].getHandleMessageCount()); 
        assertEquals(1, protocolHandlers[0].getHandleMessageCount()); 

        assertEquals(1, protocolHandlers[1].getHandleMessageCount()); 

        // now, invoke handlers on outbound leg
        invoker.invokeLogicalHandlers(true); 

        assertEquals(1, logicalHandlers[1].getHandleMessageCount()); 
        assertEquals(0, logicalHandlers[0].getHandleMessageCount()); 
        assertEquals(1, protocolHandlers[0].getHandleMessageCount()); 
        assertEquals(1, protocolHandlers[1].getHandleMessageCount()); 

    }
    
    protected void checkLogicalHandlersInvoked(boolean outboundProperty, boolean requestorProperty) { 

        invoker.invokeLogicalHandlers(requestorProperty);

        assertNotNull(ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        assertEquals(outboundProperty, ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        assertNotNull(ctx.get(ObjectMessageContext.REQUESTOR_ROLE_PROPERTY));
        assertEquals(requestorProperty, ctx.get(ObjectMessageContext.REQUESTOR_ROLE_PROPERTY));
        assertTrue("handler not invoked", logicalHandlers[0].isHandleMessageInvoked());
        assertTrue("handler not invoked", logicalHandlers[1].isHandleMessageInvoked());
        assertTrue(invoker.getInvokedHandlers().contains(logicalHandlers[0])); 
        assertTrue(invoker.getInvokedHandlers().contains(logicalHandlers[1])); 
    }
    
    protected void checkProtocolHandlersInvoked(boolean outboundProperty) { 

        soapContext.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, invoker.isOutbound());
        EasyMock.expectLastCall().andReturn(null); 
        EasyMock.replay(soapContext); 

        invoker.invokeProtocolHandlers(false, soapContext);

        EasyMock.verify(soapContext); 

        assertTrue("handler not invoked", protocolHandlers[0].isHandleMessageInvoked());
        assertTrue("handler not invoked", protocolHandlers[1].isHandleMessageInvoked());

        assertTrue(invoker.getInvokedHandlers().contains(protocolHandlers[0])); 
        assertTrue(invoker.getInvokedHandlers().contains(protocolHandlers[1])); 
    }
    
    protected void checkStreamHandlersInvoked(boolean outboundProperty, boolean requestorProperty) { 

        invoker.invokeStreamHandlers(EasyMock.createMock(InputStreamMessageContext.class));
                 
        assertNotNull(ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        assertEquals(outboundProperty, ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        assertNotNull(ctx.get(ObjectMessageContext.REQUESTOR_ROLE_PROPERTY));
        assertEquals(requestorProperty, ctx.get(ObjectMessageContext.REQUESTOR_ROLE_PROPERTY));
        assertTrue("handler not invoked", streamHandlers[0].isHandleMessageInvoked());
        assertTrue("handler not invoked", streamHandlers[1].isHandleMessageInvoked());
        assertTrue(invoker.getInvokedHandlers().contains(streamHandlers[0])); 
        assertTrue(invoker.getInvokedHandlers().contains(streamHandlers[1])); 
    }
    
    private void doHandleFaultExceptionTest(RuntimeException e) { 

        // put invoker into fault state
        ProtocolException pe = new ProtocolException("banzai");
        invoker.setFault(pe); 

        // throw exception during handleFault processing
        logicalHandlers[0].setException(e);
        boolean continueProcessing = invoker.invokeLogicalHandlers(false);
        assertFalse(continueProcessing); 
        assertTrue(invoker.isClosed()); 
        assertEquals(1, logicalHandlers[0].getHandleFaultCount());
        assertEquals(0, logicalHandlers[1].getHandleFaultCount());
    } 


    static class TestStreamHandler extends AbstractHandlerBase<StreamMessageContext> 
        implements StreamHandler {

    }


    static class TestProtocolHandler extends AbstractHandlerBase<SOAPMessageContext> {
        
    }
    
    

    static class TestLogicalHandler extends AbstractHandlerBase<LogicalMessageContextImpl>
        implements LogicalHandler<LogicalMessageContextImpl> {
        
    }    
    
    static class AbstractHandlerBase<T extends MessageContext> implements Handler<T> {
        
        private static int sinvokedOrder; 
        private static int sid; 
       
        private int invokeOrder; 
        private final int id = ++sid; 
        
        private int handleMessageInvoked;
        private int handleFaultInvoked;
        private boolean handleMessageRet = true; 
        private final boolean handleFaultRet = true; 
        private RuntimeException exception; 

        private int closeInvoked; 

        public void reset() {
            handleMessageInvoked = 0; 
            handleFaultInvoked = 0; 
            handleMessageRet = true; 
        }
        
        public boolean handleMessage(T arg0) {
            invokeOrder = ++sinvokedOrder; 
            handleMessageInvoked++;

            if (exception != null) {
                RuntimeException e = exception;
                exception = null; 
                throw e;
            }

            return handleMessageRet;
        }

        public boolean handleFault(T arg0) {
            invokeOrder = ++sinvokedOrder; 
            handleFaultInvoked++; 

            if (exception != null) {
                throw exception;
            }

            return handleFaultRet;
        }

        public void close(MessageContext arg0) {
            invokeOrder = ++sinvokedOrder; 
            closeInvoked++;
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
        
        public int getHandleFaultCount() {
            return handleFaultInvoked;
        }

        public boolean isHandleMessageInvoked() {
            return handleMessageInvoked > 0;
        }

        public boolean isCloseInvoked() { 
            return closeInvoked > 0; 
        } 

        public int getCloseCount() { 
            return closeInvoked;
        } 

        public int getInvokedOrder() {
            return invokeOrder;            
        }
        
        public void setHandleMessageRet(boolean ret) {
            handleMessageRet = ret; 
        }
        
        public void setHandleFaultRet(boolean ret) {
            //handleFaultRet = ret; 
        }

        public String toString() { 
            return "[" + super.toString() + " id: " + id + " invoke order: " + invokeOrder + "]";
        }

        
        public void setException(RuntimeException rte) { 
            exception = rte;
        } 

        public static void clear() { 
            sinvokedOrder = 0; 
            sid = 0; 
        } 
    }
}
