package demo.hw.server;

import javax.xml.ws.EndpointFactory;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

public class Server implements Runnable {

    Bus bus;
    
    protected Server(String[] args) throws Exception {
        System.out.println("Starting Server");

        /**
         * Creation of the endpoint could be part of the bus initialisation
         * based on configuration. For now, do it manually.
         */

        bus = Bus.init(args);
        EndpointFactory epf = EndpointFactory.newInstance();
        Object implementor = new GreeterImpl();
        String address = "http://localhost:9000/SoapContext/SoapPort";
        epf.publish(address, implementor);
    }
    
    public static void main(String args[]) throws Exception {
        Server server = new Server(args);
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
        System.out.println("running bus");
        
        bus.run();
    }
    
    void shutdown(boolean wait) throws BusException {
        bus.shutdown(wait);
    }

    
}
