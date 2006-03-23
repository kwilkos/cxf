package org.objectweb.celtix.systest.ws.rm;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;

public class Server extends TestServerBase {

    protected void run() {

        ControlImpl implementor = new ControlImpl();
        String address = "http://localhost:9001/SoapContext/ControlPort";
        Endpoint.publish(address, implementor);

    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            s.start();            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally {
            System.out.println("done!");
        }
    }
}
