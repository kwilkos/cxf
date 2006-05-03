package org.objectweb.celtix.systest.callback;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.callback.SOAPService;
import org.objectweb.callback.ServerPortType;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.wsdl.WSDLManagerImpl;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;


public final class CallbackClientServerTest extends ClientServerTestBase {

    private static final QName SERVICE_NAME 
        = new QName("http://objectweb.org/callback", "SOAPService");
    //private static final QName CALLBACK_SERVICE_NAME 
    //    = new QName("http://objectweb.org/callback", "CallbackService");

    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(CallbackClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }

    public void testCallback() {
   
        Bus bus = null;
        try {
            bus = Bus.init();
        } catch (BusException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Object implementor = new CallbackImpl();
        String address = "http://localhost:9005/CallbackContext/CallbackPort";
        Endpoint.publish(address, implementor);
        
        URL wsdlURL = getClass().getResource("/wsdl/basic_callback.wsdl");
        
        SOAPService ss = new SOAPService(wsdlURL, SERVICE_NAME);
        ServerPortType port = ss.getSOAPPort();
       
        EndpointReferenceType ref = null;
        try {
            ref = EndpointReferenceUtils.getEndpointReference(new WSDLManagerImpl(bus), implementor);
        } catch (BusException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        String resp = port.registerCallback(ref);

        assertTrue(resp.equals("registerCallback called"));
        
        try {
            bus.shutdown(true);
        } catch (BusException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //System.exit(0); 

    }

}

