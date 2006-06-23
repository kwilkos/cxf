package org.objectweb.celtix.systest.securebasic;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;



public class WantNotNeedClientAuthInterServer extends TestServerBase {
    
    private String url;
    
    public WantNotNeedClientAuthInterServer(String urlParam) {
        String configFile = getClass().getResource(".") + "WantNotNeedClientAuthInterServer.xml";
        System.setProperty("celtix.config.file", configFile);
        url = urlParam;
    }
    
    protected void run()  {
        Object implementor = new InterGreeterImpl();
        String address = url;
        Endpoint.publish(address, implementor);
    }
    

    public static void main(String[] args) {
        
        try { 
            WantNotNeedClientAuthInterServer requireClientAuth = 
                new WantNotNeedClientAuthInterServer("https://localhost:9004/SoapContext/SoapPort"); 
            requireClientAuth.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
}
