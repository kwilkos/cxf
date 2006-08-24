package org.apache.cxf.jaxws;

import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.SOAPService;

public class ProxyTest extends TestCase {
    
    private final QName serviceName = new QName("http://apache.org/hello_world_soap_http",
                                                "SOAPService");   
    private final QName portName = new QName("http://apache.org/hello_world_soap_http",
                                             "SoapPort");
    
        
    public void testCreatePort() throws Exception {
        
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        Greeter greeter = service.getPort(portName, Greeter.class);

        try {
            String reply = greeter.sayHi();
            System.out.println("Response to sayHi() is: " + reply);
        } catch (Exception ex) {
            ex.printStackTrace();
            // ecpect this as no server is up and running
        }

    }

}
