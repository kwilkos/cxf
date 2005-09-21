package org.objectweb.hello_world_soap_http;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@javax.jws.WebService(name = "Greeter", serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/hello_world_soap_http")
public class AnnotatedGreeterImpl {

    private static final Logger LOG = 
        Logger.getLogger(AnnotatedGreeterImpl.class.getName());
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
    public String sayHi(String me) throws RemoteException {
        incrementInvocationCount("overloadedSayHi");
        return "Hi " + me + "!";
    }

    @javax.jws.WebMethod(operationName = "sayHi")
    /*
     * @javax.jws.WebResult(name="responseType",
     * targetNamespace="http://objectweb.org/hello_world_soap_http")
     */
    public String sayHi() throws RemoteException {
        incrementInvocationCount("sayHi");
        return "Hi";
    }

    public String greetMe(String me) throws RemoteException {
        incrementInvocationCount("greetMe");
        return "Bonjour " + me + "!";
    }

    private void incrementInvocationCount(String method) {
        LOG.info("Executing " + method);
        int n = invocationCount.get(method);
        invocationCount.put(method, n + 1);
    }

}
