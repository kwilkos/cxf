package org.objectweb.hello_world_soap_http;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceContext;

@javax.jws.WebService(name = "Greeter", serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/hello_world_soap_http")
public class AnnotatedGreeterImpl {

    private static final Logger LOG = 
        Logger.getLogger(AnnotatedGreeterImpl.class.getName());

    private WebServiceContext context;

    private Map<String, Integer> invocationCount = new HashMap<String, Integer>();

    public AnnotatedGreeterImpl() {
        invocationCount.put("sayHi", 0);
        invocationCount.put("greetMe", 0);
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
    @WebMethod(operationName = "sayHiOverloaded")
    @WebResult(name = "responseType2", targetNamespace = "http://objectweb.org/hello_world_soap_http/types")
    @RequestWrapper(className = "org.objectweb.hello_world_soap_http.types.SayHi2",
                    localName = "sayHi2",
                    targetNamespace = "http://objectweb.org/hello_world_soap_http/types")
    @ResponseWrapper(className = "org.objectweb.hello_world_soap_http.types.SayHiResponse2",
                     localName = "sayHiResponse2",
                     targetNamespace = "http://objectweb.org/hello_world_soap_http/types")
    public String sayHi(String me) {
        incrementInvocationCount("overloadedSayHi");
        return "Hi " + me + "!";
    }

    @WebMethod
    @WebResult(name = "responseType",
               targetNamespace = "http://objectweb.org/hello_world_soap_http/types")
    @RequestWrapper(className = "org.objectweb.hello_world_soap_http.types.SayHi",
                    localName = "sayHi",
                    targetNamespace = "http://objectweb.org/hello_world_soap_http/types")
    @ResponseWrapper(className = "org.objectweb.hello_world_soap_http.types.SayHiResponse",
                     localName = "sayHiResponse",
                     targetNamespace = "http://objectweb.org/hello_world_soap_http/types")
    public String sayHi() {
        incrementInvocationCount("sayHi");
        return "Hi";
    }

    @WebMethod
    @WebResult(name = "responseType",
               targetNamespace = "http://objectweb.org/hello_world_soap_http/types")
    @RequestWrapper(className = "org.objectweb.hello_world_soap_http.types.GreetMe",
                    localName = "greetMe",
                    targetNamespace = "http://objectweb.org/hello_world_soap_http/types")
    @ResponseWrapper(className = "org.objectweb.hello_world_soap_http.types.GreetMeResponse",
                     localName = "greetMeResponse",
                     targetNamespace = "http://objectweb.org/hello_world_soap_http/types")
    public String greetMe(String me) {
        incrementInvocationCount("greetMe");
        return "Bonjour " + me + "!";
    }
    
    @WebMethod
    @RequestWrapper(className = "org.objectweb.hello_world_soap_http.types.GreetMeOneWay",
                    localName = "greetMeOneWay",
                    targetNamespace = "http://objectweb.org/hello_world_soap_http/types")
    public void greetMeOneWay(String me) {
        incrementInvocationCount("greetMeOneWay");
        System.out.println("Hello there " + me);
        System.out.println("That was OneWay to say hello");
    }

    public void testDocLitFault(String faultType)  throws BadRecordLitFault, NoSuchCodeLitFault {        
    }

    @Resource
    public void setContext(WebServiceContext ctx) { 
        context = ctx;
    }

    public WebServiceContext getContext() {
        return context;
    }

    private void incrementInvocationCount(String method) {
        LOG.info("Executing " + method);
        int n = invocationCount.get(method);
        invocationCount.put(method, n + 1);
    }

}
