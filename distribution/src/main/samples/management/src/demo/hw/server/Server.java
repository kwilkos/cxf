package demo.hw.server;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.ws.Endpoint;
import org.objectweb.celtix.Bus; 

public class Server implements ServerMBean {

    private static GreeterImpl implementor;
    private String address;
    
    protected Server() throws Exception {
        System.out.println("Starting Server");
        implementor = new GreeterImpl();
        address = "http://localhost:9000/SoapContext/SoapPort";
        Endpoint.publish(address, implementor);
        //register to the bus MBServer        
        Bus bus = Bus.getCurrent();
        MBeanServer mbserver = bus.getInstrumentationManager().getMBeanServer();
        ObjectName name = new ObjectName("org.objectweb.celtix.instrumentation:type=ServerMBean,Bus="
                        + bus.getBusID() + ",name=ServerMBean");
        mbserver.registerMBean(this, name);
    }

    public String getServiceName() {
        return "SoapService";
    }

    public String getAddress() {
        return address;
    }
    
    public static void main(String args[]) throws Exception {
        new Server();
        System.out.println("Server ready..."); 
        
        Thread.sleep(5 * 60 * 1000); 

        System.out.println("Server exiting");
        implementor.shutDown();
        System.exit(0);
    }
    
}
