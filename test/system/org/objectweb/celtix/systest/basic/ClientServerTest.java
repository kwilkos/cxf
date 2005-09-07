package org.objectweb.celtix.systest.basic;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceFactory;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.ServiceFactoryImpl;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_soap_http.Greeter;

public class ClientServerTest extends ClientServerTestBase {
    
    
    private QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");    
    private QName portName = new QName("http://objectweb.org/hello_world_soap_http", "SoapPort");

    
    public void setUp() throws BusException {
        super.setUp();
        launchServer(Server.class);
    }
    
    public void tearDown() throws BusException {
        super.tearDown();
    }
    
    
    public void testBasicConnection() throws Exception {
                
        URL wsdl = getClass().getResource("../resource/hello_world.wsdl");
        ServiceFactory factory = ServiceFactory.newInstance();
        assertNotNull(factory);
        assertTrue(factory.getClass().getName(), factory instanceof ServiceFactoryImpl);
        
        Service service = factory.createService(wsdl, serviceName);
        assertNotNull(service);

        try { 
            Greeter greeter = (Greeter) service.getPort(portName, Greeter.class);
            String greeting = greeter.greetMe("Hello-Milestone");
            assertNotNull("no response received from service", greeting);
            System.out.println("response from service: " +  greeting);
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    } 
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ClientServerTest.class);
    }
}
