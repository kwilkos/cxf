package org.objectweb.celtix.bus.bindings;

import java.io.IOException;

import javax.wsdl.WSDLException;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class BindingFactoryManagerImplTest extends TestCase {

    private static final String TEST_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";

    public void testConstructor() {
        BindingManagerImpl bm = new BindingManagerImpl(null);
        assertEquals(0, bm.bindingFactories.size());
        assertEquals(0, bm.getFactoryNamespaceMappings().size());
    }

    public void testLoadTransportFactory() throws BusException {
        BindingManagerImpl bm = new BindingManagerImpl(null);
        try {
            bm.loadBindingFactory("org.objectweb.celtix.bindings.none.NoBindingFactory",
                                     TEST_NAMESPACE);
        } catch (BusException ex) {
            assertTrue(ex.getCause() instanceof ClassNotFoundException);
        }
        try {
            bm.loadBindingFactory(TestBindingFactoryProtectedConstructor.class.getName(),
                                      TEST_NAMESPACE);
        } catch (BusException ex) {
            assertTrue(ex.getCause() instanceof InstantiationException);
        }
        bm.loadBindingFactory(TestBindingFactory.class.getName(), TEST_NAMESPACE);
        assertNotNull("Transport factory not found.", bm.getBindingFactory(TEST_NAMESPACE));
    }

    static class TestBindingFactory implements BindingFactory {

        public ClientBinding createClientBinding(EndpointReferenceType reference) 
            throws WSDLException, IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public ServerBinding createServerBinding(EndpointReferenceType reference, 
            ServerBindingEndpointCallback endpointCallback) throws WSDLException, IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public void init(Bus bus) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    static class TestBindingFactoryProtectedConstructor implements BindingFactory {

        protected TestBindingFactoryProtectedConstructor() {            
        }
        
        public ClientBinding createClientBinding(EndpointReferenceType reference) 
            throws WSDLException, IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public ServerBinding createServerBinding(EndpointReferenceType reference, 
            ServerBindingEndpointCallback endpointCallback) throws WSDLException, IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public void init(Bus bus) {
            // TODO Auto-generated method stub
            
        }
    }
}
