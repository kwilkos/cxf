package org.objectweb.celtix.systest.ws.rm;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.objectweb.celtix.greeter_control.Greeter;
import org.objectweb.celtix.greeter_control.PingMeFault;
import org.objectweb.celtix.greeter_control.types.FaultDetail;
import org.objectweb.celtix.greeter_control.types.GreetMeResponse;
import org.objectweb.celtix.greeter_control.types.PingMeResponse;
import org.objectweb.celtix.greeter_control.types.SayHiResponse;



@WebService(serviceName = "GreeterService", portName = "GreeterPort", 
            name = "Greeter", 
            targetNamespace = "http://celtix.objectweb.org/greeter_control")
public class GreeterImpl implements Greeter {
    private static final Logger LOG = Logger.getLogger(GreeterImpl.class.getName());
    
    public String greetMe(String me) { 
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Invoking greetMe with parameters(s): " + me);
        }
        return "Hello " + me;
    }

    public void greetMeOneWay(String requestType) {  
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Invoking greetMeOneWay with parameters(s): " + requestType);
        }
        try {
            // sleep will ensure a spurious resend if partial response containing
            // ACK is not completed *before* the implementor is invoked
            Thread.sleep(15 * 1000);
        } catch (Exception e) {
            // ignore
        }
    }

    public String sayHi() {
        LOG.info("Invoking sayHi");
        return "Bonjour";
    }

    public void pingMe() throws PingMeFault {
        LOG.info("Invoking pingMe");
        FaultDetail faultDetail = new FaultDetail();
        faultDetail.setMajor((short)2);
        faultDetail.setMinor((short)1);
        throw new PingMeFault("PingMeFault raised by server", faultDetail);       
    }
    
    public Future<?> greetMeAsync(String requestType, AsyncHandler<GreetMeResponse> asyncHandler) {
        // not called
        return null;
    }
    public Response<GreetMeResponse> greetMeAsync(String requestType) {
        // not called
        return null;
    }
    
    public Response<PingMeResponse> pingMeAsync() {
        // not called
        return null;
    }

    public Future<?> pingMeAsync(AsyncHandler<PingMeResponse> asyncHandler) {
        // not called
        return null;
    }

    public Response<SayHiResponse> sayHiAsync() {
        // not called
        return null;
    }

    public Future<?> sayHiAsync(AsyncHandler<SayHiResponse> asyncHandler) {
        // not called
        return null;
    }
    
    
    
   
}
