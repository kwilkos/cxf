package org.apache.hello_world_soap_http;


import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.apache.hello_world_soap_http.types.BareDocumentResponse;
import org.apache.hello_world_soap_http.types.ErrorCode;
import org.apache.hello_world_soap_http.types.GreetMeResponse;
import org.apache.hello_world_soap_http.types.GreetMeSometimeResponse;
import org.apache.hello_world_soap_http.types.NoSuchCodeLit;
import org.apache.hello_world_soap_http.types.SayHiResponse;
import org.apache.hello_world_soap_http.types.TestDocLitFaultResponse;

@WebService(serviceName = "SOAPService", 
            portName = "SoapPort", 
            endpointInterface = "org.apache.hello_world_soap_http.Greeter",
            targetNamespace = "http://apache.org/hello_world_soap_http")
public class GreeterImpl implements Greeter {
    
    private static final Logger LOG = Logger.getLogger(GreeterImpl.class.getName());

    private int invocationCount;
    
    public String greetMe(String me) {
        LOG.info("Invoking greetMe");
        invocationCount++;
        return "Hello " + me;
    }

    public String sayHi() {
        LOG.info("Invoking sayHi");
        invocationCount++;        
        return "Bonjour";
    }

    public void testDocLitFault(String faultType) throws BadRecordLitFault, NoSuchCodeLitFault {
        LOG.info("Invoking testDocLitFault");
        invocationCount++;
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

    public void greetMeOneWay(String requestType) {
        invocationCount++;
        System.out.println("*********  greetMeOneWay: " + requestType);
    }
    
    public String greetMeSometime(String me) {
        invocationCount++;
        //System.err.println("In greetMeSometime: " + me);
        return "How are you " + me;
    }
    
    public BareDocumentResponse testDocLitBare(String in) {
        invocationCount++;
        BareDocumentResponse res = new BareDocumentResponse();
        res.setCompany("Celtix");
        res.setId(1);
        return res;
    }
    
    public Future<?>  greetMeSometimeAsync(String requestType, 
                                           AsyncHandler<GreetMeSometimeResponse> asyncHandler) {
        invocationCount++;
        System.err.println("In greetMeSometimeAsync 1");
        return null; 
        /*not called */
    }
    
    public Response<GreetMeSometimeResponse> greetMeSometimeAsync(String requestType) { 
        invocationCount++;
        System.err.println("In greetMeSometimeAsync 2");
        return null; 
        /*not called */
    }
    
    public Response<TestDocLitFaultResponse> testDocLitFaultAsync(String faultType) {  
        invocationCount++;
        System.err.println("In testDocLitFaultAsync 1");
        return null; 
        /*not called */
    }
    
    public Future<?> testDocLitFaultAsync(String faultType, AsyncHandler ah) {  
        invocationCount++;
        System.err.println("In testDocLitFaultAsync 2");
        return null; 
        /*not called */
    }
    
    public Future<?> testDocLitBareAsync(String bare, AsyncHandler ah) {
        invocationCount++;
        return null;
        /* not called */
    }
    
    public Response<BareDocumentResponse> testDocLitBareAsync(String bare) {
        invocationCount++;
        return null;
        /* not called */
    }
    
    public Future<?> greetMeAsync(String requestType, AsyncHandler<GreetMeResponse> asyncHandler) { 
        invocationCount++;
        return null; 
        /*not called */
    }
    
    public Response<GreetMeResponse> greetMeAsync(String requestType) { 
        invocationCount++;
        return null; 
        /*not called */
    }
    
    public Future<?> sayHiAsync(AsyncHandler<SayHiResponse> asyncHandler) { 
        invocationCount++;
        return null; 
        /*not called */
    }
    
    public Response<SayHiResponse> sayHiAsync() { 
        invocationCount++;
        return null; 
        /*not called */
    }

    public int getInvocationCount() {
        return invocationCount;
    }
    
}
