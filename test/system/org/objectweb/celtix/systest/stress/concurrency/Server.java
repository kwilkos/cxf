package org.objectweb.celtix.systest.stress.concurrency;


import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;
import static org.objectweb.celtix.systest.stress.concurrency.ConcurrentInvokerTest.EXPECTED_CALLS;

public class Server extends TestServerBase {
    
    private GreeterImpl implementor;

    protected void run()  {
        implementor = new GreeterImpl();
        String address = "http://localhost:9009/SoapContext/SoapPort";
        Endpoint.publish(address, implementor);
    }
    
    public static void main(String[] args) {
        try { 
            Server s = new Server(); 
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }

    /**
     * Used to facilitate assertions on server-side behaviour.
     *
     * @param log logger to use for diagnostics if assertions fail
     * @return true if assertions hold
     */
    protected boolean verify(Logger log) {
        boolean verified = implementor.greetMeCount.get() == EXPECTED_CALLS
                && implementor.greetMeOneWayCount.get() == EXPECTED_CALLS
                && implementor.sayHiCount.get() == EXPECTED_CALLS
                && implementor.docLitFaultCount.get() == EXPECTED_CALLS;
        if (!verified) {
            warn(log, "greetMe", implementor.greetMeCount.get());
            warn(log, "greetMeOneWay", implementor.greetMeOneWayCount.get());
            warn(log, "sayHi", implementor.sayHiCount.get());
            warn(log, "docLitFault", implementor.docLitFaultCount.get());
        }
        return verified;
    }

    private void warn(Logger log, String method, int received) {
        log.log(Level.WARNING, 
                "{0} received {1} calls, expected {2}", 
                new Object[] {method, received, EXPECTED_CALLS});
    }
}
