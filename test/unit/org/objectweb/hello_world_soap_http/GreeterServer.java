package org.objectweb.hello_world_soap_http;

import javax.xml.ws.EndpointFactory;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

public class GreeterServer implements Runnable {

    Bus bus;
    
    protected GreeterServer(String[] args) throws Exception {
        System.out.println("Starting Server");

        /**
         * Creation of the endpoint could be part of the bus initialisation
         * based on configuration. For now, do it manually.
         */

        bus = Bus.init(args);
        EndpointFactory epf = EndpointFactory.newInstance();
        Object implementor = new CorrectlyAnnotatedGreeterImpl();
        String address = "http://localhost:8080/hello_world_soap_http";
        epf.publish(address, implementor);
    }
    
    public static void main(String args[]) throws Exception {
        GreeterServer server = new GreeterServer(args);
        Thread t = new Thread(server);
        t.start();
        try {
            Thread.sleep(100);          
        } catch (InterruptedException ex) {
            // ignore
        }
        System.out.print("Press any key to terminate the server ...");
        int c = System.in.read();
        
        server.shutdown(true);
        
        t.join();  
    }
    
    public void run() {
        bus.run();
    }
    
    void shutdown(boolean wait) throws BusException {
        bus.shutdown(wait);
    }

    
}
