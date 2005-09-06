package org.objectweb.celtix.systest.basic;

import javax.xml.ws.EndpointFactory;

import org.objectweb.celtix.systest.common.TestServerBase;

public class Server extends TestServerBase {
    
    
    protected void run()  {
        EndpointFactory epf = EndpointFactory.newInstance();
        Object implementor = new GreeterImpl();
        String address = "http://localhost:9000/SoapContext/SoapPort";
        epf.publish(address, implementor);
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
