package org.objectweb.celtix.transports.https;

import java.net.URL;
import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.classextension.EasyMock;
import org.mortbay.http.SslListener;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.configuration.security.SSLServerPolicy;
import org.objectweb.celtix.configuration.Configuration;

public class JettySslListenerConfigurerTest extends TestCase {

    
    private static final String DROP_BACK_SRC_DIR = 
        "../../../../../../../src/test/java/org/objectweb/celtix/transports/https/";

    Bus bus;
    private Configuration configuration;

    


    public JettySslListenerConfigurerTest(String arg0) {
        super(arg0);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(JettySslListenerConfigurerTest.class);
        return new TestSetup(suite) {
            protected void tearDown() throws Exception {
                super.tearDown();
            }
        };
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(JettySslListenerConfigurerTest.class);
    }

    public void setUp() throws BusException {
        bus = EasyMock.createMock(Bus.class);
        configuration = EasyMock.createMock(Configuration.class);
    }

    public void tearDown() throws Exception {
        EasyMock.reset(bus);
        EasyMock.reset(configuration);
        Properties props = System.getProperties();
        props.remove("javax.net.ssl.trustStore");
        props.remove("javax.net.ssl.keyStore");
    }
    
    public void testSecurityConfigurer() {
        try {
            System.setProperty("celtix.security.configurer.celtix.null",
                               "org.objectweb.celtix.transports.https.SetAllDataSecurityDataProvider");
            SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
            TestHandler handler = new TestHandler();
            JettySslListenerConfigurer jettySslListenerConfigurer = 
                                createJettySslListenerConfigurer(sslServerPolicy, 
                                                               "https://dummyurl",
                                                               handler);
    
            jettySslListenerConfigurer.configure();
            SslListener sslListener = jettySslListenerConfigurer.getSslListener(); 
            assertTrue("Keystore not set properly", 
                       sslListener.getKeystore().contains("resources/defaultkeystore"));
            String trustStr = System.getProperty("javax.net.ssl.trustStore");
            assertTrue("Trust store loaded success message not present", 
                       trustStr.contains("resources/defaulttruststore"));
            assertTrue("Keystore type not being read", 
                       sslListener.getKeystoreType().equals("JKS"));
            assertTrue("Keystore password not being read", 
                       sslServerPolicy.getKeystorePassword().equals("defaultkeypass"));
            assertTrue("Key password not being read", 
                       sslServerPolicy.getKeyPassword().equals("defaultkeypass"));  
            
            assertTrue("Ciphersuites is not being read from the config provider", 
                       sslListener.getCipherSuites()[0].equals("MyCipher")); 
            assertTrue("Truststore type not being read", 
                       handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                      + "TrustStoreType"));          
    
            assertTrue("Secure socket protocol not being read", 
                       handler.checkLogContainsString("The secure socket protocol has been set to TLSv1."));
            assertTrue("Session caching set but no warning about not supported", 
                       handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                      + "SessionCaching"));
            assertTrue("SessionCacheKey caching set but no warning about not supported", 
                       handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                      + "SessionCacheKey"));
            assertTrue("MaxChainLength caching set but no warning about not supported", 
                       handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                      + "MaxChainLength"));
            assertTrue("CertValidator caching set but no warning about not supported", 
                       handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                      + "CertValidator"));
            
            System.setProperty("celtix.security.configurer.celtix.null",
                "org.objectweb.celtix.bus.transports.https.DoesNotExistSetAllDataSecurityDataProvider");
            SSLServerPolicy sslServerPolicy2 = new SSLServerPolicy();
            TestHandler handler2 = new TestHandler();
            EasyMock.reset(configuration);
            configuration = EasyMock.createMock(Configuration.class);
            JettySslListenerConfigurer jettySslListenerConfigurer2 = 
                createJettySslListenerConfigurer(sslServerPolicy2, 
                                            "https://dummyurl",
                                            handler2);
            sslServerPolicy2.setKeyPassword("test");
            sslServerPolicy2.setKeystorePassword("test1");
            jettySslListenerConfigurer2.configure();
            
            assertTrue("Keystore not set properly", 
                       handler2.checkLogContainsString("Failure invoking on custom security configurer "
                                 + "org.objectweb.celtix.bus.transports.https."
                                 + "DoesNotExistSetAllDataSecurityDataProvider, "));
        } finally {
            System.setProperty("celtix.security.configurer.celtix.null", "");
        }
    }
    
    public void testSetAllData() {
        
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
        sslServerPolicy.setKeystore(keyStoreStr);
        sslServerPolicy.setKeystoreType("JKS");
        
        sslServerPolicy.setKeyPassword("defaultkeypass");
        sslServerPolicy.setKeystorePassword("defaultkeypass");
        sslServerPolicy.setTrustStoreType("JKS");
        sslServerPolicy.setTrustStoreAlgorithm("JKS");
        sslServerPolicy.setSecureSocketProtocol("TLSv1");
        sslServerPolicy.setSessionCacheKey("Anything");
        sslServerPolicy.setSessionCaching(true);
        sslServerPolicy.setMaxChainLength(new Long(2));
        sslServerPolicy.setCertValidator("Anything");

        
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslServerPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslListenerConfigurer jettySslListenerConfigurer = 
                            createJettySslListenerConfigurer(sslServerPolicy, 
                                                           "https://dummyurl",
                                                           handler);

        jettySslListenerConfigurer.configure();
        SslListener sslListener = 
                jettySslListenerConfigurer.getSslListener();
        
        assertTrue("Keystore not set properly", 
                   sslListener.getKeystore().contains("resources/defaultkeystore"));
        String trustStr = System.getProperty("javax.net.ssl.trustStore");
        assertTrue("Trust store loaded success message not present", 
                   trustStr.contains("resources/defaulttruststore"));
        assertTrue("Keystore type not being read", 
                   sslListener.getKeystoreType().equals("JKS"));
        assertTrue("Keystore password not being read", 
                   sslServerPolicy.getKeystorePassword().equals("defaultkeypass"));
        assertTrue("Key password not being read", 
                   sslServerPolicy.getKeyPassword().equals("defaultkeypass"));  
        
        assertTrue("Ciphersuites is being being read from somewhere unknown", 
                   sslListener.getCipherSuites() == null); 
        assertTrue("Truststore type not being read", 
                   handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                  + "TrustStoreType"));          

        assertTrue("Secure socket protocol not being read", 
                   handler.checkLogContainsString("The secure socket protocol has been set to TLSv1."));
        assertTrue("Session caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                  + "SessionCaching"));
        assertTrue("SessionCacheKey caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                  + "SessionCacheKey"));
        assertTrue("MaxChainLength caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                  + "MaxChainLength"));
        assertTrue("CertValidator caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                  + "CertValidator"));
    }
    
    public void testSetAllDataExceptKeystoreAndTrustStore() {
        
        SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
        sslServerPolicy.setKeystore(null);
        sslServerPolicy.setKeystoreType("JKS");
        
        sslServerPolicy.setKeyPassword("defaultkeypass");
        sslServerPolicy.setKeystorePassword("defaultkeypass");
        sslServerPolicy.setTrustStoreType("JKS");
        sslServerPolicy.setTrustStoreAlgorithm("JKS");
        sslServerPolicy.setSecureSocketProtocol("TLSv1");
        sslServerPolicy.setSessionCacheKey("Anything");
        sslServerPolicy.setSessionCaching(true);
        sslServerPolicy.setMaxChainLength(new Long(2));
        sslServerPolicy.setCertValidator("Anything"); 

        
        
        sslServerPolicy.setTrustStore(null);
        TestHandler handler = new TestHandler();
        JettySslListenerConfigurer jettySslListenerConfigurer = 
                            createJettySslListenerConfigurer(sslServerPolicy, 
                                                           "https://dummyurl",
                                                           handler);

        jettySslListenerConfigurer.configure();
        SslListener sslListener = 
                jettySslListenerConfigurer.getSslListener();
        
        assertTrue("Keystore not set properly, sslListener.getKeystore() = " + sslListener.getKeystore(), 
                   sslListener.getKeystore().contains(".keystore"));
        String trustStr = System.getProperty("javax.net.ssl.trustStore");
        assertTrue("Trust store loaded success message not present", 
                   trustStr.contains("cacerts"));
        assertTrue("Keystore type not being read", 
                   sslListener.getKeystoreType().equals("JKS"));
        assertTrue("Keystore password not being read", 
                   sslServerPolicy.getKeystorePassword().equals("defaultkeypass"));
        assertTrue("Key password not being read", 
                   sslServerPolicy.getKeyPassword().equals("defaultkeypass"));  
        
        assertTrue("Ciphersuites is being being read from somewhere unknown", 
                   sslListener.getCipherSuites() == null); 
        assertTrue("Truststore type not being read", 
                   handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                  + "TrustStoreType"));          

        assertTrue("Secure socket protocol not being read", 
                   handler.checkLogContainsString("The secure socket protocol has been set to TLSv1."));
        assertTrue("Session caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                  + "SessionCaching"));
        assertTrue("SessionCacheKey caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                  + "SessionCacheKey"));
        assertTrue("MaxChainLength caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                  + "MaxChainLength"));
        assertTrue("CertValidator caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLServerPolicy property : "
                                                  + "CertValidator"));
    }

    public void testAllValidDataJKS() {
        
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
        sslServerPolicy.setKeystore(keyStoreStr);
        sslServerPolicy.setKeyPassword("defaultkeypass");
        sslServerPolicy.setKeystorePassword("defaultkeypass");
        
        sslServerPolicy.setKeystoreType("JKS");
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslServerPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslListenerConfigurer jettySslListenerConfigurer = 
                            createJettySslListenerConfigurer(sslServerPolicy, 
                                                           "https://dummyurl",
                                                           handler);

        jettySslListenerConfigurer.configure();

        
    }
    
    public void testAllValidDataPKCS12() {
        
        String keyStoreStr = getPath("resources/celtix.p12");
        SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
        sslServerPolicy.setKeystore(keyStoreStr);
        sslServerPolicy.setKeyPassword("celtixpass");
        sslServerPolicy.setKeystorePassword("celtixpass");
        
        sslServerPolicy.setKeystoreType("PKCS12");
        String trustStoreStr = getPath("resources/abigcompany_ca.pem");
        sslServerPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslListenerConfigurer jettySslListenerConfigurer = 
                            createJettySslListenerConfigurer(sslServerPolicy, 
                                                           "https://dummyurl",
                                                           handler);

        jettySslListenerConfigurer.configure();

        
    }

    
        
    
    public void testAllElementsHaveSetupMethod() {
        SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
        TestHandler handler = new TestHandler(); 
        JettySslListenerConfigurer jettySslListenerConfigurer = 
            createJettySslListenerConfigurer(sslServerPolicy, 
                                           "https://dummyurl",
                                           handler);
        assertTrue("A new element has been "
                   + "added to SSLServerPolicy without a corresponding "
                   + "setup method in the configurer.",
                   jettySslListenerConfigurer.testAllDataHasSetupMethod());
    }
    
    
    private JettySslListenerConfigurer createJettySslListenerConfigurer(
                                             SSLServerPolicy sslServerPolicy,
                                             String urlStr, 
                                             TestHandler handler) {
        try {
            
            
            
            SslListener sslListener = new SslListener();
            JettySslListenerConfigurer jettySslListenerConfigurer = 
                new JettySslListenerConfigurer(configuration,
                                               sslServerPolicy,
                                               sslListener);
            
            jettySslListenerConfigurer.addLogHandler(handler);
            return jettySslListenerConfigurer;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    protected static String getPath(String fileName) {
        URL keystoreURL = JettySslListenerConfigurerTest.class.getResource(".");
        String str = keystoreURL.getFile(); 
        str += DROP_BACK_SRC_DIR  + fileName;
        return str;
    }
}


