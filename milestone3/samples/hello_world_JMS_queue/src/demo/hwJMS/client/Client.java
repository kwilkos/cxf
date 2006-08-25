package demo.hwJMS.client;

import java.io.File;
import javax.xml.namespace.QName;
import org.objectweb.celtix.hello_world_jms.HelloWorldPortType;
import org.objectweb.celtix.hello_world_jms.HelloWorldService;

public final class Client {

    private static final QName SERVICE_NAME =
        new QName("http://celtix.objectweb.org/hello_world_jms", "HelloWorldService");
    private static final QName PORT_NAME =
        new QName("http://celtix.objectweb.org/hello_world_jms", "HelloWorldPortType");

    private Client() {
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("please specify wsdl");
            System.exit(1);
        }

        File wsdl = new File(args[0]);

        HelloWorldService service = new HelloWorldService(wsdl.toURL(), SERVICE_NAME);
        HelloWorldPortType greeter = (HelloWorldPortType)service.getPort(PORT_NAME, HelloWorldPortType.class);

        System.out.println("Invoking sayHi...");
        System.out.println("server responded with: " + greeter.sayHi());
        System.out.println();

        System.out.println("Invoking greetMe...");
        System.out.println("server responded with: " + greeter.greetMe(System.getProperty("user.name")));
        System.out.println();
        
        System.out.println("Invoking greetMeOneWay...");
        greeter.greetMeOneWay(System.getProperty("user.name"));
        System.out.println("No response from server as method is OneWay");
        System.out.println();

        System.exit(0);
    }
}
