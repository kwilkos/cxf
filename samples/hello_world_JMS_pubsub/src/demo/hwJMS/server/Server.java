package demo.hwJMS.server;

import javax.xml.ws.Endpoint;

public class Server {

    protected Server() throws Exception {
//        System.setProperty("javax.xml.ws.spi.Provider", "org.objectweb.celtix.bus.jaxws.spi.ProviderImpl");
        System.out.println("Starting Server");
        Object implementor = new GreeterJMSImpl();
        String address = "http://celtix.objectweb.org/transports/jms";
        Endpoint.publish(address, implementor);
    }

    public static void main(String args[]) throws Exception {
        new Server();
        System.out.println("Server ready...");

        Thread.sleep(125 * 60 * 1000);
        System.out.println("Server exitting");
        System.exit(0);
    }
}
