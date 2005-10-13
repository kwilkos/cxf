package org.objectweb.celtix.bus;

import java.util.Properties;

import javax.xml.ws.Endpoint;
import javax.xml.ws.spi.Provider;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bus.bindings.TestBinding;
import org.objectweb.celtix.bus.bindings.TestBindingFactory;
import org.objectweb.celtix.bus.jaxws.spi.ProviderImpl;
import org.objectweb.hello_world_soap_http.AnnotatedGreeterImpl;

public class EndpointImplTest extends TestCase {
    private String epfClassName;
    private Bus bus;
    private Endpoint endpoint;

    public void setUp() throws Exception {
        epfClassName = System.getProperty(Provider.JAXWSPROVIDER_PROPERTY);
        System.setProperty(Provider.JAXWSPROVIDER_PROPERTY,
                           ProviderImpl.JAXWS_PROVIDER);
        bus = Bus.init();
        BindingManager bm = bus.getBindingManager();
        bm.registerBinding(TestBinding.TEST_BINDING, new TestBindingFactory(bus));
        endpoint = Endpoint.create(TestBinding.TEST_BINDING, new AnnotatedGreeterImpl());

    }

    public void tearDown() throws Exception {
        bus.shutdown(true);
        if (null == epfClassName) {
            Properties properties = System.getProperties();
            properties.remove(Provider.JAXWSPROVIDER_PROPERTY);
            System.setProperties(properties);
        } else {
            System.setProperty(Provider.JAXWSPROVIDER_PROPERTY, epfClassName);
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
            assertEquals("BINDING_INCOMPATIBLE_ADDRESS_EXC", ((BusException)ex.getCause()).getCode());
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
