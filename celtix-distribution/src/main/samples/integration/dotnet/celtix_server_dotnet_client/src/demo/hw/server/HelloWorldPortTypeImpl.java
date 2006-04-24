package demo.hw.server;

import java.util.logging.Logger;
import org.objectweb.hello_world_soap_http.HelloWorldPortType;


@javax.jws.WebService(name = "HelloWorldPortType", serviceName = "SOAPService",
                      targetNamespace = "http://objectweb.org/hello_world_soap_http",
                      wsdlLocation = "file:./wsdl/hello_world.wsdl")

public class HelloWorldPortTypeImpl implements HelloWorldPortType {

    private static final Logger LOG =
        Logger.getLogger(HelloWorldPortTypeImpl.class.getPackage().getName());


    public java.lang.String sayHi() {
        LOG.info("Executing operation sayHi");
        System.out.println("Executing operation sayHi");
        return "Bonjour";
    }


    public java.lang.String greetMe(
        java.lang.String me
    ) {
        LOG.info("Executing operation greetMe");
        System.out.println("Executing operation greetMe");
        System.out.println("Message received" + me);
        return me;
    }

}