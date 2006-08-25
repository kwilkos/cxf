package demo.hw_https.client;

import java.io.File;
import java.net.URL;
import javax.xml.namespace.QName;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.SOAPService;

public final class Client {

    private static final QName SERVICE_NAME 
        = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
    
    
    private static final QName SECURE_PORT_NAME = 
        new QName("http://objectweb.org/hello_world_soap_http",
                  "SecurePort");
    
    private static final QName STRICT_SECURE_PORT_NAME = 
        new QName("http://objectweb.org/hello_world_soap_http",
                  "StrictSecurePort");
    

    private static final QName INSECURE_PORT_NAME = 
        new QName("http://objectweb.org/hello_world_soap_http",
                  "InsecurePort");


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
        SOAPService ss = new SOAPService(wsdlURL, SERVICE_NAME);
        Greeter port;
        
        if ((args[1] != null) && (args[1].equalsIgnoreCase("secure_user"))) {
            System.out.println("The secure_user credentials will be used for the invocation.");
            System.out.println();
            port = ss.getPort(SECURE_PORT_NAME, Greeter.class);
        } else if ((args[1] != null) && (args[1].equalsIgnoreCase("strict_secure_user"))) {
            String configurerProperty = "celtix.security.configurer" 
                        + ".celtix.{http://objectweb.org/hello_world_soap_http}" 
                        + "SOAPService/StrictSecurePort.http-client";
            String configurerClass = "demo.hw_https.common.DemoSecurityConfigurer";
            System.setProperty(configurerProperty, configurerClass);
            System.out.println("The strict_secure_user credentials will be used for the invocation.");
            System.out.println("Extra security data will be provided by the class, " + configurerClass 
                               + " because the system property  " + configurerProperty
                               + " has been set.");
            System.out.println();
            port = ss.getPort(STRICT_SECURE_PORT_NAME, Greeter.class);
        } else {
            System.out.println("The insecure_user credentials will be used for the invocation.");
            System.out.println();
            port = ss.getPort(INSECURE_PORT_NAME, Greeter.class);
        }
        
        String resp; 

        System.out.println("Invoking greetMe...");
        try {
            resp = port.greetMe(System.getProperty("user.name"));
            System.out.println("Server responded with: " + resp);
            System.out.println();
            
        } catch (Exception e) {
            System.out.println("Invocation to server failed with the following error : " + e.getCause());
            System.out.println();
        }

        System.exit(0); 
    }

}
