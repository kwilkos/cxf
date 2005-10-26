package demo.hw.client;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.io.File;
import javax.xml.ws.WebServiceException;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.PingMeFault;
import org.objectweb.hello_world_soap_http.SOAPService;
import java.rmi.RemoteException;

public class Client {

    static QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", 
                                           "SOAPService");

    static QName portName = new QName("http://objectweb.org/hello_world_soap_http", 
                                        "SoapPort");
    static Bus bus; 

    public static void main(String[] args) throws Exception {
        
        if (args.length == 0) {
            System.err.println("please provide wsdl");
            System.exit(0);
        }

        File wsdl = new File(args[0]);

        bus = Bus.init();

        SOAPService service = new SOAPService(wsdl.toURL(), serviceName);
        Greeter greeter = (Greeter) service.getPort(portName, Greeter.class);
        String greeting = greeter.greetMe("blah");
        System.out.println("response from greetMe operatione: " +  greeting);
        
        String hiReply = greeter.sayHi();
        System.out.println("response from sayHi operation: " +  hiReply);

        try {
            greeter.pingMe();
        } catch(PingMeFault ex) {
            System.out.println("Exception: PingMeFault has Occurred.");
            System.out.println(ex.toString());
        }
                        
        if (bus != null) { 
            bus.shutdown(true);
        }
    }
}

