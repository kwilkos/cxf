package org.objectweb.celtix.systest.provider;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;

import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.SOAPServiceRPCLit;

public class ProviderClientServerTest extends ClientServerTestBase {

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ProviderClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }

    public void testMessageModeWithSOAPMessageData() throws Exception {
        
        QName serviceName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SOAPServiceRPCLit");
        QName portName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SoapPortRPCLit");

        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("TestGreetMeResponse");
        String response2 = new String("TestSayHiResponse");
        try {
            GreeterRPCLit greeter = service.getPort(portName, GreeterRPCLit.class);
            for (int idx = 0; idx < 2; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                assertEquals(response1, greeting);

                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    public void testMessageModeWithDOMSourceData() throws Exception {
        QName serviceName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SOAPServiceRPCLit1");
        QName portName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SoapPortRPCLit1");

        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("TestGreetMeResponse");
        String response2 = new String("TestSayHiResponse");
        try {
            GreeterRPCLit greeter = service.getPort(portName, GreeterRPCLit.class);
            for (int idx = 0; idx < 2; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                assertEquals(response1, greeting);

                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    public void testPayloadModeWithDOMSourceData() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        QName serviceName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SOAPServiceRPCLit2");
        QName portName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SoapPortRPCLit2");

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("TestGreetMeResponse");
        String response2 = new String("TestSayHiResponse");
        try {
            GreeterRPCLit greeter = service.getPort(portName, GreeterRPCLit.class);
            for (int idx = 0; idx < 1; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                assertEquals(response1, greeting);

                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }
    
    public void testMessageModeWithSAXSourceData() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        QName serviceName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SOAPServiceRPCLit3");
        QName portName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SoapPortRPCLit3");

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("TestGreetMeResponse");
        String response2 = new String("TestSayHiResponse");
        try {
            GreeterRPCLit greeter = service.getPort(portName, GreeterRPCLit.class);
            for (int idx = 0; idx < 1; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                assertEquals(response1, greeting);

                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    public void testMessageModeWithStreamSourceData() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        QName serviceName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SOAPServiceRPCLit4");
        QName portName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SoapPortRPCLit4");

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("TestGreetMeResponse");
        String response2 = new String("TestSayHiResponse");
        try {
            GreeterRPCLit greeter = service.getPort(portName, GreeterRPCLit.class);
            for (int idx = 0; idx < 1; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                assertEquals(response1, greeting);

                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }
 

    
    public void testPayloadModeWithSAXSourceData() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        QName serviceName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SOAPServiceRPCLit5");
        QName portName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SoapPortRPCLit5");

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("TestGreetMeResponse");
        String response2 = new String("TestSayHiResponse");
        try {
            GreeterRPCLit greeter = service.getPort(portName, GreeterRPCLit.class);
            for (int idx = 0; idx < 1; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                assertEquals(response1, greeting);

                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

 
    public void testPayloadModeWithStreamSourceData() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        QName serviceName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SOAPServiceRPCLit6");
        QName portName = 
            new QName("http://objectweb.org/hello_world_rpclit", "SoapPortRPCLit6");

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("TestGreetMeResponse");
        String response2 = new String("TestSayHiResponse");
        try {
            GreeterRPCLit greeter = service.getPort(portName, GreeterRPCLit.class);
            for (int idx = 0; idx < 1; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                assertEquals(response1, greeting);

                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }
    

    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ProviderClientServerTest.class);
    }
}
