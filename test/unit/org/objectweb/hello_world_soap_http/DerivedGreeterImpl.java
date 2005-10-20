package org.objectweb.hello_world_soap_http;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@javax.jws.WebService(name = "Greeter", serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/hello_world_soap_http")
public class DerivedGreeterImpl implements Greeter {

    private static final Logger LOG = 
        Logger.getLogger(DerivedGreeterImpl.class.getName());
    private Map<String, Integer> invocationCount = new HashMap<String, Integer>();

    public DerivedGreeterImpl() {
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
    public String sayHi() {
        incrementInvocationCount("sayHi");
        return "Hi";
    }

    public void testDocLitFault(String faultType)  throws BadRecordLitFault, NoSuchCodeLitFault {        
    }

    
    public String greetMe(String me) {
        incrementInvocationCount("greetMe");
        return "Bonjour " + me + "!";
    }

    private void incrementInvocationCount(String method) {
        LOG.info("Executing " + method);
        int n = invocationCount.get(method);
        invocationCount.put(method, n + 1);
    }

}
