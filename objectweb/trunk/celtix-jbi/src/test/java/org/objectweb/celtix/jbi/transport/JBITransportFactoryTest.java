package org.objectweb.celtix.jbi.transport;

import javax.jbi.messaging.DeliveryChannel;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.jbi.se.CeltixServiceUnitManager;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class JBITransportFactoryTest extends TestCase {

    JBITransportFactory factory = new JBITransportFactory();
    
    private final DeliveryChannel channel = EasyMock.createMock(DeliveryChannel.class);
    private final CeltixServiceUnitManager suMgr = EasyMock.createMock(CeltixServiceUnitManager.class);
    private final EndpointReferenceType endpointRef = new EndpointReferenceType();
    
    
    public void setUp() {
        factory.setDeliveryChannel(channel);
        factory.setServiceUnitManager(suMgr);
    }
    
    
    public void testGetSetDeliveryChannel() {

        factory.setDeliveryChannel(channel);
        assertSame("get must return set value", channel, factory.getDeliveryChannel());
    }

    public void testGetSetServiceUnitManager() {

        factory.setServiceUnitManager(suMgr);
        assertSame("get must return set value", suMgr, factory.getServiceUnitManager());
    }


    public void testCreateServerTransportNotInitialized() throws Exception {

        try {
            factory = new JBITransportFactory();
            factory.createServerTransport(endpointRef);
            fail("did not get expected exception");
        } catch (IllegalStateException ex) {
            // expected exception
        }
    }

  
    public void testCreateServerTransport() throws Exception {
        
        ServerTransport st = factory.createServerTransport(endpointRef);
        assertNotNull("server transport must not be null", st);
        assertSame("transport must JBIServerTransport", JBIServerTransport.class, st.getClass());
    }

  
    public void testCreateTransientServerTransport() throws Exception {
        
        try {
            factory.createTransientServerTransport(endpointRef);
            fail("did not get expected message");
        } catch (RuntimeException ex) {
            assertEquals("wrong message in exception", "not yet implemented", ex.getMessage());
        }
       
    }

   
    public void testCreateClientTransport() throws Exception {
        
        QName serviceName = new QName("", "foobar");
        EndpointReferenceUtils.setServiceAndPortName(endpointRef, serviceName, "SOAPPort");
        
        ClientBinding clientBinding = EasyMock.createMock(ClientBinding.class);
        ResponseCallback responseCallback = EasyMock.createMock(ResponseCallback.class);
        clientBinding.createResponseCallback();
        EasyMock.expectLastCall().andReturn(responseCallback);
        
        EasyMock.replay(clientBinding);
        
        ClientTransport ct = factory.createClientTransport(endpointRef, clientBinding);
        assertNotNull("server transport must not be null", ct);
        assertSame("transport must JBIClientTransport", JBIClientTransport.class, ct.getClass());
        EasyMock.verify(clientBinding);
    
    }

 
    public void testSetResponseCallback() {

        try {
            factory.setResponseCallback(null);
            fail("did not get expected message");
        } catch (RuntimeException ex) {
            assertEquals("wrong message in exception", "not yet implemented", ex.getMessage());
        }
    }

}
