package org.objectweb.celtix.bus.jaxws;


import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.spi.Provider;

import junit.framework.TestCase;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.util.InetAddrPort;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.TestBinding;
import org.objectweb.celtix.bindings.TestBindingFactory;
import org.objectweb.celtix.bus.jaxws.spi.ProviderImpl;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.hello_world_soap_http.AnnotatedGreeterImpl;
import org.objectweb.hello_world_soap_http.HWSourcePayloadProvider;
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

    public void testPublishUsingHttpContext() throws Exception {
        
        assertNotNull(endpoint);
        assertTrue(!endpoint.isPublished());
        
        HttpServer server = new HttpServer();      
        SocketListener listener = new SocketListener(new InetAddrPort(27220));
        server.addListener(listener);
        try {
            server.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        HttpContext context = server.getContext("http://localhost:27220/test");
        endpoint.publish(context);
        assertTrue(endpoint.isPublished());
        
        listener.stop();
        server.stop(true);
        
        
    }
    
    public void testPublishUsingEndpointReferenceTypeContext() throws Exception {
        
        assertNotNull(endpoint);
        assertTrue(!endpoint.isPublished());
             
        EndpointReferenceType context = 
            EndpointReferenceUtils.getEndpointReference("http://localhost:8080/test");
        endpoint.publish(context);
        
        assertTrue(endpoint.isPublished());       
    }


    public void testResourceInjectionApplicationContext() { 
        // WebServiceContext is specified by a resource annotation.
        // This should be inject when the endpoing is published.
        //i.e in context of Endpoint.publish.
        //Such injection should not happen for Endpoint.create.
        //JAX_WS Spec 5.2.1.
        
        WebServiceContext ctx = servant.getContext();
        assertNull(ctx);
        try {
            String address = "http://localhost:8080/test";
            endpoint.publish(address);
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getCause() instanceof BusException);
            assertEquals("BINDING_INCOMPATIBLE_ADDRESS_EXC", ((BusException)ex.getCause()).getCode());
        }
        ctx = servant.getContext();
        assertNotNull(ctx);
    }

    public void testHandlerAnnotation() { 

        List<Handler> handlers = endpoint.getBinding().getHandlerChain();
        assertNotNull(handlers);
    }
    
    public void testCreateWithProvider() {
        HelloWorldServiceProvider provider = new  HelloWorldServiceProvider();
        endpoint = Endpoint.create(TestBinding.TEST_BINDING, provider);
        assertNotNull(endpoint);

        EndpointImpl impl = (EndpointImpl)endpoint;
        assertNotNull(impl.getWebServiceProvider());
        assertEquals(DataBindingCallback.Mode.MESSAGE, impl.getServiceMode());
    }
    
    public void testBadProvider() {   
        NotAnnotatedProvider badProvider = new  NotAnnotatedProvider();
        try {
            Endpoint.create(TestBinding.TEST_BINDING, badProvider);
            fail("Should have received a exception");
        } catch (WebServiceException ex) {
            // expected
        }
    }
    
    public void testGetMethod() {
        QName opName = new QName("", "PutLastTradedPrice");
        assertTrue(endpoint instanceof EndpointImpl);
        EndpointImpl impl = (EndpointImpl)endpoint;
        //Check if a method by a localPart of opName exists on the Implementor.
        Method m = impl.getMethod(opName);
        
        assertNotNull(m);
        
        opName = new QName("", "putLastTradedPrice");
        m = impl.getMethod(opName);
        assertNull(m);
        
        //Test for provider
        HelloWorldServiceProvider provider = new  HelloWorldServiceProvider();
        endpoint = Endpoint.create(TestBinding.TEST_BINDING, provider);
        assertTrue(endpoint instanceof EndpointImpl);
        impl = (EndpointImpl)endpoint;
        opName = new QName("", "invoke");
        //Check if a method by a localPart of opName exists on the Implementor.
        m = impl.getMethod(opName);
        assertNotNull(m);
        assertEquals("invoke", m.getName());
    }

    public void testGetServiceMode() {
        EndpointImpl impl = (EndpointImpl)endpoint;
        assertNotNull(impl);
        DataBindingCallback.Mode mode = impl.getServiceMode();
        assertNotNull(mode);
        assertEquals(DataBindingCallback.Mode.PARTS, mode);
        
        //Test for provider
        HelloWorldServiceProvider provider = new  HelloWorldServiceProvider();
        endpoint = Endpoint.create(TestBinding.TEST_BINDING, provider);
        assertTrue(endpoint instanceof EndpointImpl);
        impl = (EndpointImpl)endpoint;

        mode = impl.getServiceMode();
        assertNotNull(mode);
        assertEquals(DataBindingCallback.Mode.MESSAGE, mode);

        HWSourcePayloadProvider provider1 = new  HWSourcePayloadProvider();
        endpoint = Endpoint.create(TestBinding.TEST_BINDING, provider1);
        assertTrue(endpoint instanceof EndpointImpl);

        impl = (EndpointImpl)endpoint;        
        mode = impl.getServiceMode();
        assertNotNull(mode);
        assertEquals(DataBindingCallback.Mode.PAYLOAD, mode);
    }
    
    /*
    public void testCreateDataBindingCallback() {
        ObjectMessageContextImpl ctx = new ObjectMessageContextImpl();
        EndpointImpl impl = (EndpointImpl)endpoint;
        assertNotNull(impl);
        DataBindingCallback cb = 
            impl.createDataBindingCallback(ctx, DataBindingCallback.Mode.PARTS);
        assertNotNull(cb);
        assertTrue(cb instanceof JAXBDataBindingCallback);
        assertEquals(DataBindingCallback.Mode.PARTS, cb.getMode());
        
        //Test for provider
        HelloWorldServiceProvider provider = new  HelloWorldServiceProvider();
        endpoint = Endpoint.create(TestBinding.TEST_BINDING, provider);
        assertTrue(endpoint instanceof EndpointImpl);
        impl = (EndpointImpl)endpoint;

        cb = impl.createDataBindingCallback(ctx, DataBindingCallback.Mode.MESSAGE);
        assertNotNull(cb);
        assertTrue(cb instanceof DynamicDataBindingCallback);
        assertEquals(DataBindingCallback.Mode.MESSAGE, cb.getMode());

        HWSourcePayloadProvider provider1 = new  HWSourcePayloadProvider();
        endpoint = Endpoint.create(TestBinding.TEST_BINDING, provider1);
        assertTrue(endpoint instanceof EndpointImpl);

        impl = (EndpointImpl)endpoint;        
        cb = impl.createDataBindingCallback(ctx, DataBindingCallback.Mode.PAYLOAD);
        assertNotNull(cb);
        assertTrue(cb instanceof DynamicDataBindingCallback);
        assertEquals(DataBindingCallback.Mode.PAYLOAD, cb.getMode());
    }
    */
    
    public void testGetWebServiceAnnotatedClass() {
        EndpointImpl impl = (EndpointImpl)endpoint;
        assertNotNull(impl);
        List<Class<?>> classList = impl.getWebServiceAnnotatedClass();

        assertNotNull(classList);
        assertEquals(1, classList.size());
        
        //Test for provider
        HelloWorldServiceProvider provider = new  HelloWorldServiceProvider();
        endpoint = Endpoint.create(TestBinding.TEST_BINDING, provider);
        assertTrue(endpoint instanceof EndpointImpl);
        impl = (EndpointImpl)endpoint;

        classList = impl.getWebServiceAnnotatedClass();
        assertNotNull(classList);
        assertEquals(0, classList.size());
    }    
    
}
