package org.objectweb.celtix;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.ws.ServiceFactory;

import com.iona.hello_world_soap_http.Greeter;
import com.iona.hello_world_soap_http.SOAPService;
import junit.framework.TestCase;

public class ServiceTest extends TestCase {

    public ServiceTest(String arg0) {
        super(arg0);
        System.setProperty(ServiceFactory.SERVICEFACTORY_PROPERTY,
                "org.objectweb.celtix.bus.ServiceFactoryImpl");
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ServiceTest.class);
    }

    /*
     * Test method for 'javax.xml.ws.Service.getPorts()'
     */
    public void testGetPorts() throws Exception {
        Bus bus = Bus.init();
        ServiceFactory sf = ServiceFactory.newInstance();
        QName endpoint = new QName("", "SoapPort"); 
        
        URL url = getClass().getResource("resources/hello_world.wsdl");
        try {
            SOAPService hwService = sf.createService(url, SOAPService.class);
            assertNotNull(hwService);
            assertTrue("Should be a proxy class.", Proxy.isProxyClass(hwService.getClass()));
            Iterator iter = hwService.getPorts();
            assertFalse("Should have no element", iter.hasNext());

            Greeter port = hwService.getSoapPort();
            assertNotNull(port);
            
            iter = hwService.getPorts();
            assertTrue("Should have one element", iter.hasNext());            
            assertEquals("Activated EndPoints are not the same", endpoint, iter.next());            
        } finally {
            bus.shutdown(true);
        }
    }

}

