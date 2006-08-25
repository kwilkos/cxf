package demo.hw.server;

import java.util.logging.Logger;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.management.InstrumentationManager;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.PingMeFault;
import org.objectweb.hello_world_soap_http.types.FaultDetail;

@javax.jws.WebService(name = "Greeter", serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/hello_world_soap_http", 
                      wsdlLocation = "file:./wsdl/hello_world.wsdl")
                  
public class GreeterImpl implements Greeter {

    private static final Logger LOG = 
        Logger.getLogger(GreeterImpl.class.getPackage().getName());

    int[] requestCounters = new int[4];

    String returnName = "Bonjour";

    private GreeterInstrumentation in;

    private InstrumentationManager im;

    /*
     * Create the instrumentation component and register it to instrumentation manager
     */
    public GreeterImpl() {
        in = new GreeterInstrumentation(this);
        Bus bus = Bus.getCurrent();
        im = bus.getInstrumentationManager();
        im.register(in);
    }

    public void shutDown() {
        im.unregister(in);
    }

    /* (non-Javadoc)
     * @see org.objectweb.hello_world_soap_http.Greeter#greetMe(java.lang.String)
     */
    public String greetMe(String me) {
        LOG.info("Executing operation greetMe");
        System.out.println("Executing operation greetMe");
        System.out.println("Message received: " + me + "\n");
        requestCounters[0]++;
        return "Hello " + me;
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.hello_world_soap_http.Greeter#greetMeOneWay(java.lang.String)
     */
    public void greetMeOneWay(String me) {
        LOG.info("Executing operation greetMeOneWay");
        System.out.println("Executing operation greetMeOneWay\n");
        System.out.println("Hello there " + me);
        requestCounters[1]++;
    }

    /* (non-Javadoc)
     * @see org.objectweb.hello_world_soap_http.Greeter#sayHi()
     */
    public String sayHi() {
        LOG.info("Executing operation sayHi");
        System.out.println("Executing operation sayHi\n");
        requestCounters[2]++;
        return returnName;
    }
    
    public void pingMe() throws PingMeFault {
        FaultDetail faultDetail = new FaultDetail();
        faultDetail.setMajor((short)2);
        faultDetail.setMinor((short)1);
        LOG.info("Executing operation pingMe, throwing PingMeFault exception");
        System.out.println("Executing operation pingMe, throwing PingMeFault exception\n");
        requestCounters[3]++;
        throw new PingMeFault("PingMeFault raised by server", faultDetail);
    }

    
}
