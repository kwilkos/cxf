package demo.client;

import java.io.File;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.ProtocolException;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.SOAPService;


/**
 * validate call to ejb in jboss from command line
 */
public class GreeterClient {

    private static final QName SERVICE_NAME 
        = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");

    private GreeterClient() {
    }
    
    public static void main(String[] args) throws Exception {
        
        
        if(args.length < 1) { 
            System.out.println("please specify wsdl");
            System.exit(1); 
        } 
        URL wsdlURL;
        File wsdlFile = new File(args[0]);
        if (wsdlFile.exists()) {
            wsdlURL = wsdlFile.toURL();
        } else {
            wsdlURL = new URL(args[0]);
        }
        
        System.out.println(wsdlURL);
        SOAPService ss = new SOAPService(wsdlURL, SERVICE_NAME);
        Greeter port = ss.getSoapPort();
        String resp; 

        System.out.println("Invoking sayHi...");
        resp = port.sayHi();
        System.out.println("Server responded with: " + resp);
        System.out.println();

        System.out.println("Invoking greetMe...");
        resp = port.greetMe(System.getProperty("user.name"));
        System.out.println("Server responded with: " + resp);
        System.out.println();

        System.out.println("Invoking greetMe with invalid length string, expecting exception...");
        try {
            resp = port.greetMe("Invoking greetMe with invalid length string, expecting exception...");
        } catch (ProtocolException e) {
            System.out.println("Expected exception has occurred: " + e.getClass().getName());
        }

        System.exit(0); 

    }
}
