package org.objectweb.celtix.bus.bindings;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointFactory;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bus.EndpointImpl;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.hello_world_soap_http.CorrectlyAnnotatedGreeterImpl;

public class ServerBindingTest extends TestCase {

    private String epfClassName;
    private Bus bus;
    private EndpointImpl ei;
    private CorrectlyAnnotatedGreeterImpl implementor;

    public void setUp() throws Exception {
        epfClassName = System.getProperty(EndpointFactory.ENDPOINTFACTORY_PROPERTY);
        System.setProperty(EndpointFactory.ENDPOINTFACTORY_PROPERTY, 
                           "org.objectweb.celtix.bus.EndpointFactoryImpl");
        bus = Bus.init();
        BindingManager bm = bus.getBindingManager();
        bm.registerBinding("http://celtix.objectweb.org/bindings/test", new TestBindingFactory(bus));
        EndpointFactory epf = EndpointFactory.newInstance();
        implementor = new CorrectlyAnnotatedGreeterImpl();
        Endpoint ep = epf.createEndpoint(new URI(TestBinding.TEST_BINDING), implementor);
        ei = (EndpointImpl)ep;
    }

    public void tearDown() throws Exception {
        bus.shutdown(true);
        if (null == epfClassName) {
            Properties properties = System.getProperties();
            properties.remove(EndpointFactory.ENDPOINTFACTORY_PROPERTY);
            System.setProperties(properties);
        } else {
            System.setProperty(EndpointFactory.ENDPOINTFACTORY_PROPERTY, epfClassName);
        }
    }

    public void testConstruction() {
        ServerBinding sb = ei.getServerBinding();
        assertNotNull(sb);
        assertTrue(sb instanceof TestServerBinding);
        Binding b = sb.getBinding();
        assertNotNull(b);
        Endpoint ep = sb.getEndpoint();
        assertTrue(ei == ep);
        TestServerBinding tsb = (TestServerBinding)sb;
        ServerTransport st = tsb.getTransport();
        assertNull(st);
        EndpointReferenceType ref = ei.getEndpointReferenceType();
        assertNull(ref.getAddress());

    }

    public void testActivate() throws Exception {
        ServerBinding sb = ei.getServerBinding();
        try {
            sb.activate();
        } catch (NullPointerException ex) {
            // ignore
        }
        EndpointReferenceType ref = ei.getEndpointReferenceType();
        EndpointReferenceUtils.setAddress(ref, "test://localhost:7777/test");
        sb.activate();
        TestServerBinding tsb = (TestServerBinding)sb;
        ServerTransport st = tsb.getTransport();
        assertNotNull(st);

    }

    public void testDispatch() throws Exception {
        ServerBinding sb = ei.getServerBinding();
        EndpointReferenceType ref = ei.getEndpointReferenceType();
        EndpointReferenceUtils.setAddress(ref, "test://localhost:7777/test");
        sb.activate();
        TestServerBinding tsb = (TestServerBinding)sb;
        ei.setExecutor(new Executor() {
            public void execute(Runnable command) {
                command.run();            
            }
        });

        // simulate transport events
        // REVISIT: exception handling, see comment in AbstractServerBinding

        tsb.triggerTransport(); 
        assertEquals(0, implementor.getInvocationCount("sayHi"));
        assertEquals(0, implementor.getInvocationCount("greetMe"));
        
        tsb.currentOperation = "sayHi";
        tsb.triggerTransport();
        assertEquals(1, implementor.getInvocationCount("sayHi"));
        assertEquals(0, implementor.getInvocationCount("greetMe"));

    }

}
