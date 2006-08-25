package demo.hw.client;

import java.io.File;
import javax.xml.namespace.QName;
import org.objectweb.hello_world_soap_http.HelloWorldService;
import org.objectweb.hello_world_soap_http.HelloWorldServiceSoap;

public final class Client {

    private static final QName SERVICE_NAME = new QName(
        "http://objectweb.org/hello_world_soap_http", "HelloWorldService");

    private Client() {
    }

    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            System.out.println("please specify wsdl");
            System.exit(1);
        }

        File wsdl = new File(args[0]);

        HelloWorldService ss = new HelloWorldService(wsdl.toURL(), SERVICE_NAME);
        HelloWorldServiceSoap port = ss.getHelloWorldServiceSoap();

        String resp;

        System.out.println("Invoking sayHi...");
        resp = port.sayHi();
        System.out.println("Server responded with: " + resp);
        System.out.println();

        System.out.println("Invoking greetMe...");
        resp = port.greetMe(System.getProperty("user.name"));
        System.out.println("Server responded with: " + resp);
        System.out.println();

        System.exit(0);
    }

}