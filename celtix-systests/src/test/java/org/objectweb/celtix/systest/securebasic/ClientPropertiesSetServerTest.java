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

public class ClientPropertiesSetServerTest extends ClientServerTestBase {
    
    private static ClientServerSetupBase cssb;
         
    private final QName portName = new QName("http://objectweb.org/hello_world_soap_http_secure",
                                             "SoapPort");
    private final QName secureServiceName = new QName("http://objectweb.org/hello_world_soap_http_secure",
                                                "SecureSOAPService");  
    

    public static Test suite() throws Exception {

        TestSuite suite = new TestSuite(ClientPropertiesSetServerTest.class);
        cssb = new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                System.setProperty("java.util.logging.config.file", 
                                   SecureBasicUtils.getTestDir(this) 
                                       + "ClientServerTestLog.txt");                
                SecureBasicUtils.startServer(getClass().getResource(".") 
                                             + "WantAndNeedClientAuthServer.xml",
                                             "celtix.security.configurer.http-listener.9001",
                                             null,
                                             cssb,
                                             WantAndNeedClientAuthServer.class);
            }
        };
        return cssb;
    }
    
    public void testBasicConnectionSecurityDataSetAsSystemProperties() throws Exception {
        
        URL wsdl = getClass().getResource("/wsdl/hello_world_secure.wsdl");
        assertNotNull(wsdl);
        
        SecureSOAPService service = new SecureSOAPService(wsdl, secureServiceName);
        assertNotNull(service);
        
        Greeter greeter = service.getPort(portName, Greeter.class);
        
        System.setProperty("javax.net.ssl.keyStore", 
                           SecureBasicUtils.getTestDir(this) + ".clientkeystore"); 
        System.setProperty("javax.net.ssl.keyStorePassword", "clientpass"); 
        System.setProperty("javax.net.ssl.trustStore", 
                           SecureBasicUtils.getTestDir(this) + "truststore"); 
        invoke(greeter);
    } 

    private void invoke(Greeter greeter) throws Exception {
        String response1 = new String("Hello Milestone-");
        try {       
            for (int idx = 0; idx < 2; idx++) {
                Result ret = greeter.greetMeTwoTier("Milestone-" + idx, 0);
                if (!ret.isDidPass()) {
                    fail("Sould have succeeded but instead gor error message = "
                         + ret.getFailureReason());
                }
                assertNotNull("no response received from service", ret.getReturnString());
                String exResponse = response1 + idx;
                assertEquals(exResponse, ret.getReturnString());

            }            
        } catch (UndeclaredThrowableException ex) {
            fail("Caught unexpected ex = " + ex);
            throw (Exception)ex.getCause();
        }

    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ClientPropertiesSetServerTest.class);
    }
          
    
}
