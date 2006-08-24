package org.apache.cxf.interceptors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import junit.framework.TestCase;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.invoker.Invoker;

import org.easymock.IMocksControl;

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
        exchange.put(Endpoint.class, endpoint);
        Object input = new Object();
        List<Object> lst = new ArrayList<Object>();
        lst.add(input);
        m.setContent(List.class, lst);
        
        intc.handleMessage(m);
        
        assertTrue(i.invoked);
        
        List<?> list = exchange.getOutMessage().getContent(List.class);
        assertEquals(input, list.get(0));
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
