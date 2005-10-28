package demo.hwRPCLit.client;

import javax.xml.namespace.QName;
import java.io.File;
import org.objectweb.celtix.Bus;
import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.SOAPServiceRPCLit;
import org.objectweb.hello_world_rpclit.types.MyComplexStruct;

public class Client {

    static QName serviceName = new QName("http://objectweb.org/hello_world_rpclit",
                                           "SOAPServiceRPCLit");

    static QName portName = new QName("http://objectweb.org/hello_world_rpclit",
                                        "SoapPortRPCLit");

   public static void printUsage() {
	   System.out.println("Arguments Required");
	   System.out.println("wsdl, sayHi");
	   System.out.println("wsdl, greetMe, string");
       System.out.println("wsdl, sendReceiveData, string, string, int");
   }

   public static void main(String[] args) throws Exception {

		String strParam1 = null;
		String strParam2 = null;
		int intParam3 = -1;

        if (args.length == 0) {
            printUsage();
            System.exit(0);
        }

        File wsdl = new File(args[0]);

        String operationName = "sayHi";
		if (args.length > 1) {
			operationName = args[1];
		}

		if (args.length >= 3 && "greetMe".equals(operationName)) {
			strParam1 = args[2];
        } else if (args.length >= 5 && "sendReceiveData".equals(operationName)) {
			strParam1 = args[2];
			strParam2 = args[3];
			intParam3 = Integer.parseInt(args[4]);
		} else if (!"sayHi".equals(operationName)) {
			printUsage();
			System.exit(0);
		}

        Bus bus = Bus.init();

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl.toURL(), serviceName);
        GreeterRPCLit greeter = (GreeterRPCLit) service.getPort(portName, GreeterRPCLit.class);

        if ("sayHi".equals(operationName)) {

			System.out.println("Invoking sayHi...");
            System.out.println("server responded with: " + greeter.sayHi());

        } else if ("greetMe".equals(operationName)) {

			System.out.println("Invoking greetMe...");
            System.out.println("server responded with: " +  greeter.greetMe(strParam1));

		} else if ("sendReceiveData".equals(operationName)) {

			MyComplexStruct argument = new MyComplexStruct();
			MyComplexStruct retVal = null;

			argument.setElem1(strParam1);
            argument.setElem2(strParam2);
            argument.setElem3(intParam3);
            System.out.println("Invoking sendReceiveData...");

			retVal = greeter.sendReceiveData(argument);

			System.out.println("Response from sendReceiveData operation :");
			System.out.println("Element-1 : " + retVal.getElem1());
			System.out.println("Element-2 : " + retVal.getElem2());
            System.out.println("Element-3 : " + retVal.getElem3());

		}

        if (bus != null) {
            bus.shutdown(true);
        }
    }
}
