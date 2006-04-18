package org.objectweb.celtix.systest.securebasic;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_soap_http_secure.Greeter;
import org.objectweb.hello_world_soap_http_secure.SecureSOAPService;
import org.objectweb.hello_world_soap_http_secure.types.Result;

public class ClientPropertiesNotSetServerTest extends ClientServerTestBase {
    
    private static ClientServerSetupBase cssb;
       
    private final QName portName = new QName("http://objectweb.org/hello_world_soap_http_secure",
                                             "SoapPortClientPropertiesNotSet");
    private final QName secureServiceName = new QName("http://objectweb.org/hello_world_soap_http_secure",
                                                "SecureSOAPServiceClientPropertiesNotSet");  
    
   

    public static Test suite() throws Exception {
        
        TestSuite suite = new TestSuite(ClientPropertiesNotSetServerTest.class);
        cssb = new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                System.setProperty("java.util.logging.config.file", 
                                   getClass().getResource(".") + "ClientPropertiesNotSetServerTestLog.txt");
                SecureBasicUtils.startServer(getClass().getResource(".") 
                                             + "WantAndNeedClientAuthServerClientPropertiesNotSet.xml",
                                             "celtix.security.configurer.http-listener.9015",
                                             null,
                                             cssb,
                                             WantAndNeedClientAuthServerClientPropertiesNotSet.class);  
            }
        };

        return cssb;
    }
  
   
    public void testBasicConnectionClientNoPropertiesSet() throws Exception {
        System.setProperty("java.util.logging.config.file", 
                           SecureBasicUtils.getTestDir(this) + "clientlog.txt");
        System.setProperty("celtix.config.file", 
                           getClass().getResource(".") +  "client.xml");
        
        URL wsdl = getClass().getResource("/wsdl/hello_world_secure.wsdl");
        assertNotNull(wsdl);
        
        SecureSOAPService service = new SecureSOAPService(wsdl, secureServiceName);
        assertNotNull(service);
        
        Greeter greeter = service.getPort(portName, Greeter.class);
        
        invoke(greeter);
        
    } 
    
    private void invoke(Greeter greeter) throws Exception {

        try {       
            Result ret = greeter.greetMeTwoTier("Milestone", 0);
            if (ret.isDidPass()) {
                fail("Should not have succeeded, client properties not setup");
            }
        } catch (UndeclaredThrowableException ex) {
            assertTrue("Failed to catch expected exception, instead caught ex.getClass() = " 
                       + ex.getClass(), ex != null);
        }
        

    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ClientPropertiesNotSetServerTest.class);
    }
    
      
    
}
