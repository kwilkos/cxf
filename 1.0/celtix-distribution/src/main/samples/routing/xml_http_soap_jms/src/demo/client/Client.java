package demo.routing.client;

import java.io.File;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import org.objectweb.addnumbers.AddNumbers;
import org.objectweb.addnumbers.AddNumbersFault;

public final class Client {
    static QName serviceName = new QName("http://www.objectweb.org/addNumbers/types",
                                           "AddNumbersXMLService");

    static QName portName = new QName("http://www.objectweb.org/addNumbers/types",
                                        "AddNumbersPort");
    private Client() {
        //Complete
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("please provide wsdl");
            System.exit(0);
        }

        File wsdl = new File(args[0]);

        Service service = Service.create(wsdl.toURL(), serviceName);
        AddNumbers port = (AddNumbers)service.getPort(portName, AddNumbers.class);

        try {
            int number1 = 10;
            int number2 = 20;

            System.out.printf("Invoking addNumbers(%d, %d)\n", number1, number2);
            int result = port.addNumbers(number1, number2);
            System.out.printf("The result of adding %d and %d is %d.\n\n", number1, number2, result);

            number1 = 3;
            number2 = 5;

            System.out.printf("Invoking addNumbers(%d, %d)\n", number1, number2);
            result = port.addNumbers(number1, number2);
            System.out.printf("The result of adding %d and %d is %d.\n\n", number1, number2, result);

            number1 = -10;
            System.out.printf("Invoking addNumbers(%d, %d)\n", number1, number2);
            result = port.addNumbers(number1, number2);
            System.out.printf("The result of adding %d and %d is %d.\n", number1, number2, result);
        } catch (AddNumbersFault ex) {
            System.out.printf("Caught AddNumbersFault: %s\n", ex.getFaultInfo().getMessage());
        }
        System.exit(0);
    }
}
