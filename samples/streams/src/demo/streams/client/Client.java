package demo.streams.client;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import javax.xml.namespace.QName;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.PingMeFault;
import org.objectweb.hello_world_soap_http.SOAPService;


public final class Client {
    
    private static final QName SERVICE_NAME 
        = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");


    private Client() {
    } 

    public static void main(String args[]) throws Exception {
        
        try { 
            if (args.length == 0) { 
                System.out.println("please specify wsdl");
                System.exit(1); 
            }

            File wsdl = new File(args[0]);
        
            SOAPService ss = new SOAPService(wsdl.toURL(), SERVICE_NAME);
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
        } catch (UndeclaredThrowableException ex) { 
            ex.getUndeclaredThrowable().printStackTrace();
        } catch (Exception ex) { 
            ex.printStackTrace();
        }  finally { 
            System.exit(0); 
        }
    }

}