package org.apache.cxf.jaxws.handlers;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.easymock.classextension.IMocksControl;
import org.apache.cxf.message.AbstractWrappedMessage;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createNiceControl;


public class AbstractProtocolHandlerInterceptorTest extends TestCase {
    
    private IMocksControl control;
    private Binding binding;
    private HandlerChainInvoker invoker;
    private IIOPMessage message;
    private Exchange exchange;
    
    public void setUp() {
        control = createNiceControl();
        invoker = control.createMock(HandlerChainInvoker.class);
        message = control.createMock(IIOPMessage.class);
        exchange = control.createMock(Exchange.class);
        
    }
    
    public void tearDown() {
        control.verify();
    }

    public void testInterceptSuccess() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(AbstractProtocolHandlerInterceptor.HANDLER_CHAIN_INVOKER)).andReturn(invoker);
        expect(invoker.invokeProtocolHandlers(eq(true), isA(MessageContext.class))).andReturn(true);
        control.replay();
        IIOPHandlerInterceptor pi = new IIOPHandlerInterceptor(binding);
        assertEquals("unexpected phase", "user-protocol", pi.getPhase());
        pi.handleMessage(message);
    }
    
    public void testInterceptFailure() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(AbstractProtocolHandlerInterceptor.HANDLER_CHAIN_INVOKER)).andReturn(invoker);
        expect(invoker.invokeProtocolHandlers(eq(true), isA(MessageContext.class))).andReturn(false);
        control.replay();
        IIOPHandlerInterceptor pi = new IIOPHandlerInterceptor(binding);
        pi.handleMessage(message);  
    }

    class IIOPMessage extends AbstractWrappedMessage {
        public IIOPMessage(Message m) {
            super(m);
        }
    }
    
    interface IIOPMessageContext extends MessageContext {
        
    }
     
    interface IIOPHandler<T extends IIOPMessageContext> extends Handler<IIOPMessageContext> {
        
    }
    
    class IIOPHandlerInterceptor extends AbstractProtocolHandlerInterceptor<IIOPMessage> {
        IIOPHandlerInterceptor(Binding binding) {
            super(binding);
        }
    }
    
    
}
