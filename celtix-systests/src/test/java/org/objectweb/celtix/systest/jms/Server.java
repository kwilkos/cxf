package org.objectweb.celtix.systest.jms;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;

public class Server extends TestServerBase {


    protected void run()  {
        Object implementor = new GreeterImplTwoWayJMS();        
        Object impl2 =  new GreeterImplQueueOneWay();
        Object impl3  = new GreeterImplTopicOneWay();
        String address = "http://localhost:9000/SoapContext/SoapPort";
        Endpoint.publish(address, implementor);
        Endpoint.publish("http://testaddr.not.required/", impl2);
        Endpoint.publish("http://testaddr.not.required.topic/", impl3);
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
