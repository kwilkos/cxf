package org.objectweb.celtix.systest.basic;

import java.rmi.RemoteException;

import org.objectweb.hello_world_soap_http.Greeter;

@javax.jws.WebService(name = "Greeter", serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/hello_world_soap_http", 
                      wsdlLocation = "file:../resource/hello_world.wsdl")
                  
public class GreeterImpl implements Greeter {

    /*
    @javax.jws.WebMethod(operationName="sayHi")
    @javax.jws.WebResult(name="responseType", targetNamespace="http://objectweb.org/hello_world_soap_http")
    */
    public String greetMe(String me) throws RemoteException {
        return "Hello " + me;
    }

    /*
    @javax.jws.WebMethod(operationName="greetMe")
    @javax.jws.WebResult(name="responseType", targetNamespace="http://objectweb.org/hello_world_soap_http")
    */
    public String sayHi() throws RemoteException {
        return "Bonjour";
    }
}
