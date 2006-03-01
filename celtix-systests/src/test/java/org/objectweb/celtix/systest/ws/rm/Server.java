package org.objectweb.celtix.systest.ws.rm;


import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;

public class Server extends TestServerBase {
    
    private static final QName SERVICE_NAME = 
        new QName("http://objectweb.org/hello_world_soap_http", "SOAPServiceAddressing");
 
    protected void run()  {
        
        TestConfigurator tc = new TestConfigurator();
        tc.configureServer(SERVICE_NAME);
        
        GreeterImpl implementor = new GreeterImpl();
        String address = "http://localhost:9008/SoapContext/SoapPort";
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
