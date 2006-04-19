package org.objectweb.celtix.systest.type_test.xml;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;
import org.objectweb.celtix.systest.type_test.TypeTestImpl;
import org.objectweb.type_test.doc.TypeTestPortType;

public class XMLServerImpl extends TestServerBase {

    public void run()  {

        Object implementor = new XMLTypeTestImpl();
        String address = "http://localhost:9008/XMLService/XMLPort/";
        Endpoint.publish(address, implementor);
    }

    public static void main(String args[]) {
        try { 
            XMLServerImpl s = new XMLServerImpl();
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
    
    @WebService(serviceName = "XMLService", 
                portName = "XMLPort",
                endpointInterface = "org.objectweb.type_test.doc.TypeTestPortType")
    @javax.xml.ws.BindingType(value = "http://celtix.objectweb.org/bindings/xmlformat")
    class XMLTypeTestImpl extends TypeTestImpl implements TypeTestPortType {
    }
}
