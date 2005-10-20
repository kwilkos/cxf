package org.objectweb.celtix.systest.basic;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.SOAPService;

public class ClientServerTest extends ClientServerTestBase {
    
    
    private QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");    
    private QName portName = new QName("http://objectweb.org/hello_world_soap_http", "SoapPort");

    
    public void setUp() throws BusException {
        super.setUp();
    }

    public void onetimeSetUp() { 
        assertTrue("server did not launch correctly", launchServer(Server.class));
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBasicConnection() throws Exception {
        URL wsdl = getClass().getResource("/hello_world.wsdl");
        assertNotNull(wsdl);
        
        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("Hello Milestone-");
        String response2 = new String("Bonjour");
        try {
            Greeter greeter = (Greeter) service.getPort(portName, Greeter.class);
            for (int idx = 0; idx < 20; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                String exResponse = response1 + idx;
                assertEquals(exResponse, greeting);
                
                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    } 
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ClientServerTest.class);
    }
}
