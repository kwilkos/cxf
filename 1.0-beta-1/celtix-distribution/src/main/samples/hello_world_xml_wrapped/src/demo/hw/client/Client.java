package demo.hw.client;

import java.io.File;
import java.net.URL;
import javax.xml.namespace.QName;
import org.objectweb.hello_world_xml_http.wrapped.Greeter;
import org.objectweb.hello_world_xml_http.wrapped.PingMeFault;
import org.objectweb.hello_world_xml_http.wrapped.XMLService;

public final class Client {

    private static final QName SERVICE_NAME 
        = new QName("http://objectweb.org/hello_world_xml_http/wrapped", "XMLService");


    private Client() {
    } 

    public static void main(String args[]) throws Exception {
        
        if (args.length == 0) { 
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
        XMLService ss = new XMLService(wsdlURL, SERVICE_NAME);
        Greeter port = ss.getXMLPort();
        String resp; 

        System.out.println("Invoking sayHi...");
        resp = port.sayHi();
        System.out.println("Server responded with: " + resp);
        System.out.println();

        System.out.println("Invoking greetMe...");
        resp = port.greetMe(System.getProperty("user.name"));
        System.out.println("Server responded with: " + resp);
        System.out.println();

        System.out.println("Invoking greetMeOneWay...");
        port.greetMeOneWay(System.getProperty("user.name"));
        System.out.println("No response from server as method is OneWay");
        System.out.println();

        try {
            System.out.println("Invoking pingMe, expecting exception...");
            port.pingMe();
        } catch (PingMeFault ex) {
            System.out.println("Expected exception: PingMeFault has occurred.");
            System.out.println(ex.toString());
        }
                
        System.exit(0); 
    }

}
