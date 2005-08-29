package org.objectweb.hello_world_soap_http;

import java.rmi.RemoteException;
import java.util.logging.Logger;

@javax.jws.WebService(name = "Greeter", serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/hello_world_soap_http", 
                      wsdlLocation = "file:/C:/celtix/views/bus-api/trunk/test/wsdl/hello_world.wsdl")
                  
public class IncorrectlyAnnotatedGreeterImpl implements Greeter {

    private static Logger logger = 
        Logger.getLogger(IncorrectlyAnnotatedGreeterImpl.class.getPackage().getName());
    
    /* (non-Javadoc)
     * @see org.objectweb.hello_world_soap_http.Greeter#greetMe(java.lang.String)
     */
    
    /*
    @javax.jws.WebMethod(operationName="sayHi")
    @javax.jws.WebResult(name="responseType", targetNamespace="http://objectweb.org/hello_world_soap_http")
    */
    public String greetMe(String me) throws RemoteException {
        logger.info("Executing operation sayHi");
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.hello_world_soap_http.Greeter#sayHi()
     */
    /*
    @javax.jws.WebMethod(operationName="greetMe")
    @javax.jws.WebResult(name="responseType", targetNamespace="http://objectweb.org/hello_world_soap_http")
    */
    public String sayHi() throws RemoteException {
        logger.info("Executing operation greetMe");
        return "Bonjour";
    }

}
