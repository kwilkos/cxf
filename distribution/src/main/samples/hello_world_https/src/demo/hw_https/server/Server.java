package demo.hw_https.server;

import javax.xml.ws.Endpoint;

public class Server {

    protected Server() throws Exception {
        System.out.println("Starting Server");

        Object implementor = new GreeterImpl();
        String address = "https://localhost:9001/SoapContext/SoapPort";
        Endpoint.publish(address, implementor);
    }

    public static void main(String args[]) throws Exception {
        System.out.println("The server's security configuration wil be read from the configuration file, "
                           + "celtix-server.xml using the bean id \"celtix.http-listener.9001\".");
        if ((args[1] != null) && (args[1].equalsIgnoreCase("strict_server"))) {
            String configurerProperty = "celtix.security.configurer.celtix.http-listener.9001";
            String configurerClass = "demo.hw_https.common.DemoSecurityConfigurer";
            System.setProperty(configurerProperty, configurerClass);
            
            System.out.println("Extra security data will be provided by the class, " + configurerClass 
                               + " because the system property  " + configurerProperty
                               + " has been set to invoke on it.");
            
        }
        System.out.println();
        new Server();
        System.out.println("Server ready...");

        Thread.sleep(5 * 60 * 1000);
        System.out.println("Server exiting");
        System.exit(0);
    }
}
