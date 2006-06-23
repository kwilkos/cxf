package test.provider;

import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.PingMeFault;
import org.objectweb.hello_world_soap_http.types.FaultDetail;



@javax.jws.WebService(name = "Greeter", serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/hello_world_soap_http", 
                      wsdlLocation = "/META-INF/hello_world.wsdl")
                  
public class HelloWorldProvider implements Greeter {


     /* (non-Javadoc)
     * @see org.objectweb.hello_world_soap_http.Greeter#greetMe(java.lang.String)
     */
    public String greetMe(String me) {
        System.out.println("Executing operation greetMe");
        System.out.println("Message received: " + me + "\n");
        return "Hello " + me;
    }
 
    /* (non-Javadoc)
     * @see org.objectweb.hello_world_soap_http.Greeter#greetMeOneWay(java.lang.String)
     */
    public void greetMeOneWay(String me) {
        System.out.println("Executing operation greetMeOneWay\n");
        System.out.println("Hello there " + me);
    }
 
    /* (non-Javadoc)
     * @see org.objectweb.hello_world_soap_http.Greeter#sayHi()
     */
    public String sayHi() {
        System.out.println("Executing operation sayHi\n");
        return "Bonjour";
    }
 
    public void pingMe() throws PingMeFault {
        FaultDetail faultDetail = new FaultDetail();
        faultDetail.setMajor((short)2);
        faultDetail.setMinor((short)1);
        System.out.println("Executing operation pingMe, throwing PingMeFault exception\n");
        throw new PingMeFault("PingMeFault raised by server", faultDetail);
    }


}
