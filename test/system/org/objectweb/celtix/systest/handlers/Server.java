package org.objectweb.celtix.systest.handlers;


import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;
import org.objectweb.celtix.systest.common.TestServerBase;

public class Server extends TestServerBase {
    
    TestHandler handler1 = new TestHandler(true); 
    TestHandler handler2 = new TestHandler(true); 

    protected void run()  {
        System.out.println(">> running server <<");
        
        Object implementor = new HandlerTestImpl();
        String address = "http://localhost:9000/HandlerTest/SoapPort";
        Endpoint ep = Endpoint.publish(address, implementor);

        List<Handler> hc = new ArrayList<Handler>(); 
        hc.add(handler1);
        hc.add(handler2);
        ep.getBinding().setHandlerChain(hc);
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
