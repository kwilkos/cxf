package org.objectweb.celtix.systest.basic.underscore;
import javax.jws.WebService;
import org.objectweb.hello_world_soap_http_underscore.Greeter;
import org.objectweb.hello_world_soap_http_underscore.types.GreetMeSometime;
import org.objectweb.hello_world_soap_http_underscore.types.GreetMeSometimeResponse;

@WebService(serviceName = "SOAPService", 
            portName = "SoapPort", 
            endpointInterface = "org.objectweb.hello_world_soap_http_underscore.Greeter",
            targetNamespace = "http://objectweb.org/hello_world_soap_http_underscore")
public class GreeterImpl implements Greeter {

   
    public GreetMeSometimeResponse greetMeSometime(GreetMeSometime in) {
        GreetMeSometimeResponse response = new GreetMeSometimeResponse();
        response.setResponseType("hello world");
        return response;
    }   
}
