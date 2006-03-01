package org.objectweb.celtix.systest.ws.rm;

import java.util.concurrent.Future;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.objectweb.hello_world_soap_http.BadRecordLitFault;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.NoSuchCodeLitFault;
import org.objectweb.hello_world_soap_http.types.BareDocumentResponse;
import org.objectweb.hello_world_soap_http.types.GreetMeResponse;
import org.objectweb.hello_world_soap_http.types.GreetMeSometimeResponse;
import org.objectweb.hello_world_soap_http.types.SayHiResponse;
import org.objectweb.hello_world_soap_http.types.TestDocLitFaultResponse;


@WebService(serviceName = "SOAPServiceAddressing", portName = "SoapPort", 
            name = "Greeter", 
            targetNamespace = "http://objectweb.org/hello_world_soap_http")
public class GreeterImpl implements Greeter {
    
    public String greetMe(String me) {
        return "Hello " + me;
    }

    public void greetMeOneWay(String requestType) {   
    }

    public String sayHi() {
        return "Bonjour";
    }
    
    public void testDocLitFault(String faultType) throws BadRecordLitFault, NoSuchCodeLitFault {
        throw new BadRecordLitFault("TestBadRecordLit", "BadRecordLitFault");
    }

    public BareDocumentResponse testDocLitBare(String in) {
        BareDocumentResponse res = new BareDocumentResponse();
        res.setCompany("Celtix");
        res.setId(1);
        return res;
    }

    public String greetMeSometime(String me) {
        return "How are you " + me;
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
    
    public Response<TestDocLitFaultResponse> testDocLitFaultAsync(String faultType) {  
        return null; 
        /*not called */
    }
    
    public Future<?> testDocLitFaultAsync(String faultType, AsyncHandler ah) {  
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
