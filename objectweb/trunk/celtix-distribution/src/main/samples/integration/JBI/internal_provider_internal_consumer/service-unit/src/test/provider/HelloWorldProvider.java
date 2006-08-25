package test.provider;

import org.objectweb.hello_world.Greeter;
import org.objectweb.hello_world.PingMeFault;
import org.objectweb.hello_world.types.FaultDetail;



@javax.jws.WebService(name = "Greeter", serviceName = "HelloWorldService", 
                      targetNamespace = "http://objectweb.org/hello_world", 
                      wsdlLocation = "/META-INF/hello_world.wsdl")
                  
public class HelloWorldProvider implements Greeter {

    /* (non-Javadoc)
     * @see org.objectweb.hello_world#greetMe(java.lang.String)
     */
    public String greetMe(String me) {
        System.out.println("Executing operation greetMe");
        System.out.println("Message received: " + me);
        return "Hello " + me;
    }
    
    public void sayHi() {
        System.out.println("Executing operation sayHi");
    }

    public void pingMe() throws PingMeFault {
        FaultDetail faultDetail = new FaultDetail();
        faultDetail.setMajor((short)2);
        faultDetail.setMinor((short)1);
        System.out.println("Executing operation pingMe, throwing PingMeFault exception\n");
        throw new PingMeFault("PingMeFault raised by server", faultDetail);
    }

}
