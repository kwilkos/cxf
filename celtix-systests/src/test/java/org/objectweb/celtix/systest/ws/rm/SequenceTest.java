package org.objectweb.celtix.systest.ws.rm;

import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.SOAPService;

/**
 * Tests the addition of WS-Addressing Message Addressing Properties.
 */
public class SequenceTest extends ClientServerTestBase {

    private static final QName SERVICE_NAME = new QName("http://objectweb.org/hello_world_soap_http",
                                                        "SOAPServiceAddressing");
    private static final QName PORT_NAME = new QName("http://objectweb.org/hello_world_soap_http", 
                                                     "SoapPort");
    private Greeter greeter;
    private Bus bus;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SequenceTest.class);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(SequenceTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                // special case handling for WS-Addressing system test to avoid
                // UUID related issue when server is run as separate process
                // via maven on Win2k
                assertTrue("server did not launch correctly", launchServer(Server.class, "Windows 2000"
                    .equals(System.getProperty("os.name"))));
            }
        };
    }

    public void setUp() throws Exception {
        super.setUp();

        bus = Bus.init();

        TestConfigurator tc = new TestConfigurator();
        tc.configureClient(SERVICE_NAME, PORT_NAME.getLocalPart());

        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        SOAPService service = new SOAPService(wsdl, SERVICE_NAME);
        greeter = service.getPort(PORT_NAME, Greeter.class);
    }

    public void tearDown() throws Exception {
        bus.shutdown(true);
    }

    // --Tests

    public void testOneway() throws Exception {

        greeter.greetMeOneWay("once");
        greeter.greetMeOneWay("twice");
        greeter.greetMeOneWay("thrice");

    }

}
