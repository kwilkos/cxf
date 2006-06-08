package test.provider;

import org.objectweb.hello_world.Greeter;

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
}
