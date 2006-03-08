package demo.hw.server;

import java.util.logging.Logger;
import org.objectweb.hello_world_xml_http.wrapped.Greeter;

@javax.jws.WebService(name = "Greeter", serviceName = "XMLService", 
                      targetNamespace = "http://objectweb.org/hello_world_xml_http/wrapped", 
                      wsdlLocation = "file:./wsdl/hello_world.wsdl")

@javax.xml.ws.BindingType(value="http://celtix.objectweb.org/bindings/xmlformat")               
public class GreeterImpl implements Greeter {
    
    private static final Logger LOG = 
        Logger.getLogger(GreeterImpl.class.getPackage().getName());
    
    /* (non-Javadoc)
     * @see org.objectweb.hello_world_xml_http.wrapped.Greeter#greetMe(java.lang.String)
     */
    public String greetMe(String me) {
        LOG.info("Executing operation greetMe");
        System.out.println("Executing operation greetMe");
        System.out.println("Message received: " + me + "\n");
        return "Hello " + me;
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.hello_world_xml_http.wrapped.Greeter#greetMeOneWay(java.lang.String)
     */
    public void greetMeOneWay(String me) {
        LOG.info("Executing operation greetMeOneWay");
        System.out.println("Executing operation greetMeOneWay\n");
        System.out.println("Hello there " + me);
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.hello_world_xml_http.wrapped.Greeter#sayHi()
     */
    public String sayHi() {
        LOG.info("Executing operation sayHi");
        System.out.println("Executing operation sayHi\n");
        return "Bonjour";
    }   
}
