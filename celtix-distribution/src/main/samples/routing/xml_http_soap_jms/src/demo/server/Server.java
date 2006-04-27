package demo.routing.server;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

public class Server {

    protected Server() throws Exception {
        QName serviceName = new QName("http://www.objectweb.org/addNumbers/types", "AddNumbersSOAPService");
        QName portName = new QName("http://www.objectweb.org/addNumbers/types", "AddNumbersPort");        
        String address = "http://celtix.objectweb.org/transports/jms";;

        createAndpublishEndpoint(address, serviceName, portName);
    }

    private void createAndpublishEndpoint(String address, 
                                          QName serviceName,
                                          QName portName) {
        System.out.println("Starting AddNumbers Server");

        Object implementor = new AddNumbersImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(Endpoint.WSDL_SERVICE, serviceName);
        props.put(Endpoint.WSDL_PORT, portName);
        
        Endpoint ep = Endpoint.create(implementor);
        ep.setProperties(props);
        ep.publish(address);
    }

    public static void main(String args[]) throws Exception {
        new Server();
        System.out.println("Server ready..."); 
        
        Thread.sleep(5 * 60 * 1000); 
        System.out.println("Server exiting");
        System.exit(0);
    }
}