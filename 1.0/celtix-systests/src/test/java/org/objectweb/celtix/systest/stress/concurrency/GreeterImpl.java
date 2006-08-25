package org.objectweb.celtix.systest.stress.concurrency;


import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.objectweb.hello_world_soap_http.BadRecordLitFault;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.NoSuchCodeLitFault;
import org.objectweb.hello_world_soap_http.types.BareDocumentResponse;
import org.objectweb.hello_world_soap_http.types.ErrorCode;
import org.objectweb.hello_world_soap_http.types.GreetMeResponse;
import org.objectweb.hello_world_soap_http.types.GreetMeSometimeResponse;
import org.objectweb.hello_world_soap_http.types.NoSuchCodeLit;
import org.objectweb.hello_world_soap_http.types.SayHiResponse;
import org.objectweb.hello_world_soap_http.types.TestDocLitFaultResponse;

@WebService(serviceName = "SOAPServiceConcurrencyTest",
            portName = "SoapPort",
            endpointInterface = "org.objectweb.hello_world_soap_http.Greeter",
            targetNamespace = "http://objectweb.org/hello_world_soap_http")
public class GreeterImpl implements Greeter {

    protected AtomicInteger greetMeCount = new AtomicInteger(0);
    protected AtomicInteger greetMeOneWayCount = new AtomicInteger(0);
    protected AtomicInteger sayHiCount = new AtomicInteger(0);
    protected AtomicInteger docLitFaultCount = new AtomicInteger(0);
    protected AtomicInteger docLitBareCount = new AtomicInteger(0);


    public String greetMe(String me) {
        greetMeCount.incrementAndGet();
        return "Hello " + me;
    }

    public void greetMeOneWay(String me) {
        greetMeOneWayCount.incrementAndGet();
    }

    public String sayHi() {
        sayHiCount.incrementAndGet();
        return "Hiya";
    }

    public void testDocLitFault(String faultType) throws BadRecordLitFault, NoSuchCodeLitFault {
        docLitFaultCount.incrementAndGet();
        if (faultType.equals(BadRecordLitFault.class.getSimpleName())) {
            throw new BadRecordLitFault("TestBadRecordLit", "BadRecordLitFault");
        }
        if (faultType.equals(NoSuchCodeLitFault.class.getSimpleName())) {
            ErrorCode ec = new ErrorCode();
            ec.setMajor((short)1);
            ec.setMinor((short)1);
            NoSuchCodeLit nscl = new NoSuchCodeLit();
            nscl.setCode(ec);
            throw new NoSuchCodeLitFault("TestNoSuchCodeLit", nscl);
        }
    }

    public BareDocumentResponse testDocLitBare(String in) {
        docLitBareCount.incrementAndGet();
        BareDocumentResponse res = new BareDocumentResponse();
        res.setCompany("Celtix");
        res.setId(1);
        return res;
    }
    
    public String greetMeSometime(String me) {
        return "How are you " + me;
    }
    
    
    public Response<TestDocLitFaultResponse> testDocLitFaultAsync(String faultType) {  
        return null; 
        /*not called */
    }
    
    public Future<?> testDocLitFaultAsync(String faultType, 
                                          AsyncHandler<TestDocLitFaultResponse> asyncHandler) {  
        return null; 
        /*not called */
    }
    
    public Future<?> testDocLitBareAsync(String bare, AsyncHandler ah) {
        return null;
        /* not called */
    }
    
    public Response<BareDocumentResponse> testDocLitBareAsync(String bare) {
        return null;
        /* not called */
    }
    
    public Future<?>  greetMeSometimeAsync(String requestType, 
                                           AsyncHandler<GreetMeSometimeResponse> asyncHandler) { 
        return null; 
        /*not called */
    }
    
    public Response<GreetMeSometimeResponse> greetMeSometimeAsync(String requestType) { 
        return null; 
        /*not called */
    }
    
    public Future<?> greetMeAsync(String requestType, AsyncHandler<GreetMeResponse> asyncHandler) { 
        return null; 
        /*not called */
    }
    
    public Response<GreetMeResponse> greetMeAsync(String requestType) { 
        return null; 
        /*not called */
    }
    
    public Future<?> sayHiAsync(AsyncHandler<SayHiResponse> asyncHandler) { 
        return null; 
        /*not called */
    }
    
    public Response<SayHiResponse> sayHiAsync() { 
        return null; 
        /*not called */
    }
    
    
    
}
