package org.apache.hello_world_soap_http;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.apache.hello_world_soap_http.types.BareDocumentResponse;
import org.apache.hello_world_soap_http.types.GreetMeResponse;
import org.apache.hello_world_soap_http.types.GreetMeSometimeResponse;
import org.apache.hello_world_soap_http.types.SayHiResponse;
import org.apache.hello_world_soap_http.types.TestDocLitFaultResponse;


@javax.jws.WebService(endpointInterface = "org.apache.hello_world_soap_http.Greeter")
public class AnotherDerivedGreeterImpl implements GreeterEndpointInterface {

    private static final Logger LOG =
        Logger.getLogger(DerivedGreeterImpl.class.getName());
    private final Map<String, Integer> invocationCount = new HashMap<String, Integer>();

    public AnotherDerivedGreeterImpl() {
        invocationCount.put("sayHi", 0);
        invocationCount.put("greetMe", 0);
        invocationCount.put("greetMeOneWay", 0);
        invocationCount.put("overloadedSayHi", 0);
    }

    public int getInvocationCount(String method) {
        if (invocationCount.containsKey(method)) {
            return invocationCount.get(method).intValue();
        } else {
            System.out.println("No invocation count for method: " + method);
            return 0;
        }
    }

    /**
     * overloaded method - present for test purposes
     */
    public String sayHi(String me) throws RemoteException {
        incrementInvocationCount("overloadedSayHi");
        return "Hi " + me + "!";
    }

    @javax.jws.WebMethod(operationName = "sayHi")
    /*
     * @javax.jws.WebResult(name="responseType",
     * targetNamespace="http://apache.org/hello_world_soap_http")
     */
    public String sayHi() {
        incrementInvocationCount("sayHi");
        return "Hi";
    }

    public void testDocLitFault(String faultType)  throws BadRecordLitFault, NoSuchCodeLitFault {
    }

    public Response<TestDocLitFaultResponse> testDocLitFaultAsync(String faultType) {
        return null;
        /*not called */
    }

    public Response<TestDocLitFaultResponse> testDocLitFaultAsync(String faultType,
                                                                  AsyncHandler<TestDocLitFaultResponse> ah) {
        return null;
        /*not called */
    }


    public String greetMe(String me) {
        incrementInvocationCount("greetMe");
        return "Bonjour " + me + "!";
    }

    public String greetMeSometime(String me) {
        incrementInvocationCount("greetMeSometime");
        return "Hello there " + me + "!";
    }

    public Future<?>  greetMeSometimeAsync(String requestType,
                                           AsyncHandler<GreetMeSometimeResponse> asyncHandler) {
        return null;
        /* to be implemented */
    }

    public Response<GreetMeSometimeResponse> greetMeSometimeAsync(String requestType) {
        return null;
        /* to be implemented */
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

    public Future<?> testDocLitBareAsync(String in, AsyncHandler<BareDocumentResponse> asyncHandler) {
        return null;
        /*not called */
    }

    public Response<BareDocumentResponse> testDocLitBareAsync(String in) {
        return null;
        /*not called */
    }

    public void greetMeOneWay(String me) {
        incrementInvocationCount("greetMeOneWay");
    }

    public BareDocumentResponse testDocLitBare(String in) {
        incrementInvocationCount("testDocLitBare");
        BareDocumentResponse res = new BareDocumentResponse();
        res.setCompany("Celtix");
        res.setId(1);
        return res;
    }

    private void incrementInvocationCount(String method) {
        LOG.info("Executing " + method);
        int n = invocationCount.get(method);
        invocationCount.put(method, n + 1);
    }

}

