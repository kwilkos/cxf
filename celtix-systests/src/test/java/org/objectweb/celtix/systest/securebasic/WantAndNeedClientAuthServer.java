package org.objectweb.celtix.systest.securebasic;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;



public class WantAndNeedClientAuthServer extends TestServerBase {
    
    private String url;
    
    public WantAndNeedClientAuthServer(String urlParam) {
        String configFile = getClass().getResource(".") +  "WantAndNeedClientAuthServer.xml";
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
            WantAndNeedClientAuthServer requireClientAuth = 
                new WantAndNeedClientAuthServer("https://localhost:9001/SoapContext/SoapPort"); 

            ServerThread st1 = new ServerThread(requireClientAuth);
            st1.start(); 
            
            WantAndNeedClientAuthServer requireClientAuthDiffernetCiphersuite = 
                new WantAndNeedClientAuthServer("https://localhost:9011/SoapContext/SoapPort"); 

            ServerThread st2 = new ServerThread(requireClientAuthDiffernetCiphersuite);
            st2.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
}

class ServerThread extends Thread {
    TestServerBase server; 
    public ServerThread(TestServerBase t) {
        server = t;
    }
    
    public void run() {
        server.start();
    }
}
