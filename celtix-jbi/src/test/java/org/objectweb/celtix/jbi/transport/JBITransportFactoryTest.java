package org.objectweb.celtix.jbi.transport;

import java.util.HashMap;
import java.util.Map;

import javax.jbi.messaging.DeliveryChannel;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.jbi.se.CeltixServiceUnitManager;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.addressing.MetadataType;

public class JBITransportFactoryTest extends TestCase {

    JBITransportFactory factory = new JBITransportFactory();
    
    private final DeliveryChannel channel = EasyMock.createMock(DeliveryChannel.class);
    private final CeltixServiceUnitManager suMgr = EasyMock.createMock(CeltixServiceUnitManager.class);
    private final EndpointReferenceType endpointRef = EasyMock.createMock(EndpointReferenceType.class);
    
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

  
    public void disabledtestCreateServerTransport() throws Exception {
        
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

        Map<QName, String> attrMap = new HashMap<QName, String>();
        QName serviceName = new QName("http://www.w3.org/2004/08/wsdl", "service");
        attrMap.put(serviceName, new QName("", "foobar").toString());
        
        MetadataType metaData = EasyMock.createMock(MetadataType.class);
        endpointRef.getMetadata();
        EasyMock.expectLastCall().andReturn(metaData);
        metaData.getOtherAttributes();
        EasyMock.expectLastCall().andReturn(attrMap);
        
        EasyMock.replay(endpointRef);
        EasyMock.replay(metaData);
        
        ClientTransport ct = factory.createClientTransport(endpointRef);
        assertNotNull("server transport must not be null", ct);
        assertSame("transport must JBIClientTransport", JBIClientTransport.class, ct.getClass());

        EasyMock.verify(endpointRef);
        EasyMock.verify(metaData);
    
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
