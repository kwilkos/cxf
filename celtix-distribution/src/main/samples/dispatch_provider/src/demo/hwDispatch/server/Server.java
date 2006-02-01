package demo.hwDispatch.server;

import javax.xml.ws.Endpoint;


public class Server {

    protected Server() throws Exception {
        System.out.println("Starting Server");

        System.out.println("Starting SoapService1");
        Object implementor = new GreeterSoapMessageProvider();
        String address = "http://localhost:9000/SoapContext/SoapPort1";
        Endpoint.publish(address, implementor);
        
        System.out.println("Starting SoapService2");
        implementor = new GreeterDOMSourceMessageProvider();
        address = "http://localhost:9000/SoapContext/SoapPort2";
        Endpoint.publish(address, implementor);
        
        System.out.println("Starting SoapService3");
        implementor = new GreeterDOMSourcePayloadProvider();
        address = "http://localhost:9000/SoapContext/SoapPort3";
        Endpoint.publish(address, implementor);        
    }

    public static void main(String args[]) throws Exception {
        new Server();
        System.out.println("Server ready...");

        Thread.sleep(5 * 60 * 1000);
        System.out.println("Server exiting");
        System.exit(0);
    }

}
