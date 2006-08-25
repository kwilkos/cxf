package org.objectweb.hello_world_soap_http;

import javax.xml.ws.EndpointFactory;

import org.objectweb.celtix.Bus;

public class GreeterServerMain {

    protected GreeterServerMain() {
    }

    public static void main(String args[]) throws Exception {
        System.out.println("Starting Server");

        /**
         * Creation of the endpoint could be part of the bus initialisation
         * based on configuration. For now, do it manually.
         */

        Bus bus = Bus.init(args);
        Runtime.getRuntime().addShutdownHook(
            new Thread(new GreeterServerMain().new TerminationHandler(bus, true)));
        EndpointFactory epf = EndpointFactory.newInstance();
        Object implementor = new AnnotatedGreeterImpl();
        String address = "http://loalhost:8080/hello_world_soap_http";
        epf.publish(address, implementor);
        bus.run();
    }

    private class TerminationHandler implements Runnable {
        private Bus bus;
        private boolean processRemainingTasks;

        TerminationHandler(Bus b, boolean p) {
            bus = b;
            processRemainingTasks = p;
        }

        public void run() {
            try {
                bus.shutdown(processRemainingTasks);
            } catch (Exception ex) {
                System.err.println("Failed to shutdown the bus:\n" + ex.getMessage());
            }
        }
    }
}
