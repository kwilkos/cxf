package org.objectweb.celtix.systest.routing.passthrough;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.celtix.systest.routing.RouterServer;
import org.objectweb.hello_world_doc_lit.Greeter;
import org.objectweb.hello_world_doc_lit.PingMeFault;
import org.objectweb.hello_world_doc_lit.SOAPService;
import org.objectweb.hello_world_doc_lit.types.FaultDetail;

public class PassThroughRouterTest extends ClientServerTestBase {
    private final QName serviceName = new QName("http://objectweb.org/hello_world_doc_lit",
                                                "SOAPService");
    private final QName portName = new QName("http://objectweb.org/hello_world_doc_lit",
                                             "SoapPort");

    private Greeter greeter;
    protected void setUp() throws Exception {
        super.setUp();
        URL wsdl = getClass().getResource("/wsdl/hello_world_doc_lit.wsdl");
        assertNotNull(wsdl);
        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        greeter = service.getPort(portName, Greeter.class);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(PassThroughRouterTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", 
                           launchServer(Server.class, false));
                
                assertTrue("server did not launch correctly", 
                           launchServer(RouterServer.class, 
                                        null, 
                                        new String[]{"org.objectweb.celtix.BusId", "celtix-st"}));
            }
        };
    }
    
    public void testBasic() throws Exception {
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

                //TODO Not Supported By Router
                //greeter.greetMeOneWay("Milestone-" + idx);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    public void testFaults() throws Exception {
        
        for (int idx = 0; idx < 0; idx++) {
            try {
                greeter.pingMe();
                fail("Should have thrown a PingMeFault exception");
            } catch (PingMeFault pmf) {
                assertEquals(pmf.getMessage(), "Test Exception");
                FaultDetail fd = pmf.getFaultInfo();
                assertNotNull("FaultDetail should havea valid value", fd);
                assertEquals(2, fd.getMajor());
                assertEquals(1, fd.getMinor());
            }
        }
        
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PassThroughRouterTest.class);
    }

}
