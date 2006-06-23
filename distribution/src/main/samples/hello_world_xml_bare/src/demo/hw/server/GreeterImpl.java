package demo.hw.server;

import java.util.logging.Logger;
import org.objectweb.hello_world_xml_http.bare.Greeter;
import org.objectweb.hello_world_xml_http.bare.types.MyComplexStruct;

@javax.jws.WebService(name = "Greeter", serviceName = "XMLService",
                      targetNamespace = "http://objectweb.org/hello_world_xml_http/bare",
                      wsdlLocation = "file:./wsdl/hello_world.wsdl")

@javax.xml.ws.BindingType(value = "http://celtix.objectweb.org/bindings/xmlformat")
public class GreeterImpl implements Greeter {

    private static final Logger LOG = Logger.getLogger(GreeterImpl.class.getPackage().getName());

    public String greetMe(String me) {
        LOG.info("Executing operation greetMe");
        System.out.println("Executing operation greetMe");
        System.out.println("Message received: " + me + "\n");
        return "Hello " + me;
    }

    public String sayHi() {
        LOG.info("Executing operation sayHi");
        System.out.println("Executing operation sayHi" + "\n");
        return "Bonjour";
    }

    public MyComplexStruct sendReceiveData(MyComplexStruct in) {
        LOG.info("Executing operation sendReceiveData");
        System.out.println("Executing operation sendReceiveData");
        System.out.println("Received struct with values :\nElement-1 : " + in.getElem1() + "\nElement-2 : "
                           + in.getElem2() + "\nElement-3 : " + in.getElem3() + "\n");
        return in;
    }
}
