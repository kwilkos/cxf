package org.objectweb.celtix.systest.soapheader;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;

public class Server extends TestServerBase {
    
    
    protected void run()  {
        Object implementor = new TestHeaderImpl();
        String address = "http://localhost:9104/SoapHeaderContext/SoapHeaderPort";
        Endpoint ep = Endpoint.create(implementor);
        Map<String, Object> props = new HashMap<String, Object>(2);
        props.put(Endpoint.WSDL_SERVICE, new QName("http://objectweb.org/header_test", "SOAPHeaderService"));
        props.put(Endpoint.WSDL_PORT, new QName("http://objectweb.org/header_test", "SoapHeaderPort"));
        ep.setProperties(props);
        ep.publish(address);
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
