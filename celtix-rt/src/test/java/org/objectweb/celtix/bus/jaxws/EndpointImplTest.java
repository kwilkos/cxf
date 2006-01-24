package org.objectweb.celtix.bus.jaxws;


import java.util.List;
import java.util.Properties;

import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.spi.Provider;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bus.bindings.TestBinding;
import org.objectweb.celtix.bus.bindings.TestBindingFactory;
import org.objectweb.celtix.bus.jaxws.spi.ProviderImpl;
import org.objectweb.hello_world_soap_http.AnnotatedGreeterImpl;
import org.objectweb.hello_world_soap_http.HelloWorldServiceProvider;
import org.objectweb.hello_world_soap_http.NotAnnotatedProvider;

public class EndpointImplTest extends TestCase {
    private String epfClassName;
    private Bus bus;
    private Endpoint endpoint;
    private AnnotatedGreeterImpl servant; 

    public void setUp() throws Exception {
        epfClassName = System.getProperty(Provider.JAXWSPROVIDER_PROPERTY);
        System.setProperty(Provider.JAXWSPROVIDER_PROPERTY,
                           ProviderImpl.JAXWS_PROVIDER);
        bus = Bus.init();
        BindingManager bm = bus.getBindingManager();
        bm.registerBinding(TestBinding.TEST_BINDING, new TestBindingFactory(bus));
        servant = new AnnotatedGreeterImpl();
        endpoint = Endpoint.create(TestBinding.TEST_BINDING, servant);

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


    public void testResourceInjectionApplicationContext() { 
        
     
        // WebServiceContext is specified by a resource annotation.
        // This should be inject when the endpoing is published.
        //
        WebServiceContext ctx = servant.getContext();
        assertNotNull(ctx);
    }

    public void testHandlerAnnotation() { 

        List<Handler> handlers = endpoint.getBinding().getHandlerChain();
        assertNotNull(handlers);
    }
    
    public void testCreatWithProvider() {
        HelloWorldServiceProvider provider = new  HelloWorldServiceProvider();
        endpoint = Endpoint.create(TestBinding.TEST_BINDING, provider);
        assertNotNull(endpoint);
        
        NotAnnotatedProvider badProvider = new  NotAnnotatedProvider();
        try {
            endpoint = Endpoint.create(TestBinding.TEST_BINDING, badProvider);
            assertNull(endpoint);
            //Ideally Should have thrown a WebServiceException
            //fail("Should have received a exception");
        } catch (WebServiceException ex) {
            //Expected Exception
        }
    }
}
