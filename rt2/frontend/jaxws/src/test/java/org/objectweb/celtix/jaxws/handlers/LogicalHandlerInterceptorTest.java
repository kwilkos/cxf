package org.objectweb.celtix.jaxws.handlers;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.LogicalMessageContext;

import junit.framework.TestCase;

import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.Message;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createNiceControl;


public class LogicalHandlerInterceptorTest extends TestCase {
    
    private IMocksControl control;
    private Binding binding;
    private HandlerChainInvoker invoker;
    private Message message;
    private Exchange exchange;
    
    public void setUp() {
        control = createNiceControl();
        binding = control.createMock(Binding.class);
        invoker = control.createMock(HandlerChainInvoker.class);
        message = control.createMock(Message.class);
        exchange = control.createMock(Exchange.class);        
    }
    
    public void tearDown() {
        control.verify();
    }
    
    public void testInterceptSuccess() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(AbstractProtocolHandlerInterceptor.HANDLER_CHAIN_INVOKER)).andReturn(invoker);
        expect(invoker.invokeLogicalHandlers(eq(true),
            isA(LogicalMessageContext.class))).andReturn(true);
        control.replay();
        LogicalHandlerInterceptor<Message> li = new LogicalHandlerInterceptor<Message>(binding);
        assertEquals("unexpected phase", "user-logical", li.getPhase());
        li.handleMessage(message);
    }
    
    public void testInterceptFailure() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(AbstractProtocolHandlerInterceptor.HANDLER_CHAIN_INVOKER)).andReturn(invoker);
        expect(invoker.invokeLogicalHandlers(eq(true), 
            isA(LogicalMessageContext.class))).andReturn(false);
        control.replay();
        LogicalHandlerInterceptor<Message> li = new LogicalHandlerInterceptor<Message>(binding);
        li.handleMessage(message);   
    }
    
    public void testOnCompletion() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(AbstractProtocolHandlerInterceptor.HANDLER_CHAIN_INVOKER)).andReturn(invoker);
        invoker.mepComplete(message);
        expectLastCall();
        control.replay();
        LogicalHandlerInterceptor<Message> li = new LogicalHandlerInterceptor<Message>(binding);
        li.onCompletion(message);
    }
}
