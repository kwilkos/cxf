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

public class ClientPropertiesReadFromConfigSetServerTest extends ClientServerTestBase {
  
    private static final int REPEAT_NUM_TIMES = 1;
    
    private static ClientServerSetupBase cssb;
    
   

    public static Test suite() throws Exception {

        TestSuite suite = new TestSuite(ClientPropertiesReadFromConfigSetServerTest.class);
        
        cssb = new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                SecureBasicUtils.startServer(getClass().getResource(".") 
                                             + "WantAndNeedClientAuthServer.xml",
                                             "celtix.security.configurer.http-listener.9001",
                                             null, cssb, WantAndNeedClientAuthServer.class);
                SecureBasicUtils.startServer(getClass().getResource(".")
                                             + "WantNotNeedClientAuthServer.xml",
                                             "celtix.security.configurer.http-listener.9002",
                                             null, cssb, WantNotNeedClientAuthServer.class);
                SecureBasicUtils.startServer(getClass().getResource(".") 
                                             + "WantAndNeedClientAuthInterServer.xml",
                                             "celtix.security.configurer.http-listener.9003",
                                             null, cssb, WantAndNeedClientAuthInterServer.class);
                SecureBasicUtils.startServer(getClass().getResource(".") 
                                             + "WantNotNeedClientAuthInterServer.xml",
                                             "celtix.security.configurer.http-listener.9004",
                                             null, cssb, WantNotNeedClientAuthInterServer.class); 
                SecureBasicUtils.startServer(getClass().getResource(".") 
                                             + "WantAndNeedClientAuthServerSetGoodDataProvider.xml",
                                             "celtix.security.configurer.celtix.http-listener.9005",
                                               "org.objectweb.celtix.systest.securebasic."
                                             + "SetAllDataSecurityDataProvider",
                                             cssb, WantAndNeedClientAuthServerSetGoodDataProvider.class);
                SecureBasicUtils.startServer(getClass().getResource(".") 
                                             + "WantAndNeedClientAuthServerSetBadDataProvider.xml",
                                             "celtix.security.configurer.celtix.http-listener.9006",
                                             "org.objectweb.celtix.systest.securebasic." 
                                                 + "SetBadDataSecurityDataProvider",
                                             cssb, WantAndNeedClientAuthServerSetBadDataProvider.class); 
                SecureBasicUtils.startServer(getClass().getResource(".") 
                                             + "WantAndNeedClientAuthServerPKCS12.xml",
                                             "celtix.security.configurer.celtix.http-listener.9007",
                                             null, cssb, WantAndNeedClientAuthServerPKCS12.class); 
                                            
            }
        };
        return cssb;
    }
    
  
    public void testTwoTiers() throws Exception {
        String configFile = getClass().getResource(".") + "client.xml";
        System.setProperty("celtix.config.file", configFile);
        
        for (int index = 0; index < Matrix.TWO_TIER_TESTS.length; index++) {
            URL wsdl = getClass().getResource("/wsdl/" + Matrix.TWO_TIER_TESTS[index].clientData.clientWsdl);
            assertNotNull(wsdl);
            QName serviceName = 
                new QName("http://objectweb.org/hello_world_soap_http_secure",
                          Matrix.TWO_TIER_TESTS[index].clientData.clientServiceName); 
            SecureSOAPService service = new SecureSOAPService(wsdl, serviceName);
            assertNotNull(service);
            
            QName portName = new QName("http://objectweb.org/hello_world_soap_http_secure",
                                       Matrix.TWO_TIER_TESTS[index].clientData.clientPortName);
            Greeter greeter = service.getPort(portName, Greeter.class);
            String propStr = "celtix.security.configurer" 
                + ".celtix.{http://objectweb.org/hello_world_soap_http_secure}" 
                + Matrix.TWO_TIER_TESTS[index].clientData.clientServiceName + "/" 
                + Matrix.TWO_TIER_TESTS[index].clientData.clientPortName + ".http-client";
            if (Matrix.TWO_TIER_TESTS[index].clientData.securityConfigurer != null) {
                System.setProperty(propStr, 
                                   Matrix.TWO_TIER_TESTS[index].clientData.securityConfigurer);
                
            }
            invokeTwoTier(greeter, index);
            if (System.getProperty("propStr") != null) {
                System.getProperties().remove(propStr);
            }
        }
    }      
    
 
     
    private void invokeTwoTier(Greeter greeter, int index) throws Exception {
        String response1 = new String("Hello Milestone-");
        try {       
            for (int idx = 0; idx < REPEAT_NUM_TIMES; idx++) {
                Result ret = greeter.greetMeTwoTier("Milestone-" + idx, index);
                if (!Matrix.TWO_TIER_TESTS[index].clientData.clientExpectSuccess) {
                    fail("Expected to FAIL but didn't, index = " + index);
                }
                assertNotNull("no response received from service", ret.getReturnString());
                String exResponse = response1 + idx;
                assertEquals(exResponse, ret.getReturnString());
                
            }            
        } catch (UndeclaredThrowableException ex) {
            if (Matrix.TWO_TIER_TESTS[index].clientData.clientExpectSuccess) {
                fail("Caught unexpected ex = " + ex
                     + ", ex message is " + ex.getMessage()
                     + ", index = " + index);
                throw (Exception)ex.getCause();
            } 

        }

    }
    
    public void testThreeTiers() throws Exception {
        String configFile = getClass().getResource(".") +  "client.xml";
        System.setProperty("celtix.config.file", configFile);
        
        for (int index = 0; index < Matrix.THREE_TIER_TESTS.length; index++) {
            URL wsdl = getClass().getResource("/wsdl/" 
                                              + Matrix.THREE_TIER_TESTS[index].clientData.clientWsdl);
            assertNotNull(wsdl);
            QName serviceName = 
                new QName("http://objectweb.org/hello_world_soap_http_secure",
                          Matrix.THREE_TIER_TESTS[index].clientData.clientServiceName); 
            SecureSOAPService service = new SecureSOAPService(wsdl, serviceName);
            assertNotNull(service);
            
            QName portName = new QName("http://objectweb.org/hello_world_soap_http_secure",
                                       Matrix.THREE_TIER_TESTS[index].clientData.clientPortName);
            Greeter greeter = service.getPort(portName, Greeter.class);
            
            invokeThreeTier(greeter, index);
        }
    }  

     
    private void invokeThreeTier(Greeter greeter, int index) throws Exception {
        String response1 = new String("Hello Milestone-");
        try {       
            for (int idx = 0; idx < REPEAT_NUM_TIMES; idx++) {
                Result ret = greeter.greetMeThreeTier("Milestone-" + idx, index);
                if (!Matrix.THREE_TIER_TESTS[index].clientData.clientExpectSuccess) {
                    fail("Expected to FAIL but didn't");
                } else if (!ret.isDidPass()) {
                    fail("The inter server reported the following error : " + ret.getFailureReason());
                }
                assertNotNull("no response received from service", ret.getReturnString());
                String exResponse = response1 + idx;
                assertEquals(exResponse, ret.getReturnString());
                
            }            
        } catch (UndeclaredThrowableException ex) {
            if (Matrix.THREE_TIER_TESTS[index].clientData.clientExpectSuccess) {
                fail("Caught unexpected exception for test index, " + index + ",ex = " + ex);
                throw (Exception)ex.getCause();
            } 
            
        }

    }
    
 
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ClientPropertiesReadFromConfigSetServerTest.class);
    }
          
    
}
