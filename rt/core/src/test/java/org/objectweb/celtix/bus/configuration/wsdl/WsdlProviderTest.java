package org.objectweb.celtix.bus.configuration.wsdl;

import java.net.URL;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.wsdl.WSDLManagerImpl;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.JAXBExtensionHelper;
import org.objectweb.celtix.wsdl.WSDLManager;

public class WsdlProviderTest extends TestCase {
    
    private static final URL WSDL_URL = WsdlProviderTest.class.getResource("/wsdl/wsdl_provider_test.wsdl");
    private static final QName SERVICE = new QName("http://celtix.objectweb.org/HelloWorld", 
                                                   "HelloWorldPortBinding");
    private static final String PORT = "HelloWorldPort";
    private static WSDLManager wmgr;
    
    public void setUp() throws WSDLException, BusException, JAXBException {
        if (null == wmgr) {
            wmgr = new WSDLManagerImpl(null);
            JAXBExtensionHelper.addExtensions(wmgr.getExtenstionRegistry(), javax.wsdl.Port.class,
                org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy.class);
            JAXBExtensionHelper.addExtensions(wmgr.getExtenstionRegistry(), javax.wsdl.Port.class,
                org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy.class);
        }
    }
    
    public void testWsdlPortProvider() throws WSDLException {
        
        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(WSDL_URL, SERVICE, PORT);
        Port p = EndpointReferenceUtils.getPort(wmgr, ref);       
       
        WsdlPortProvider pp = new WsdlPortProvider(p);
        assertNotNull(pp);
        
        Object value = pp.getObject("bindingId");
        assertEquals(value.toString(), "http://schemas.xmlsoap.org/wsdl/soap/", value);

        value = pp.getObject("address");
        assertEquals("http://localhost:9876", (String)value);
        
        pp = new WsdlPortProvider(null);
        assertNull(pp.getObject("bindingId"));
    }
    
    public void testWsdlHttpConfigurationProvider() throws WSDLException, JAXBException {
        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(WSDL_URL, SERVICE, PORT);
        Port p = EndpointReferenceUtils.getPort(wmgr, ref);       
       
        WsdlHttpConfigurationProvider hcp = new WsdlHttpConfigurationProvider(p, true);
        assertNull(hcp.getObject("SendTimeout"));
        Object value = hcp.getObject("httpServer");        
        assertNotNull(value);
        assertEquals(30000, ((HTTPServerPolicy)value).getReceiveTimeout());
    
           
        hcp = new WsdlHttpConfigurationProvider(p, false);
        assertNull(hcp.getObject("ReceiveTimeout"));
        value = hcp.getObject("httpClient");
        assertEquals(90000, ((HTTPClientPolicy)value).getReceiveTimeout());
        
        hcp = new WsdlHttpConfigurationProvider(null, false);
        value = hcp.getObject("httpClient");
        assertNull(value);
    }
    

}
