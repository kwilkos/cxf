package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;
import java.net.URL;

import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.wsdl.WSDLManagerImpl;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.WSDLManager;

public class RMPolicyProviderTest extends TestCase {
    
    public void testNoPolicy() throws BusException, WSDLException {
        URL url = getClass().getResource("/wsdl/hello_world.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        String portName = "SoapPort";
        
        EndpointReferenceType epr = EndpointReferenceUtils.getEndpointReference(url, serviceName, portName);
        WSDLManager wsdlManager = new WSDLManagerImpl(null);
        
        Bus bus = EasyMock.createMock(Bus.class);
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager).times(2);
        EasyMock.replay(bus);
        
        RMPolicyProvider provider = new RMPolicyProvider(bus, epr);
        assertNull(provider.getObject("rmPolicy"));
        assertNull(provider.getObject("rmAssertion"));
        
        EasyMock.verify(bus);    
    }
/*
    public void testReferenced() throws BusException, WSDLException, JAXBException {
        URL url = getClass().getResource("resources/ReliableOneWay.wsdl");
        assertNotNull("Could not find WSDL", url);
        QName serviceName = new QName("http://tempuri.org/", "PingService");
        String portName = "WSHttpBinding_IPing";

        EndpointReferenceType epr = EndpointReferenceUtils.getEndpointReference(url, serviceName, portName);
        WSDLManager wsdlManager = new WSDLManagerImpl(null);

        Bus bus = EasyMock.createMock(Bus.class);
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager).times(2);
        EasyMock.replay(bus);

        RMPolicyProvider provider = new RMPolicyProvider(bus, epr);
        RMAssertionType rma = (RMAssertionType)provider.getObject("rmAssertion");
        assertNotNull(rma);
        assertEquals(new BigInteger("600000"), rma.getInactivityTimeout().getMilliseconds());
        assertEquals(new BigInteger("200"), rma.getAcknowledgementInterval().getMilliseconds());
        assertNull(rma.getBaseRetransmissionInterval());     

        EasyMock.verify(bus);
    }
*/
    
    public void testPolicyOnBinding() throws BusException, WSDLException, JAXBException {
        URL url = getClass().getResource("resources/hello_world_rmassertion.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService1");
        String portName = "SoapPort";

        EndpointReferenceType epr = EndpointReferenceUtils.getEndpointReference(url, serviceName, portName);
        WSDLManager wsdlManager = new WSDLManagerImpl(null);

        Bus bus = EasyMock.createMock(Bus.class);
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager).times(2);
        EasyMock.replay(bus);

        RMPolicyProvider provider = new RMPolicyProvider(bus, epr);
        assertNotNull(EndpointReferenceUtils.getPort(wsdlManager, epr));
        assertNotNull(EndpointReferenceUtils.getPort(wsdlManager, epr).getBinding());
        RMAssertionType rma = (RMAssertionType)provider.getObject("rmAssertion");
        assertNotNull(rma);
        assertEquals(new BigInteger("600000"), rma.getInactivityTimeout().getMilliseconds());
        assertEquals(new BigInteger("200"), rma.getAcknowledgementInterval().getMilliseconds());
        assertNull(rma.getBaseRetransmissionInterval());     

        EasyMock.verify(bus);
    }
    
    public void testPolicyOnPort() throws BusException, WSDLException, JAXBException {
        URL url = getClass().getResource("resources/hello_world_rmassertion.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService2");
        String portName = "SoapPort";

        EndpointReferenceType epr = EndpointReferenceUtils.getEndpointReference(url, serviceName, portName);
        WSDLManager wsdlManager = new WSDLManagerImpl(null);

        Bus bus = EasyMock.createMock(Bus.class);
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager).times(2);
        EasyMock.replay(bus);

        RMPolicyProvider provider = new RMPolicyProvider(bus, epr);
        assertNotNull(EndpointReferenceUtils.getPort(wsdlManager, epr));
        assertNotNull(EndpointReferenceUtils.getPort(wsdlManager, epr).getBinding());
        RMAssertionType rma = (RMAssertionType)provider.getObject("rmAssertion");
        assertNotNull(rma);
        assertEquals(new BigInteger("900000"), rma.getInactivityTimeout().getMilliseconds());
        assertEquals(new BigInteger("400"), rma.getAcknowledgementInterval().getMilliseconds());
        assertNull(rma.getBaseRetransmissionInterval());     

        EasyMock.verify(bus);
    }
    
    public void testUnresolvedReference() throws BusException, WSDLException, JAXBException {
        URL url = getClass().getResource("resources/hello_world_rmassertion.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService3");
        String portName = "SoapPort";

        EndpointReferenceType epr = EndpointReferenceUtils.getEndpointReference(url, serviceName, portName);
        WSDLManager wsdlManager = new WSDLManagerImpl(null);

        Bus bus = EasyMock.createMock(Bus.class);
        bus.getWSDLManager();
        EasyMock.expectLastCall().andReturn(wsdlManager).times(2);
        EasyMock.replay(bus);

        RMPolicyProvider provider = new RMPolicyProvider(bus, epr);
        assertNotNull(EndpointReferenceUtils.getPort(wsdlManager, epr));
        assertNotNull(EndpointReferenceUtils.getPort(wsdlManager, epr).getBinding());
        RMAssertionType rma = (RMAssertionType)provider.getObject("rmAssertion");
        assertNull(rma);    

        EasyMock.verify(bus);
    }
    
    
    
}
