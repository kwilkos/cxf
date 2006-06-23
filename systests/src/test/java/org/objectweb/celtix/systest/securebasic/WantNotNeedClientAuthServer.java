package org.objectweb.celtix.systest.securebasic;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;



public class WantNotNeedClientAuthServer extends TestServerBase {
    
    private String url;
    
    public WantNotNeedClientAuthServer(String urlParam) {
        String configFile = getClass().getResource(".") +  "WantNotNeedClientAuthServer.xml";
        System.setProperty("celtix.config.file", configFile);
        url = urlParam;
    }
    
    protected void run()  {
        Object implementor = new GreeterImpl();
        String address = url;
        Endpoint.publish(address, implementor);
    }
    

    public static void main(String[] args) {
        
        try { 
            WantNotNeedClientAuthServer dontRequireClientAuth = 
                new WantNotNeedClientAuthServer("https://localhost:9002/SoapContext/SoapPort"); 
            dontRequireClientAuth.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
}
