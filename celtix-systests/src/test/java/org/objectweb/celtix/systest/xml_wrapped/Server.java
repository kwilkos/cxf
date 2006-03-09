package org.objectweb.celtix.systest.xml_wrapped;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;

public class Server extends TestServerBase {

    protected void run()  {
        Object implementor = new GreeterImpl();
        String address = "http://localhost:9000/XMLService/XMLPort";
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
