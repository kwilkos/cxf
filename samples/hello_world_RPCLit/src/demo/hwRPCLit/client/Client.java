package demo.hwRPCLit.client;

import javax.xml.namespace.QName;
import java.io.File;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.SOAPServiceRPCLit;
import org.objectweb.hello_world_rpclit.types.MyComplexStruct;
import java.rmi.RemoteException;

public class Client {

    static QName serviceName = new QName("http://objectweb.org/hello_world_rpclit",
                                           "SOAPServiceRPCLit");

    static QName portName = new QName("http://objectweb.org/hello_world_rpclit",
                                        "SoapPortRPCLit");
    static Bus bus;

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("please provide wsdl");
            System.exit(0);
        }

        File wsdl = new File(args[0]);

        bus = Bus.init();

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl.toURL(), serviceName);
        GreeterRPCLit greeter = (GreeterRPCLit) service.getPort(portName, GreeterRPCLit.class);
        String greeting = greeter.greetMe("blah");
        System.out.println("response from greetMe operatione: " +  greeting);

        String hiReply = greeter.sayHi();
        System.out.println("response from sayHi operation: " +  hiReply);

		MyComplexStruct argument = new MyComplexStruct();
        MyComplexStruct retVal = null;

        argument.setElem1("Hello, I am first element");
		argument.setElem2("And I am second element");
		argument.setElem3(3);

        retVal = greeter.sendReceiveData(argument);

        System.out.println("Response from sendReceiveData opertion ");
        System.out.println("Element-1 : " + retVal.getElem1());
        System.out.println("Element-2 : " + retVal.getElem2());
        System.out.println("Element-3 : " + retVal.getElem3());

        if (bus != null) {
            bus.shutdown(true);
        }
    }
}
