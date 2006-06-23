package org.objectweb.celtix.systest.securebasic;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;



public class WantAndNeedClientAuthServerPKCS12 extends TestServerBase {
    
    private String url;
    
    public WantAndNeedClientAuthServerPKCS12(String urlParam) {
        String configFile = getClass().getResource(".") +  "WantAndNeedClientAuthServerPKCS12.xml";
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
            WantAndNeedClientAuthServerPKCS12 requireClientAuth = 
                new WantAndNeedClientAuthServerPKCS12("https://localhost:9007/SoapContext/SoapPort"); 
            requireClientAuth.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
}
