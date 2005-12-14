package demo.soap_header.server;

import javax.xml.ws.Endpoint;

public class Server {

    protected Server() throws Exception {
        System.out.println("Starting Server");

        Object implementor = new TestHeaderImpl();
        String address = "http://localhost:9100/SoapHeaderContext/SoapHeaderPort";
        Endpoint.publish(address, implementor);
    }
    
    public static void main(String args[]) throws Exception {
        new Server();
        System.out.println("Server ready..."); 
        
        Thread.sleep(5 * 60 * 1000); 
        System.out.println("Server exitting");
        System.exit(0);
    }
}
