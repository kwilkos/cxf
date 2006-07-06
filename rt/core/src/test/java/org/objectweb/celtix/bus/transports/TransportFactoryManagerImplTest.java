package org.objectweb.celtix.bus.transports;

import java.io.IOException;

import javax.wsdl.WSDLException;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class TransportFactoryManagerImplTest extends TestCase {

    private static final String TEST_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/http/";

    public void testConstructor() {
        TransportFactoryManagerImpl tfm = new TransportFactoryManagerImpl(null);
        assertEquals(0, tfm.transportFactories.size());
        assertEquals(0, tfm.getFactoryNamespaceMappings().size());
    }

    public void testLoadTransportFactory() throws BusException {
        TransportFactoryManagerImpl tfm = new TransportFactoryManagerImpl(null);
        try {
            tfm.loadTransportFactory("org.objectweb.celtix.transports.none.NoTransportFactory",
                                     TEST_NAMESPACE);
        } catch (BusException ex) {
            assertTrue(ex.getCause() instanceof ClassNotFoundException);
        }
        try {
            tfm
                .loadTransportFactory(TestTransportFactoryProtectedConstructor.class.getName(),
                                      TEST_NAMESPACE);
        } catch (BusException ex) {
            assertTrue(ex.getCause() instanceof InstantiationException);
        }
        tfm.loadTransportFactory(TestTransportFactory.class.getName(), TEST_NAMESPACE);
        assertNotNull("Transport factory not found.", tfm.getTransportFactory(TEST_NAMESPACE));
    }

    static final class TestTransportFactory implements TransportFactory {

        public TestTransportFactory() {

        }

        public ClientTransport createClientTransport(EndpointReferenceType address, ClientBinding binding)
            throws WSDLException, IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public ServerTransport createServerTransport(EndpointReferenceType address) throws WSDLException,
            IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public ServerTransport createTransientServerTransport(EndpointReferenceType address)
            throws WSDLException, IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public void init(Bus bus) {
            // TODO Auto-generated method stub

        }
    }

    static final class TestTransportFactoryProtectedConstructor implements TransportFactory {

        protected TestTransportFactoryProtectedConstructor() {

        }

        public ClientTransport createClientTransport(EndpointReferenceType address, ClientBinding binding)
            throws WSDLException, IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public ServerTransport createServerTransport(EndpointReferenceType address) throws WSDLException,
            IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public ServerTransport createTransientServerTransport(EndpointReferenceType address)
            throws WSDLException, IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public void init(Bus bus) {
            // TODO Auto-generated method stub

        }
    }
}
