package org.objectweb.celtix.systest.soapheader;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;

public class Server extends TestServerBase {
    
    
    protected void run()  {
        Object implementor = new TestHeaderImpl();
        String address = "http://localhost:9104/SoapHeaderContext/SoapHeaderPort";
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
