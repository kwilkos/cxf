package org.objectweb.celtix.systest.stress.concurrency;

import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_soap_http.BadRecordLitFault;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.NoSuchCodeLitFault;
import org.objectweb.hello_world_soap_http.SOAPService;

public class ConcurrentInvokerTest extends ClientServerTestBase {

    static final int INVOKER_COUNT = 5;
    static final int INVOCATION_REPS = 50;
    static final int EXPECTED_CALLS = INVOKER_COUNT * INVOCATION_REPS;

    Greeter greeter;
    private final QName serviceName = new QName("http://objectweb.org/hello_world_soap_http",
                                                "SOAPServiceConcurrencyTest");    
    private final QName portName = new QName("http://objectweb.org/hello_world_soap_http", "SoapPort");

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ConcurrentInvokerTest.class);
    }
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ConcurrentInvokerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }  

    public void setUp() throws Exception {
        super.setUp();
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        SOAPService service = new SOAPService(wsdl, serviceName);
        greeter = service.getPort(portName, Greeter.class);
    }

    public void testConcurrentInvocation() throws Exception {
        waitFor(launch(populateInvokers()));
    } 

    private Thread[] populateInvokers() {
        Thread[] invokers = new Thread[INVOKER_COUNT * 3];
        for (int i = 0; i < INVOKER_COUNT * 3; i++) {
            String s = "Invoker[" + i + "]";
            switch (i / INVOKER_COUNT) {
            case 0:
                invokers[i] = new Thread(new TwoWayInvoker(), "TwoWay" + s);
                break;
            case 1:
                invokers[i] = new Thread(new OneWayInvoker(), "OneWay" + s);
                break;
            case 2:
                invokers[i] = new Thread(new FaultInvoker(), "Fault" + s);
                break;
            default:
            }
        }
        return invokers;
    }

    private Thread[] launch(Thread[] invokers) {
        for (int i = 0; i < invokers.length; i++) {
            invokers[i].start();
        }
        return invokers;
    }

    private void waitFor(Thread[] invokers) {
        for (int i = 0; i < invokers.length; i++) {
            try {
                invokers[i].join();
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        // stop the server here, instead of relying on the shutdown hook
        // installed by the base class, as this would be too late to assert
        // via junit that server received the expected number of calls
        //assertTrue("server failed, see log for details", stopAllServers());
    }

    private class TwoWayInvoker implements Runnable {
        public void run() {
            String root = Thread.currentThread().getName() + " call: ";
            for (int i = 0; i < INVOCATION_REPS; i++) {
                String in = root + i;
                String greeting = greeter.greetMe(in);
                assertNotNull("no response received from service", greeting);
                assertEquals("Hello " + in, greeting);
                String hi = greeter.sayHi();
                assertNotNull("no response received from service", hi);
                assertEquals("Hiya", hi);
            }
        }
    }

    private class OneWayInvoker implements Runnable {
        public void run() {
            String root = Thread.currentThread().getName() + " call: ";
            for (int i = 0; i < INVOCATION_REPS; i++) {
                String in = root + i;
                greeter.greetMeOneWay(in);
            }
        }
    }

    private class FaultInvoker implements Runnable {
        public void run() {
            String fault = NoSuchCodeLitFault.class.getSimpleName();
            for (int i = 0; i < INVOCATION_REPS; i++) {
                try {
                    greeter.testDocLitFault(fault);
                    fail("Should have thrown NoSuchCodeLitFault exception");
                } catch (NoSuchCodeLitFault nslf) {
                    assertNotNull(nslf.getFaultInfo());
                    assertNotNull(nslf.getFaultInfo().getCode());
                } catch (BadRecordLitFault brlf) {
                    fail("unexpected fault: " + brlf);
                }
            }
        }
    }
}
