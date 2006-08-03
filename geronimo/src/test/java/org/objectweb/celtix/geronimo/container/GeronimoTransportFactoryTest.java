package org.objectweb.celtix.geronimo.container;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.geronimo.MockBusFactory;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class GeronimoTransportFactoryTest extends TestCase {

    private ThreadAssertionHandler handler = new ThreadAssertionHandler(); 
    
    private CeltixWebServiceContainer container;
    private GeronimoTransportFactory factory = new GeronimoTransportFactory(); 
    private EndpointReferenceType addr; 
    
    public void setUp() throws Exception { 
        container = new CeltixWebServiceContainer(null);
        addr = new EndpointReferenceType();
        AttributedURIType uri = new AttributedURIType(); 
        uri.setValue("http://not.there.iona.com/wibbly/wobbly/wonder");
        addr.setAddress(uri);
        QName serviceName = new QName("http://www.w3.org/2004/08/wsdl", "testServiceName");
        EndpointReferenceUtils.setServiceAndPortName(addr, serviceName, "");
        
        MockBusFactory busFactory = new MockBusFactory();
        Bus mockBus = busFactory.createMockBus();

        Configuration child = 
            busFactory.addChildConfig("http://celtix.objectweb.org/bus/jaxws/endpoint-config", null, null);
        Configuration httpServerCfg = 
            busFactory.addChildConfig("http://celtix.objectweb.org/bus/transports/http/http-server-config",
                null, null, child);
        EasyMock.replay(child);
        EasyMock.replay(httpServerCfg);

        busFactory.replay();

        factory.init(mockBus);
        
    }
    
    
    public void testCreateServerTransport() throws Exception { 

        factory.setCurrentContainer(container);
        ServerTransport st = factory.createServerTransport(addr);
        assertNotNull("factory must not return null transport", st);
        assertTrue("factory must return GeronimoServerTransport", st instanceof GeronimoServerTransport);
        assertSame("CeltixWebServiceContainer must contain server transport", st, 
                   container.getServerTransport());
    }
    
    
    public void testCurrentContainer() throws Exception {
        
        assertSame(null, factory.getCurrentContainer());
        factory.setCurrentContainer(container);
        assertSame(container, factory.getCurrentContainer());
        
        Thread t = new Thread() {
            public void run() {
                CeltixWebServiceContainer cntr1 = new CeltixWebServiceContainer(null);
                assertSame(null, factory.getCurrentContainer());
                factory.setCurrentContainer(cntr1);
                assertSame(cntr1, factory.getCurrentContainer());
            }
        };

        
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        handler.checkAssertions();
    }
    
    static class ThreadAssertionHandler implements Thread.UncaughtExceptionHandler {
        private final Collection<AssertionFailedError> assertions = new LinkedList<AssertionFailedError>();
        
        public ThreadAssertionHandler() {
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
        
        public ThreadAssertionHandler(Thread t) {
            t.setUncaughtExceptionHandler(this);
        }
        
        public void uncaughtException(Thread thread, Throwable ex) {
            if (ex instanceof AssertionFailedError) {
                assertions.add((AssertionFailedError)ex);
            } else {
                throw new RuntimeException("unexpected exception", ex);
            }
        }
        
        public void checkAssertions() {
            for (AssertionFailedError assertion : assertions) {
                // just throw the first one we meet
                throw assertion;
            }
        }
    }
}
