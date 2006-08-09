package org.objectweb.celtix.systest.jca;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;


import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.connector.Connection;
import org.objectweb.celtix.jca.celtix.CeltixConnectionRequestInfo;
import org.objectweb.celtix.jca.celtix.ManagedConnectionFactoryImpl;
import org.objectweb.celtix.systest.basic.Server;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.SOAPService;
import org.objectweb.hello_world_soap_http.types.BareDocumentResponse;


public class OutBoundConnectionTest extends ClientServerTestBase {

      
    private final QName portName = new QName("http://objectweb.org/hello_world_soap_http",
                                             "SoapPort");

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(OutBoundConnectionTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                System.out.println("[Begin] start the basic test service");
                assertTrue("server did not launch correctly", launchServer(Server.class));
                System.out.println("[End] start the basic test service");
                                   
            }
        };
    }
    public void testBasicConnection() throws Exception {
        SOAPService service = new SOAPService();
        assertNotNull(service);
        URL wsdl = service.getWSDLDocumentLocation();
        assertNotNull(wsdl);
        
        // setup the service model for connection        
                
        CeltixConnectionRequestInfo cri = new CeltixConnectionRequestInfo(Greeter.class, 
                                           wsdl,
                                           service.getServiceName(),
                                           portName);
        
        ManagedConnectionFactoryImpl managedFactory = new ManagedConnectionFactoryImpl();
        Subject subject = new Subject();
        ManagedConnection mc = managedFactory.createManagedConnection(subject, cri);        
        Object o = mc.getConnection(subject, cri);
        
        assertTrue("returned connect does not implement Connection interface", o instanceof Connection);
        assertTrue("returned connect does not implement Connection interface", o instanceof Greeter);
   
        Greeter greeter = (Greeter) o;
        
        String response1 = new String("Hello Milestone-");
        String response2 = new String("Bonjour");
        try {       
            for (int idx = 0; idx < 5; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                String exResponse = response1 + idx;
                assertEquals(exResponse, greeting);
                
                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);

                greeter.greetMeOneWay("Milestone-" + idx);
                
                BareDocumentResponse bareres = greeter.testDocLitBare("MySimpleDocument");
                assertNotNull("no response for operation testDocLitBare", bareres);
                assertEquals("Celtix", bareres.getCompany());
                assertTrue(bareres.getId() == 1);  
                
            }            
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    } 
    
    
    public void testBasicConnection2() throws Exception {
        SOAPService service = new SOAPService();
        assertNotNull(service);
        URL wsdl = service.getWSDLDocumentLocation();
        assertNotNull(wsdl);
        
        // setup the service model for connection        
                
        CeltixConnectionRequestInfo cri = new CeltixConnectionRequestInfo(Greeter.class, 
                                           wsdl,
                                           service.getServiceName(),
                                           null);
        
        ManagedConnectionFactoryImpl managedFactory = new ManagedConnectionFactoryImpl();
        Subject subject = new Subject();
        ManagedConnection mc = managedFactory.createManagedConnection(subject, cri);        
        Object o = mc.getConnection(subject, cri);
        
        assertTrue("returned connect does not implement Connection interface", o instanceof Connection);
        assertTrue("returned connect does not implement Connection interface", o instanceof Greeter);
        
        Greeter greeter = (Greeter) o;
        
        String response1 = new String("Hello Milestone-");
        String response2 = new String("Bonjour");
        try {       
            for (int idx = 0; idx < 5; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                String exResponse = response1 + idx;
                assertEquals(exResponse, greeting);
                
                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);

                greeter.greetMeOneWay("Milestone-" + idx);
                
                BareDocumentResponse bareres = greeter.testDocLitBare("MySimpleDocument");
                assertNotNull("no response for operation testDocLitBare", bareres);
                assertEquals("Celtix", bareres.getCompany());
                assertTrue(bareres.getId() == 1);  
                
            }            
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    } 
    
    
    
}
