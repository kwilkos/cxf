package org.objectweb.hello_world_soap_http;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.logging.Logger;

@javax.jws.WebService(name = "Greeter", serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/hello_world_soap_http", 
                      wsdlLocation = "file:/C:/celtix/views/bus-api/trunk/test/wsdl/hello_world.wsdl")
public class AnnotatedGreeterImpl implements Greeter {

    private static Logger logger = Logger.getLogger(AnnotatedGreeterImpl.class.getPackage().getName());
    private HashMap<String, Integer> invocationCount = new HashMap<String, Integer>();

    public AnnotatedGreeterImpl() {
        invocationCount.put("sayHi", new Integer(0));
        invocationCount.put("greetMe", new Integer(0));
        invocationCount.put("overloadedSayHi", new Integer(0));
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
        logger.info("Executing " + method);
        int n = invocationCount.get(method).intValue();
        invocationCount.put(method, new Integer(n + 1));
    }

}
