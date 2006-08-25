package org.objectweb.celtix.systest.securebasic;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;



public class WantAndNeedClientAuthServerSetBadDataProvider extends TestServerBase {
    
    private String url;
    
    public WantAndNeedClientAuthServerSetBadDataProvider(String urlParam) {
        String configFile = getClass().getResource(".") 
                            + "WantAndNeedClientAuthServerSetBadDataProvider.xml";
        System.setProperty("celtix.config.file", configFile);
        System.setProperty("celtix.security.configurer.celtix.http-listener.9006", 
            "org.objectweb.celtix.systest.securebasic.SetBadDataSecurityDataProvider");
        url = urlParam;
    }
    
    protected void run()  {
        
        
        Object implementor = new GreeterImpl();
        String address = url;
        String configFile = System.getProperty("celtix.config.file");
        String provider = System.getProperty("celtix.security.configurer.celtix.http-listener.9006");
        System.out.println("configFile = :" + configFile + ":");
        System.out.println("provider = :" + provider + ":");
        Endpoint.publish(address, implementor);
    }
    

    public static void main(String[] args) {
        
        try { 
            WantAndNeedClientAuthServerSetBadDataProvider server = 
                new WantAndNeedClientAuthServerSetBadDataProvider(
                        "https://localhost:9006/SoapContext/SoapPort"); 
            server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
}
