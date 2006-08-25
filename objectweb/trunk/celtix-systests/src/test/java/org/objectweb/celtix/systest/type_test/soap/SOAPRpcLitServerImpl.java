package org.objectweb.celtix.systest.type_test.soap;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;
import org.objectweb.celtix.systest.type_test.TypeTestImpl;
import org.objectweb.type_test.rpc.TypeTestPortType;

public class SOAPRpcLitServerImpl extends TestServerBase {

    public void run()  {

        Object implementor = new SOAPTypeTestImpl();
        String address = "http://localhost:9006/SOAPService/SOAPPort/";
        Endpoint.publish(address, implementor);
    }

    public static void main(String args[]) {
        try { 
            SOAPRpcLitServerImpl s = new SOAPRpcLitServerImpl();
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
    
    @WebService(serviceName = "SOAPRpcLitService", 
                portName = "SOAPPort",
                endpointInterface = "org.objectweb.type_test.rpc.TypeTestPortType",
                targetNamespace = "http://objectweb.org/type_test/rpc")
    class SOAPTypeTestImpl extends TypeTestImpl implements TypeTestPortType {
    }
}
