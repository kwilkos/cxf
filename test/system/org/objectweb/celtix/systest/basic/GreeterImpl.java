package org.objectweb.celtix.systest.basic;


import javax.jws.WebService;

import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.LiteralException;

@WebService(serviceName = "SOAPService", portName = "SoapPort", name = "Greeter", 
            targetNamespace = "http://objectweb.org/hello_world_soap_http")
public class GreeterImpl implements Greeter {

    public String greetMe(String me) {
        return "Hello " + me;
    }

    public String sayHi() {
        return "Bonjour";
    }
    
    public void testDocLitFault() throws LiteralException {        
    }
}
