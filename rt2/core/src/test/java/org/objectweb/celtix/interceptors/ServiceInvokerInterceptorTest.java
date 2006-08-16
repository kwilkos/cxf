package org.objectweb.celtix.interceptors;

import java.util.concurrent.Executor;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeConstants;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.service.ServiceImpl;
import org.objectweb.celtix.service.invoker.Invoker;

import static org.easymock.EasyMock.createNiceControl;
import static org.easymock.EasyMock.expect;

public class ServiceInvokerInterceptorTest extends TestCase {
    public void testInterceptor() throws Exception {
        ServiceInvokerInterceptor intc = new ServiceInvokerInterceptor();
        
        MessageImpl m = new MessageImpl();
        Exchange exchange = new ExchangeImpl();
        m.setExchange(exchange);
        exchange.setInMessage(m);
        
        exchange.setOutMessage(new MessageImpl());
        
        TestInvoker i = new TestInvoker();
        Endpoint endpoint = createEndpoint(i);
        exchange.put(ExchangeConstants.ENDPOINT, endpoint);
        Object input = new Object();
        m.setContent(Object.class, input);
        
        intc.handleMessage(m);
        
        assertTrue(i.invoked);
        
        Object object = exchange.getOutMessage().getContent(Object.class);
        assertEquals(input, object);
    }
    
    Endpoint createEndpoint(Invoker i) throws Exception {
        IMocksControl control = createNiceControl();
        Endpoint endpoint = control.createMock(Endpoint.class);

        ServiceImpl service = new ServiceImpl(null);
        service.setInvoker(i);
        service.setExecutor(new SimpleExecutor());
        expect(endpoint.getService()).andReturn(service).anyTimes();
        
        control.replay();

        return endpoint;
    }
    
    static class TestInvoker implements Invoker {
        boolean invoked;
        public Object invoke(Exchange exchange, Object o) {
            invoked = true;
            assertNotNull(exchange);
            assertNotNull(o);
            return o;
        }
    }
    
    static class SimpleExecutor implements Executor {

        public void execute(Runnable command) {
            command.run();
        }
        
    }
}
