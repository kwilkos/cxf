package org.objectweb.celtix.systest.type_test.soap;


import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;
import org.objectweb.celtix.systest.type_test.TypeTestImpl;

public class SOAPServerImpl extends TestServerBase {

    public void run()  {

        Object implementor = new SOAPTypeTestImpl();
        String address = "http://localhost:9200/SOAPService/SOAPPort/";
        Endpoint.publish(address, implementor);
    }

    public static void main(String args[]) {
        try { 
            SOAPServerImpl s = new SOAPServerImpl();
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
    
    @WebService(serviceName = "SOAPService", portName = "SOAPPort",
                name = "TypeTestPortType",
                targetNamespace = "http://objectweb.org/type_test")
    class SOAPTypeTestImpl extends TypeTestImpl {
    }
}
