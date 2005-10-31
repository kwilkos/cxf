package demo.handlers.server;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

import demo.handlers.common.LoggingHandler;

public class Server implements Runnable {

    Bus bus;

    protected Server(String[] args) throws Exception {
        System.out.println("Starting AddNumbers Server");

        /**
         * Creation of the endpoint could be part of the bus initialisation
         * based on configuration. For now, do it manually.
         */

        bus = Bus.init(args);
        Object implementor = new AddNumbersImpl();
        String address = "http://localhost:9000/handlers/AddNumbersService/AddNumbersPort";
        Endpoint ep = Endpoint.publish(address, implementor);

    }

    public static void main(String args[]) throws Exception {
        Server server = new Server(args);
        server.run();
    }

    public void run() {
        System.out.println("Running Celtix Bus...");

        bus.run();
    }

    void shutdown(boolean wait) throws BusException {
        bus.shutdown(wait);
    }


}
