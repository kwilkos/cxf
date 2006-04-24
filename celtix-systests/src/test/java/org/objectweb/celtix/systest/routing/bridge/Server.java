package org.objectweb.celtix.systest.routing.bridge;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;
import org.objectweb.celtix.systest.routing.DocLitWrappedImpl;

public class Server extends TestServerBase {
    private static final String XML_BINDINGID = 
        new String("http://celtix.objectweb.org/bindings/xmlformat"); 
    protected void run()  {
        QName serviceName = new QName("http://objectweb.org/hello_world_doc_lit", "XMLService1");
        QName portName = new QName("http://objectweb.org/hello_world_doc_lit", "XMLPort1");
        
        String address = "http://localhost:9002/XMLService1/XMLPort1";
        createAndpublishEndpoint(address, serviceName, 
                                 portName, XML_BINDINGID);

        serviceName = new QName("http://objectweb.org/hello_world_doc_lit", "XMLService2");
        portName = new QName("http://objectweb.org/hello_world_doc_lit", "XMLPort2");
        address = new String("http://localhost:0/JMSService/JMSPort");
        createAndpublishEndpoint(address, serviceName, 
                                 portName, XML_BINDINGID);
        
        serviceName = new QName("http://objectweb.org/hello_world_doc_lit", "SOAPService5");
        portName = new QName("http://objectweb.org/hello_world_doc_lit", "SoapPort5");
        address = new String("http://localhost:9002/SOAPService5/SoapPort5");
        createAndpublishEndpoint(address, serviceName, portName, null);
        
        serviceName = new QName("http://objectweb.org/hello_world_doc_lit", "SOAPService2");
        portName = new QName("http://objectweb.org/hello_world_doc_lit", "SoapPort2");
        address = new String("http://localhost:0/JMSService/JMSPort");
        //createAndpublishEndpoint(address, serviceName, portName, null);        
    }
    
    private void createAndpublishEndpoint(String address, 
                                          QName serviceName,
                                          QName portName,
                                          String bindingId) {
        Object implementor = new DocLitWrappedImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(Endpoint.WSDL_SERVICE, serviceName);
        props.put(Endpoint.WSDL_PORT, portName);
        
        Endpoint ep = null;
        if (null != bindingId) {
            ep = Endpoint.create(bindingId, implementor);
        } else {
            ep = Endpoint.create(implementor);
        }
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
