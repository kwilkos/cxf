package org.objectweb.celtix.systest.basic.underscore;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;


import javax.xml.namespace.QName;
//import javax.xml.ws.AsyncHandler;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;


import org.objectweb.hello_world_soap_http_underscore.Greeter;
import org.objectweb.hello_world_soap_http_underscore.SOAPService;
import org.objectweb.hello_world_soap_http_underscore.types.GreetMeSometime;
import org.objectweb.hello_world_soap_http_underscore.types.GreetMeSometimeResponse;


public class ClientServerTest extends ClientServerTestBase {

    private final QName serviceName = new QName("http://objectweb.org/hello_world_soap_http_underscore",
                                                "SOAPService");    
    private final QName portName = new QName("http://objectweb.org/hello_world_soap_http_underscore",
                                             "SoapPort");

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }
    public void testGreetMeSometime() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_underscore.wsdl");
        assertNotNull(wsdl);
        
        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);
        
        Greeter greeter = service.getPort(portName, Greeter.class);
        
        
        try {
            GreetMeSometime request = new GreetMeSometime();
            GreetMeSometimeResponse response = greeter.greetMeSometime(request);

            assertEquals("hello world", response.getResponseType());

        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    } 
   
    
    
}
