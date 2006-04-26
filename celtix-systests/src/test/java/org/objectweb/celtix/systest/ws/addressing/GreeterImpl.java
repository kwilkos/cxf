package org.objectweb.celtix.systest.ws.addressing;

import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceContext;

import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.hello_world_soap_http.BadRecordLitFault;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.NoSuchCodeLitFault;
import org.objectweb.hello_world_soap_http.types.BareDocumentResponse;
//import org.objectweb.hello_world_soap_http.types.ErrorCode;
import org.objectweb.hello_world_soap_http.types.ErrorCode;
import org.objectweb.hello_world_soap_http.types.GreetMeResponse;
import org.objectweb.hello_world_soap_http.types.GreetMeSometimeResponse;
import org.objectweb.hello_world_soap_http.types.NoSuchCodeLit;
//import org.objectweb.hello_world_soap_http.types.NoSuchCodeLit;
import org.objectweb.hello_world_soap_http.types.SayHiResponse;
import org.objectweb.hello_world_soap_http.types.TestDocLitFaultResponse;

import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;


@WebService(serviceName = "SOAPServiceAddressing", portName = "SoapPort", 
            name = "Greeter", 
            targetNamespace = "http://objectweb.org/hello_world_soap_http")
public class GreeterImpl implements Greeter {
    VerificationCache verificationCache;

    /**
     * Injectable context.
     */
    @Resource
    private WebServiceContext context;


    public String greetMe(String me) {
        verifyMAPs();
        return "Hello " + me;
    }

    public void greetMeOneWay(String requestType) {   
        verifyMAPs();
    }

    public String sayHi() {
        verifyMAPs();
        return "Bonjour";
    }
    
    public void testDocLitFault(String faultType) throws BadRecordLitFault, NoSuchCodeLitFault {
        verifyMAPs();
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
        BareDocumentResponse res = new BareDocumentResponse();
        res.setCompany("Celtix");
        res.setId(1);
        return res;
    }

    private void verifyMAPs() {
        String property = SERVER_ADDRESSING_PROPERTIES_INBOUND;
        AddressingProperties maps = (AddressingProperties)
            context.getMessageContext().get(property);
        verificationCache.put(MAPTest.verifyMAPs(maps, this));
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
