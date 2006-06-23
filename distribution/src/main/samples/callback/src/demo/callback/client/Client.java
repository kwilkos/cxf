package demo.callback.client;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import org.objectweb.callback.SOAPService;
import org.objectweb.callback.ServerPortType;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.wsdl.WSDLManagerImpl;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;





public final class Client {

    private static final QName SERVICE_NAME 
        = new QName("http://objectweb.org/callback", "SOAPService");

    private Client() {
    } 

    public static void main(String args[]) throws Exception {
        
        Bus bus = Bus.init();
        
        Object implementor = new CallbackImpl();
        String address = "http://localhost:9005/CallbackContext/CallbackPort";
        Endpoint.publish(address, implementor);
        
        if (args.length == 0) { 
            System.out.println("please specify wsdl");
            System.exit(1); 
        }

        URL wsdlURL;
        File wsdlFile = new File(args[0]);
        if (wsdlFile.exists()) {
            wsdlURL = wsdlFile.toURL();
        } else {
            wsdlURL = new URL(args[0]);
        }
        
        SOAPService ss = new SOAPService(wsdlURL, SERVICE_NAME);
        ServerPortType port = ss.getSOAPPort();
        
        EndpointReferenceType ref = 
            EndpointReferenceUtils.getEndpointReference(new WSDLManagerImpl(bus), implementor);
        

        String resp = port.registerCallback(ref);

        System.out.println("Response from server: " + resp);
        
        bus.shutdown(true);
        
        System.exit(0); 
    }

}
