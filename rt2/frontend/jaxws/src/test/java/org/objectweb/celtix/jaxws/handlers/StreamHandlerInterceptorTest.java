package org.objectweb.celtix.jaxws.handlers;

import junit.framework.TestCase;

import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.Message;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createNiceControl;


public class StreamHandlerInterceptorTest extends TestCase {
    
    private IMocksControl control;
    private HandlerChainInvoker invoker;
    private Message message;
    private Exchange exchange;
    
    public void setUp() {
        control = createNiceControl();
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
        expect(invoker.invokeStreamHandlers(isA(StreamMessageContext.class))).andReturn(true);
        control.replay();
        StreamHandlerInterceptor si = new StreamHandlerInterceptor();
        assertEquals("unexpected phase", "user-stream", si.getPhase());
        si.handleMessage(message);
    }
    
    public void testInterceptFailure() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(AbstractProtocolHandlerInterceptor.HANDLER_CHAIN_INVOKER)).andReturn(invoker);
        expect(invoker.invokeStreamHandlers(isA(StreamMessageContext.class))).andReturn(false);
        control.replay();
        StreamHandlerInterceptor si = new StreamHandlerInterceptor();
        si.handleMessage(message); 
    }
}
