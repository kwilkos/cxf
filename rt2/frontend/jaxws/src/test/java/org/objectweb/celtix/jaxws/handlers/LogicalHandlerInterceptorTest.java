package org.objectweb.celtix.jaxws.handlers;

import junit.framework.TestCase;

import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.message.Message;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createNiceControl;


public class LogicalHandlerInterceptorTest extends TestCase {
    
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
        expect(invoker.invokeLogicalHandlers(true, message)).andReturn(true);
        control.replay();
        LogicalHandlerInterceptor li = new LogicalHandlerInterceptor(invoker);
        li.handleMessage(message);
    }
    
    public void testInterceptFailure() {
        expect(invoker.invokeLogicalHandlers(true, message)).andReturn(false);
        control.replay();
        LogicalHandlerInterceptor li = new LogicalHandlerInterceptor(invoker);
        li.handleMessage(message);   
    }
    
    public void testOnCompletion() {
        invoker.mepComplete(message);
        expectLastCall();
        control.replay();
        LogicalHandlerInterceptor li = new LogicalHandlerInterceptor(invoker);
        li.onCompletion(message);
    }
}
