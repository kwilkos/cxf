package org.objectweb.hello_world_soap_http;

import java.rmi.RemoteException;
import java.util.logging.Logger;

@javax.jws.WebService(name = "Greeter", serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/hello_world_soap_http", 
                      wsdlLocation = "file:/C:/celtix/views/bus-api/trunk/test/wsdl/hello_world.wsdl")
                  
public class CorrectlyAnnotatedGreeterImpl implements Greeter {
    
    private static Logger logger = 
        Logger.getLogger(CorrectlyAnnotatedGreeterImpl.class.getPackage().getName());
    private int[] invocationCount = new int[3];

   

    public String sayHi(String me) throws RemoteException {
        logger.info("Executing overloaded method sayHi");
        invocationCount[2]++;
        return "Hi " + me + "!"; 
    }
    
    @javax.jws.WebMethod(operationName = "sayHi")
    /*
    @javax.jws.WebResult(name="responseType", targetNamespace="http://objectweb.org/hello_world_soap_http")
    */
    public String sayHi() throws RemoteException {
        logger.info("Executing operation sayHi");
        invocationCount[0]++;
        return "Hi"; 
    }
    
    @javax.jws.WebMethod(operationName = "greetMe")
    /*
    @javax.jws.WebResult(name="responseType", targetNamespace="http://objectweb.org/hello_world_soap_http")
    */
    public String greetMe(String me) throws RemoteException {
        logger.info("Executing operation greetMe");
        invocationCount[1]++;
        return "Bonjour " + me  + "!";
    }
    
    public int getInvocationCount(String method) {
        if ("sayHi".equals(method)) {
            return invocationCount[0];
        } else if ("greetMe".equals(method)) {
            return invocationCount[1];
        } else if ("overloadedSayHi".equals(method)) {
            return invocationCount[2];
        }
        return 0;
    }

}
