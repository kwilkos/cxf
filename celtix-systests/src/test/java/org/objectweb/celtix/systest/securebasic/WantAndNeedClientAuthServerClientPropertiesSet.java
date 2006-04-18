package org.objectweb.celtix.systest.securebasic;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;



public class WantAndNeedClientAuthServerClientPropertiesSet extends TestServerBase {
    
    private String url;
    
    public WantAndNeedClientAuthServerClientPropertiesSet(String urlParam) {
        String configFile = getClass().getResource(".") 
            +  "WantAndNeedClientAuthServerClientPropertiesSet.xml";
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
            WantAndNeedClientAuthServerClientPropertiesSet requireClientAuth = 
                new WantAndNeedClientAuthServerClientPropertiesSet(
                              "https://localhost:9014/SoapContext/SoapPort"); 

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

