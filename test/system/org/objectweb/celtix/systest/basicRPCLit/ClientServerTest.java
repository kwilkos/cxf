package org.objectweb.celtix.systest.basicRPCLit;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.systest.common.ClientServerTestBase;

import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.SOAPServiceRPCLit;
import org.objectweb.hello_world_rpclit.types.MyComplexStruct;

public class ClientServerTest extends ClientServerTestBase {

    private QName serviceName = new QName("http://objectweb.org/hello_world_rpclit", "SOAPServiceRPCLit");
    private QName portName = new QName("http://objectweb.org/hello_world_rpclit", "SoapPortRPCLit");


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
        URL wsdl = getClass().getResource("/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("Hello Milestone-");
        String response2 = new String("Bonjour");
        try {
            GreeterRPCLit greeter = (GreeterRPCLit) service.getPort(portName, GreeterRPCLit.class);
            for (int idx = 0; idx < 5; idx++) {
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
    
    public void testComplexType() throws Exception {
        URL wsdl = getClass().getResource("/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        MyComplexStruct argument = new MyComplexStruct();
        MyComplexStruct retVal = null;
        
        try {
            GreeterRPCLit greeter = (GreeterRPCLit) service.getPort(portName, GreeterRPCLit.class);
            for (int idx = 0; idx < 5; idx++) {
                argument.setElem1("Hello Milestone-" + idx);
                argument.setElem2("Bonjour-" + idx);
                argument.setElem3(idx);
                
                retVal = greeter.sendReceiveData(argument);
                assertNotNull("no response received from service", retVal);
                
                assertTrue(argument.getElem1().equals(retVal.getElem1()));
                assertTrue(argument.getElem2().equals(retVal.getElem2()));
                assertTrue(argument.getElem3() == retVal.getElem3());
                
                retVal = null;
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }       
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ClientServerTest.class);
    }
}
