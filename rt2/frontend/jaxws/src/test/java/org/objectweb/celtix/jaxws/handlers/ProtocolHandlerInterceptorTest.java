package org.objectweb.celtix.jaxws.handlers;

import junit.framework.TestCase;

import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.message.Message;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceControl;


public class ProtocolHandlerInterceptorTest extends TestCase {
    
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
        ProtocolHandlerInterceptor pi = new ProtocolHandlerInterceptor(invoker);
        pi.intercept(message);
    }
    
    public void testInterceptFailure() {
        expect(invoker.invokeProtocolHandlers(true, message)).andReturn(false);
        control.replay();
        ProtocolHandlerInterceptor pi = new ProtocolHandlerInterceptor(invoker);
        pi.intercept(message);  
    }
}
