package demo.hwJMS.server;

import java.util.logging.Logger;
import org.objectweb.celtix.hello_world_jms.HelloWorldPortType;

@javax.jws.WebService(name = "HelloWorldPortType", serviceName = "HelloWorldService",
                      targetNamespace = "http://celtix.objectweb.org/hello_world_jms",
                      wsdlLocation = "file:./wsdl/hello_world_jms.wsdl")
public class GreeterJMSImpl implements HelloWorldPortType {

    private static final Logger LOG = Logger.getLogger(GreeterJMSImpl.class.getPackage().getName());

    public void greetMeOneWay(String me) {
        LOG.info("Executing operation greetMeOneWay");
        System.out.println("Executing operation greetMeOneWay\n");
        System.out.println("Hello there " + me);
    }
}
