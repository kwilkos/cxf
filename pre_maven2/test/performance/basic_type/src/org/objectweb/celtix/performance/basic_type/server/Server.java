package org.objectweb.celtix.performance.basic_type.server;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

public class Server implements Runnable {

    Bus bus;
    
    protected Server(String[] args) throws Exception {
        System.out.println("Starting Server");
        
        bus = Bus.init(args);
        Object implementor = new ServerImpl();
        String address = "http://localhost:20000/performance/basic_type/SoapPort";
        Endpoint.publish(address, implementor);
    }
    
    public static void main(String args[]) throws Exception {
        Server server = new Server(args);
        server.run();
    }
    
    public void run() {
        System.out.println("running bus");
        System.out.println(" READY ");
        bus.run();
        
    }
    
    void shutdown(boolean wait) throws BusException {
        System.out.println("shutting down bus");
        bus.shutdown(wait);
    }
}
