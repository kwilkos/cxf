package demo.ws_rm.client;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import javax.xml.namespace.QName;

import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.SOAPService;


public final class Client {
    
    private static final QName SERVICE_NAME = 
        new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
    private static final String USER_NAME = System.getProperty("user.name");


    private Client() {
    } 

    public static void main(String args[]) throws Exception {
        if (args.length == 0) { 
            System.out.println("please specify wsdl");
            System.exit(1); 
        }
        
        try { 
            File wsdl = new File(args[0]);
            SOAPService service = new SOAPService(wsdl.toURL(), SERVICE_NAME);
            Greeter port = service.getSoapPort();

            // make a sequence of 8 invocations
            for (int i = 0; i < 4; i++) {
                System.out.println("Invoking sayHi...");
                String resp = port.sayHi();
                System.out.println("Server responded with: " + resp + "\n");

                System.out.println("Invoking greetMeOneWay...");
                port.greetMeOneWay(USER_NAME);
                System.out.println("No response as method is OneWay\n");
            }

            // allow aynchronous resends to occur
            Thread.sleep(30 * 1000);
        } catch (UndeclaredThrowableException ex) { 
            ex.getUndeclaredThrowable().printStackTrace();
        } catch (Exception ex) { 
            ex.printStackTrace();
        } finally { 
            System.exit(0); 
        }
    }
}
