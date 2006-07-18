package org.objectweb.celtix.jaxws.handlers;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.message.AbstractWrappedMessage;
import org.objectweb.celtix.message.Message;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createNiceControl;


public class AbstractProtocolHandlerInterceptorTest extends TestCase {
    
    private IMocksControl control;
    private HandlerChainInvoker invoker;
    private Message message;
    
    public void setUp() {
        control = createNiceControl();
        invoker = control.createMock(HandlerChainInvoker.class);
        message = control.createMock(IIOPMessage.class);
    }
    
    public void tearDown() {
        control.verify();
    }

    public void testInterceptSuccess() {
        expect(invoker.invokeProtocolHandlers(eq(true), isA(MessageContext.class))).andReturn(true);
        control.replay();
        IIOPHandlerInterceptor pi = new IIOPHandlerInterceptor(invoker);
        assertEquals("unexpected phase", "user-protocol", pi.getPhase());
        pi.handleMessage(message);
    }
    
    public void testInterceptFailure() {
        expect(invoker.invokeProtocolHandlers(eq(true), isA(MessageContext.class))).andReturn(false);
        control.replay();
        IIOPHandlerInterceptor pi = new IIOPHandlerInterceptor(invoker);
        pi.handleMessage(message);  
    }

    class IIOPMessage extends AbstractWrappedMessage {
        public IIOPMessage(Message m) {
            super(m);
        }
    }
    
    interface IIOPMessageContext extends MessageContext {
        
    }
     
    interface IIOPHandler<T extends IIOPMessageContext> extends Handler {
        
    }
    
    class IIOPHandlerInterceptor extends AbstractProtocolHandlerInterceptor {

        IIOPHandlerInterceptor(HandlerChainInvoker i) {
            super(i);
        }        
        
    }
    
    
}
