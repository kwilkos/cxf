package org.apache.cxf.performance.basic_type.server;

import javax.xml.ws.Endpoint;


public class Server implements Runnable {

    
    public Server(String address) throws Exception {
        System.out.println("Starting Server");
        Object implementor = new ServerImpl();
        Endpoint.publish(address, implementor);
        System.out.println("Server published");
    }

    public Server(String[] args) throws Exception {
        this("http://localhost:20000/performance/basic_type/SoapPort");
    }
    
    public static void main(String args[]) throws Exception {
        Server server = new Server(args);
        server.run();
    }
    
    public void run() {
        System.out.println("running server");
        System.out.println(" READY ");
        
    }
    
    void shutdown(boolean wait) {
        System.out.println("shutting down server");
    }
}
