package org.objectweb.celtix.systest.basic;

import javax.xml.ws.EndpointFactory;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

public class Server extends TestServerBase {

    private Bus bus;
    
        
    public void run()  {
        try {
            bus = Bus.init();
            EndpointFactory epf = EndpointFactory.newInstance();
            Object implementor = new GreeterImpl();
            String address = "http://localhost:9000/SoapContext/SoapPort";
            epf.publish(address, implementor);
            ready();
        } catch (BusException ex) {
            ex.printStackTrace();
            startFailed();
        }
    }
    
    public Bus getBus() {
        return bus; 
    }
}
