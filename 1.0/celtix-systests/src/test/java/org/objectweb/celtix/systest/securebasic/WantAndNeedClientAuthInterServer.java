package org.objectweb.celtix.systest.securebasic;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;



public class WantAndNeedClientAuthInterServer extends TestServerBase {
    
    private String url;
    
    public WantAndNeedClientAuthInterServer(String urlParam) {
        String configFile = getClass().getResource(".")
                            + "WantAndNeedClientAuthInterServer.xml";
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
            WantAndNeedClientAuthInterServer requireClientAuth = 
                new WantAndNeedClientAuthInterServer("https://localhost:9003/SoapContext/SoapPort"); 
            requireClientAuth.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
}
