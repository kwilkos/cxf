package org.objectweb.celtix.bus;

import java.net.URI;
import java.util.Properties;

import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointFactory;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bus.bindings.TestBinding;
import org.objectweb.celtix.bus.bindings.TestBindingFactory;
import org.objectweb.hello_world_soap_http.CorrectlyAnnotatedGreeterImpl;

public class EndpointImplTest extends TestCase {
    private String epfClassName;
    private Bus bus;
    private Endpoint endpoint;

    public void setUp() throws Exception {
        epfClassName = System.getProperty(EndpointFactory.ENDPOINTFACTORY_PROPERTY);
        System.setProperty(EndpointFactory.ENDPOINTFACTORY_PROPERTY,
                           "org.objectweb.celtix.bus.EndpointFactoryImpl");
        bus = Bus.init();
        BindingManager bm = bus.getBindingManager();
        bm.registerBinding(TestBinding.TEST_BINDING, new TestBindingFactory(bus));
        EndpointFactory epf = EndpointFactory.newInstance();
        endpoint = epf.createEndpoint(new URI(TestBinding.TEST_BINDING), new CorrectlyAnnotatedGreeterImpl());

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

    public void testPublishUsingAddress() throws Exception {
        assertNotNull(endpoint);
        assertTrue(!endpoint.isPublished());
        String address = "http://localhost:8080/test";
        try {
            endpoint.publish(address);
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getCause() instanceof BusException);
            assertEquals("BINDING_INCOMPATIBLE_ADDRESS", ((BusException)ex.getCause()).getCode());
        }
        address = "test://localhost:7777/test";
        endpoint.publish(address);
        assertTrue(endpoint.isPublished());
        endpoint.stop();
        assertTrue(!endpoint.isPublished());
        endpoint.stop();
        assertTrue(!endpoint.isPublished());
    }

    public void testPublishUsingContext() throws Exception {
        // TODO
    }

}
