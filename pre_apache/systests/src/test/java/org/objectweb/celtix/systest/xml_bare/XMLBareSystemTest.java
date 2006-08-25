package org.objectweb.celtix.systest.xml_bare;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_xml_http.bare.Greeter;
import org.objectweb.hello_world_xml_http.bare.XMLService;
import org.objectweb.hello_world_xml_http.bare.types.MyComplexStruct;

public class XMLBareSystemTest extends ClientServerTestBase {

    private static final String TEST_NAMESPACE = "http://objectweb.org/hello_world_xml_http/bare";
    private final QName serviceName = new QName(TEST_NAMESPACE, "XMLService");
    private final QName portName = new QName(TEST_NAMESPACE, "XMLPort");

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(XMLBareSystemTest.class);
        return new ClientServerSetupBase(suite) {
                public void startServers() throws Exception {
                    assertTrue("server did not launch correctly", launchServer(Server.class));
                }
            };
    }

    public void testBasicConnection() throws Exception {
        
        Bus bus = Bus.init();
        bus.getTransportFactoryManager().getTransportFactory("http://schemas.xmlsoap.org/wsdl/soap/http");
        URL wsdl = getClass().getResource("/wsdl/hello_world_xml_bare.wsdl");
        assertNotNull(wsdl);
        
        XMLService service = new XMLService(wsdl, serviceName);
        assertNotNull(service);
        
        Greeter greeter = service.getPort(portName, Greeter.class);
        
        String response1 = new String("Hello Milestone-");
        String response2 = new String("Bonjour");
        MyComplexStruct argument = new MyComplexStruct();
        MyComplexStruct retVal = null;
        
        try {       
            for (int idx = 0; idx < 5; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                String exResponse = response1 + idx;
                assertEquals(exResponse, greeting);
                
                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);

                argument.setElem1("Hello Milestone-" + idx);
                argument.setElem2("Bonjour-" + idx);
                argument.setElem3(idx);
                
                retVal = null;
                retVal = greeter.sendReceiveData(argument);
                
                assertNotNull("no response received from service", retVal);
                assertTrue(argument.getElem1().equals(retVal.getElem1()));
                assertTrue(argument.getElem2().equals(retVal.getElem2()));
                assertTrue(argument.getElem3() == retVal.getElem3());
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(XMLBareSystemTest.class);
    }
}
