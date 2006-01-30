package demo.jms_greeter.client;

import java.io.File;
import javax.xml.namespace.QName;
import org.objectweb.celtix.jms_greeter.JMSGreeterPortType;
import org.objectweb.celtix.jms_greeter.JMSGreeterService;

public final class Client {

    private static final QName SERVICE_NAME =
        new QName("http://celtix.objectweb.org/jms_greeter", "JMSGreeterService");
    private static final QName PORT_NAME =
        new QName("http://celtix.objectweb.org/jms_greeter", "JMSGreeterPortType");

    private Client() {
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("please specify wsdl");
            System.exit(1);
        }

        File wsdl = new File(args[0]);

        JMSGreeterService service = new JMSGreeterService(wsdl.toURL(), SERVICE_NAME);
        JMSGreeterPortType greeter = (JMSGreeterPortType)service.getPort(PORT_NAME, JMSGreeterPortType.class);

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
