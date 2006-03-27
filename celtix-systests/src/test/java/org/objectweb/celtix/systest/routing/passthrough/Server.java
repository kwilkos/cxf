package org.objectweb.celtix.systest.routing.passthrough;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;
import org.objectweb.celtix.systest.routing.DocLitWrappedImpl;

public class Server extends TestServerBase {

    protected void run()  {
        QName serviceName = new QName("http://objectweb.org/hello_world_doc_lit", "SOAPService");
        QName portName = new QName("http://objectweb.org/hello_world_doc_lit", "SOAPPort");
        
        String address = "http://localhost:9002/HTTPSoapServiceDestination/HTTPSoapPortDestination";
        createAndpublishEndpoint(address, serviceName, portName);

        serviceName = new QName("http://objectweb.org/hello_world_doc_lit", "SOAPService4");
        portName = new QName("http://objectweb.org/hello_world_doc_lit", "SOAPPort4");
        address = new String("http://localhost:0/JMSService/JMSPort");
        createAndpublishEndpoint(address, serviceName, portName);        
    }
    
    private void createAndpublishEndpoint(String address, 
                                          QName serviceName,
                                          QName portName) {
        Object implementor = new DocLitWrappedImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(Endpoint.WSDL_SERVICE, serviceName);
        props.put(Endpoint.WSDL_PORT, portName);
        
        Endpoint ep = Endpoint.create(implementor);
        ep.setProperties(props);
        ep.publish(address);
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            s.start();
            //s.run();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally {
            System.out.println("done!");
        }
    }
}
