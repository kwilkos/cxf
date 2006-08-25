package org.objectweb.celtix.systest.js;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;

import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.SOAPService;
import org.objectweb.hello_world_soap_http.SOAPServiceTest1;

public class JSClientServerTest extends ClientServerTestBase {

    private static final String NS = "http://objectweb.org/hello_world_soap_http";

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(JSClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }

    public void testJSMessageMode() throws Exception {
        QName serviceName = new QName(NS, "SOAPService");
        QName portName = new QName(NS, "SoapPort");

        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("TestGreetMeResponse");
        String response2 = new String("TestSayHiResponse");
        try {
            Greeter greeter = service.getPort(portName, Greeter.class);
            String greeting = greeter.greetMe("TestGreetMeRequest");
            assertNotNull("no response received from service", greeting);
            assertEquals(response1, greeting);

            String reply = greeter.sayHi();
            assertNotNull("no response received from service", reply);
            assertEquals(response2, reply);
        } catch (UndeclaredThrowableException ex) {
            ex.printStackTrace();
            throw (Exception)ex.getCause();
        }
    }

    public void testJSPayloadMode() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);

        QName serviceName = new QName(NS, "SOAPService_Test1");
        QName portName = new QName(NS, "SoapPort_Test1");

        SOAPServiceTest1 service = new SOAPServiceTest1(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("TestGreetMeResponse");
        String response2 = new String("TestSayHiResponse");
        try {
            Greeter greeter = service.getPort(portName, Greeter.class);
            String greeting = greeter.greetMe("TestGreetMeRequest");
            assertNotNull("no response received from service", greeting);
            assertEquals(response1, greeting);

            String reply = greeter.sayHi();
            assertNotNull("no response received from service", reply);
            assertEquals(response2, reply);
        } catch (UndeclaredThrowableException ex) {
            ex.printStackTrace();
            throw (Exception)ex.getCause();
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JSClientServerTest.class);
    }
}
