package demo.hw.client;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import javax.xml.ws.WebServiceException;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.PingMeFault;
import org.objectweb.hello_world_soap_http.SOAPService;
import java.rmi.RemoteException;

public class Client {
    
    protected Client() {        
    }
    
    public static void main(String args[]) throws Exception {


        if (args.length == 0) {
            System.out.println("Arguments Required");
            System.out.println("wsdl, sayHi");
            System.out.println("wsdl, greetMe, string");
            System.out.println("wsdl, greetMeOneWay, string");
            System.out.println("wsdl, pingMe");
            System.exit(0);
            }

       File wsdl = new File(args[0]);

        String operationName = "sayHi";
        if (args.length > 1) {
            operationName = args[1];
        }


        String params = null;
        if (args.length > 2) {
            params = args[2];
        }
        
        System.out.println("Invoking operation: " + operationName);
        System.out.println("Parameters:" + params); 
        
        Bus bus = Bus.init();
               
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        SOAPService ss = new SOAPService(wsdl.toURL(), serviceName);
        Greeter port = ss.getSoapPort();
        
        if ("sayHi".equals(operationName)) {
            System.out.println("Invoking sayHi...");
            System.out.println("server responded with: " + port.sayHi());
        } else if ("greetMe".equals(operationName) && params != null) {
            System.out.println("Invoking greetMe...");
            System.out.println("server responded with: " + port.greetMe(params));
        } else if ("greetMeOneWay".equals(operationName) && params != null) {
            System.out.println("Invoking greetMeOneWay...");
            port.greetMeOneWay(params);
            System.out.println("no response from server as method is OneWay");
        } else if ("pingMe".equals(operationName)) {
            try {
                port.pingMe();
            } catch(PingMeFault ex) {
                System.out.println("Expected exception: PingMeFault has Occurred.");
                System.out.println(ex.toString());
            }          
        } else {
            System.err.println("No such operation");
            System.out.println("Operations supported:");
            System.out.println("operation: sayHi");
            System.out.println("operation: pingMe");
            System.out.println("operation: greetMe, parameter: string");
            System.out.println("operation: greetMeOneWay, parameter: string");   
        }
          
        if (bus != null) { 
            bus.shutdown(true);
        }
    }

}