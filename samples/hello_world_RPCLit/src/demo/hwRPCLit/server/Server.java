package demo.hwRPCLit.server;

import javax.xml.ws.Endpoint;

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
        Object implementor = new GreeterRPCLitImpl();
        String address = "http://localhost:9000/SoapContext/SoapPort";
        Endpoint.publish(address, implementor);
    }

    public static void main(String args[]) throws Exception {
        Server server = new Server(args);
	    server.run();
    }

    public void run() {
        System.out.println("running bus");

        bus.run();
    }

    void shutdown(boolean wait) throws BusException {
        bus.shutdown(wait);
    }


}
