package org.objectweb.celtix.bus.jaxws;

import java.lang.reflect.Proxy;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.ws.spi.Provider;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.jaxws.spi.ProviderImpl;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.SOAPService;

public class ServiceTest extends TestCase {

    public ServiceTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ServiceTest.class);
    }

    public void setUp() {
        System.setProperty(Provider.JAXWSPROVIDER_PROPERTY, 
            ProviderImpl.JAXWS_PROVIDER);
    }

    /*
     * Test method for 'javax.xml.ws.Service.getPorts()'
     */
    public void testGetPorts() throws Exception {
        Bus bus = Bus.init();
        QName endpoint = new QName("http://objectweb.org/hello_world_soap_http",
                                   "SoapPort"); 
        
        try {
            SOAPService hwService = new SOAPService();
            assertNotNull(hwService);
            Iterator iter = hwService.getPorts();
            assertFalse("Should have no element", iter.hasNext());

            Greeter port = hwService.getSoapPort();
            assertNotNull(port);
            assertTrue("Should be a proxy class. "
                        + port.getClass().getName(),
                        Proxy.isProxyClass(port.getClass()));
            
            iter = hwService.getPorts();
            assertTrue("Should have one element", iter.hasNext());            
            assertEquals("Activated EndPoints are not the same", endpoint, iter.next());            
        } finally {
            bus.shutdown(true);
        }
    }

}

