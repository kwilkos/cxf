package org.objectweb.celtix.performance.complex_type.server;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

public class Server implements Runnable {

    Bus bus;
    
    public Server(Bus b, String address) throws Exception {
        System.out.println("Starting Server");
        bus = b;
        Object implementor = new ServerImpl();
        Endpoint.publish(address, implementor);
        System.out.println("Server published");
    }

    public Server(String[] args) throws Exception {
        this(Bus.init(args), "http://localhost:20003/performance/complex_type/SoapPort");
    }
    
    public static void main(String args[]) throws Exception {
        Server server = new Server(args);
        server.run();
    }
    
    public void run() {
        System.out.println("running bus");
        System.out.println("READY");
        bus.run();
    }
    
    void shutdown(boolean wait) throws BusException {
        System.out.println("shutting down bus");
        bus.shutdown(wait);
    }

    
}
