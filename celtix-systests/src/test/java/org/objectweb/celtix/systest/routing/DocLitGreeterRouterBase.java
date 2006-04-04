package org.objectweb.celtix.systest.routing;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_doc_lit.Greeter;
import org.objectweb.hello_world_doc_lit.PingMeFault;
import org.objectweb.hello_world_doc_lit.SOAPService;
import org.objectweb.hello_world_doc_lit.types.FaultDetail;

public class DocLitGreeterRouterBase extends ClientServerTestBase {
    protected Greeter greeter;
    protected QName serviceName;
    protected QName portName;
    protected boolean enableOneway = true;

    protected void setUp() throws Exception {
        super.setUp();
        URL wsdl = getClass().getResource("/wsdl/hello_world_doc_lit.wsdl");
        assertNotNull(wsdl);
        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        greeter = service.getPort(portName, Greeter.class);
    }

    public void testBasic() throws Exception {
        String response1 = new String("Hello Milestone-");
        String response2 = new String("Bonjour");
        try {
            for (int idx = 0; idx < 10; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                String exResponse = response1 + idx;
                assertEquals(exResponse, greeting);

                if (enableOneway) {
                    greeter.greetMeOneWay("Milestone-" + idx);
                }
                
                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    public void testFaults() throws Exception {

        for (int idx = 0; idx < 10; idx++) {
            try {
                greeter.pingMe();
                fail("Should have thrown a PingMeFault exception");
            } catch (PingMeFault pmf) {
                FaultDetail fd = pmf.getFaultInfo();
                assertNotNull("FaultDetail should havea valid value", fd);
                assertEquals(2, fd.getMajor());
                assertEquals(1, fd.getMinor());
            }
        }
    }
}
