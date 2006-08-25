package org.objectweb.celtix.systest.securebasic;


import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;



public class WantAndNeedClientAuthServerSetGoodDataProvider extends TestServerBase {
    
    private String url;
    
    public WantAndNeedClientAuthServerSetGoodDataProvider(String urlParam) {
        String configFile = getClass().getResource(".") 
                            + "WantAndNeedClientAuthServerSetGoodDataProvider.xml";
        System.setProperty("celtix.config.file", configFile);  
        System.setProperty("celtix.security.configurer.celtix.http-listener.9005", 
            "org.objectweb.celtix.systest.securebasic.SetAllDataSecurityDataProvider");
        url = urlParam; 
       
    }
    
    protected void run()  {
        Object implementor = new GreeterImpl();
        String configFile = System.getProperty("celtix.config.file");
        String provider = System.getProperty("celtix.security.configurer.celtix.http-listener.9005");
        System.out.println("configFile = :" + configFile + ":");
        System.out.println("provider = :" + provider + ":");
        String address = url;
        Endpoint.publish(address, implementor);
        System.out.println("done");
    }
    

    public static void main(String[] args) {
        
        try { 
            WantAndNeedClientAuthServerSetGoodDataProvider server = 
                new WantAndNeedClientAuthServerSetGoodDataProvider(
                        "https://localhost:9005/SoapContext/SoapPort"); 
            server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
}
