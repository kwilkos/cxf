package org.objectweb.celtix.systest.handlers;


// import java.util.ArrayList;
// import java.util.List;
import javax.xml.ws.Endpoint;
// import javax.xml.ws.handler.Handler;

import org.objectweb.celtix.systest.common.TestServerBase;

public class Server extends TestServerBase {
    
    protected void run()  {
        Object implementor = new HandlerTestImpl();
        String address = "http://localhost:9005/HandlerTest/SoapPort";
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
