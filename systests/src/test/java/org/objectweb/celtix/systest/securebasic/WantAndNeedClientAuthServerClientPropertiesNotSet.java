package org.objectweb.celtix.systest.securebasic;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;



public class WantAndNeedClientAuthServerClientPropertiesNotSet extends TestServerBase {
    
    private String url;
    
    public WantAndNeedClientAuthServerClientPropertiesNotSet(String urlParam) {
        String configFile = getClass().getResource(".") 
            +  "WantAndNeedClientAuthServerClientPropertiesNotSet.xml";
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
            WantAndNeedClientAuthServerClientPropertiesNotSet requireClientAuth = 
                new WantAndNeedClientAuthServerClientPropertiesNotSet(
                               "https://localhost:9015/SoapContext/SoapPort"); 

            ServerThread st1 = new ServerThread(requireClientAuth);
            st1.start(); 
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
}

