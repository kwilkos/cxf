package org.objectweb.celtix.jaxws.handlers;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.message.Message;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceControl;


public class AbstractProtocolHandlerInterceptorTest extends TestCase {
    
    private IMocksControl control;
    private HandlerChainInvoker invoker;
    private Message message;
    
    public void setUp() {
        control = createNiceControl();
        invoker = control.createMock(HandlerChainInvoker.class);
        message = control.createMock(Message.class);
    }
    
    public void tearDown() {
        control.verify();
    }

    public void testInterceptSuccess() {
        expect(invoker.invokeProtocolHandlers(true, message)).andReturn(true);
        control.replay();
        AbstractProtocolHandlerInterceptor pi = new IIOPHandlerInterceptor(invoker);
        pi.intercept(message);
    }
    
    public void testInterceptFailure() {
        expect(invoker.invokeProtocolHandlers(true, message)).andReturn(false);
        control.replay();
        AbstractProtocolHandlerInterceptor pi = new IIOPHandlerInterceptor(invoker);
        pi.intercept(message);  
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
