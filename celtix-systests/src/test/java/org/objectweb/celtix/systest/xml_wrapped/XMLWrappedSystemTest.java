package org.objectweb.celtix.systest.xml_wrapped;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_xml_http.wrapped.Greeter;
import org.objectweb.hello_world_xml_http.wrapped.PingMeFault;
import org.objectweb.hello_world_xml_http.wrapped.XMLService;

public class XMLWrappedSystemTest extends ClientServerTestBase {

    private static final String TEST_NAMESPACE = "http://objectweb.org/hello_world_xml_http/wrapped";
    private final QName serviceName = new QName(TEST_NAMESPACE, "XMLService");
    private final QName portName = new QName(TEST_NAMESPACE, "XMLPort");

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(XMLWrappedSystemTest.class);
        return new ClientServerSetupBase(suite) {
                public void startServers() throws Exception {
                    assertTrue("server did not launch correctly", launchServer(Server.class));
                }
            };
    }

    public void testBasicConnection() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_xml_wrapped.wsdl");
        assertNotNull(wsdl);
        
        XMLService service = new XMLService(wsdl, serviceName);
        assertNotNull(service);
        
        Greeter greeter = service.getPort(portName, Greeter.class);
        
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
            }            
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    public void testFaults() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_xml_wrapped.wsdl");
        assertNotNull(wsdl);
        
        XMLService service = new XMLService(wsdl, serviceName);
        assertNotNull(service);

        Greeter greeter = service.getPort(portName, Greeter.class);
        for (int idx = 0; idx < 2; idx++) {
            try {
                greeter.pingMe();
                fail("Should have thrown PingMeFault exception");
            } catch (PingMeFault nslf) {
                assertNotNull(nslf.getFaultInfo());
                assertEquals(1, nslf.getFaultInfo().getMinor());
                assertEquals(2, nslf.getFaultInfo().getMajor());
            } 
        }
    } 

    public static void main(String[] args) {
        junit.textui.TestRunner.run(XMLWrappedSystemTest.class);
    }
}
