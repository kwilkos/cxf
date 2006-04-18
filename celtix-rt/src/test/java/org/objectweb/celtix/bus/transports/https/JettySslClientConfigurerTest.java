package org.objectweb.celtix.bus.transports.https;

import java.net.URL;
import java.util.Properties;

import javax.net.ssl.SSLSocketFactory;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.configuration.security.SSLClientPolicy;
import org.objectweb.celtix.bus.transports.http.JettyHTTPServerEngine;
import org.objectweb.celtix.configuration.Configuration;

public class JettySslClientConfigurerTest extends TestCase {

    
    private static final String DROP_BACK_SRC_DIR = 
        "../../../../../../../../src/test/java/org/objectweb/celtix/bus/transports/https/";

    Bus bus;
    private Configuration configuration;


    public JettySslClientConfigurerTest(String arg0) {
        super(arg0);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(JettySslClientConfigurerTest.class);
        return new TestSetup(suite) {
            protected void tearDown() throws Exception {
                super.tearDown();
                JettyHTTPServerEngine.destroyForPort(9000);
            }
        };
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(JettySslClientConfigurerTest.class);
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
        props.remove("javax.net.ssl.keyPassword");
        props.remove("javax.net.ssl.keyStorePassword");
    }
    
    public void testSecurityConfigurer() {
        try {
            System.setProperty("celtix.security.configurer.celtix.null.http-client",
                               "org.objectweb.celtix.bus.transports.https.SetAllDataSecurityDataProvider");
            SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
            TestHandler handler = new TestHandler();
            JettySslClientConfigurer jettySslClientConfigurer = 
                                createJettySslClientConfigurer(sslClientPolicy, 
                                                               "https://dummyurl",
                                                               handler);
    
            jettySslClientConfigurer.configure();
            assertTrue("Keystore loaded success message not present", 
                       handler.checkLogContainsString("Successfully loaded keystore"));
            assertTrue("Trust store loaded success message not present", 
                       handler.checkLogContainsString("Successfully loaded trust store"));
            assertTrue("Keystore type not being read", 
                       handler.checkLogContainsString("The key store type has been set in configuration "
                                                      + "to JKS"));
            assertTrue("Keystore password not being read", 
                       handler.checkLogContainsString("The key store password was found to be set in "
                                                      + "configuration and will be used."));
            assertTrue("Key password not being read", 
                       handler.checkLogContainsString("The key password was found to be set in "
                                                      + "configuration and will be used."));
            assertTrue("Key manager factory is being being read from somewhere unknown", 
                       handler.checkLogContainsString("The keystore key manager factory "
                                                      + "algorithm has not been set in configuration "
                                                      + "so the default value SunX509 will be used."));
            
            assertTrue("Trust manager factory is being being read from somewhere unknown", 
                       handler.checkLogContainsString("The truststore key manager factory "
                                                      + "algorithm has not been set in configuration "
                                                      + "so the default value PKIX will be used."));  
            
            assertTrue("Ciphersuites is being being read from somewhere unknown", 
                       handler.checkLogContainsString("The cipher suite has not been set, default values "
                                                      + "will be used.")); 
            assertTrue("Truststore type not being read", 
                       handler.checkLogContainsString("The key store type has been set in "
                                                      + "configuration to JKS"));          
    
            assertTrue("Secure socket protocol not being read", 
                       handler.checkLogContainsString("The secure socket protocol has been set to TLSv1."));
            assertTrue("Session caching set but no warning about not supported", 
                       handler.checkLogContainsString("Unsupported SSLClientPolicy property : "
                                                      + "SessionCaching"));
            assertTrue("SessionCacheKey caching set but no warning about not supported", 
                       handler.checkLogContainsString("Unsupported SSLClientPolicy property : "
                                                      + "SessionCacheKey"));
            assertTrue("MaxChainLength caching set but no warning about not supported", 
                       handler.checkLogContainsString("Unsupported SSLClientPolicy property : "
                                                      + "MaxChainLength"));
            assertTrue("CertValidator caching set but no warning about not supported", 
                       handler.checkLogContainsString("Unsupported SSLClientPolicy property : "
                                                      + "CertValidator"));
        } finally {
            System.setProperty("celtix.security.configurer.celtix.null.http-client", "");
        }
    }
    
    public void testSetAllData() {
        
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeystoreType("JKS");
        
        sslClientPolicy.setKeyPassword("defaultkeypass");
        sslClientPolicy.setKeystorePassword("defaultkeypass");
        sslClientPolicy.setTrustStoreType("JKS");
        sslClientPolicy.setTrustStoreAlgorithm("JKS");
        sslClientPolicy.setSecureSocketProtocol("TLSv1");
        sslClientPolicy.setSessionCacheKey("Anything");
        sslClientPolicy.setSessionCaching(true);
        sslClientPolicy.setMaxChainLength(new Long(2));
        sslClientPolicy.setCertValidator("Anything");
        sslClientPolicy.setProxyHost("Anything");
        sslClientPolicy.setProxyPort(new Long(1234));
        
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslClientConfigurer jettySslClientConfigurer = 
                            createJettySslClientConfigurer(sslClientPolicy, 
                                                           "https://dummyurl",
                                                           handler);

        jettySslClientConfigurer.configure();
        SSLSocketFactory sSLSocketFactory = 
                jettySslClientConfigurer.getHttpsConnection().getSSLSocketFactory();
        
        assertTrue(sSLSocketFactory instanceof SSLSocketFactoryWrapper);
        assertTrue("Keystore loaded success message not present", 
                   handler.checkLogContainsString("Successfully loaded keystore"));
        assertTrue("Trust store loaded success message not present", 
                   handler.checkLogContainsString("Successfully loaded trust store"));
        assertTrue("Keystore type not being read", 
                   handler.checkLogContainsString("The key store type has been set in configuration to JKS"));
        assertTrue("Keystore password not being read", 
                   handler.checkLogContainsString("The key store password was found to be set in "
                                                  + "configuration and will be used."));
        assertTrue("Key password not being read", 
                   handler.checkLogContainsString("The key password was found to be set in "
                                                  + "configuration and will be used."));
        assertTrue("Key manager factory is being being read from somewhere unknown", 
                   handler.checkLogContainsString("The keystore key manager factory "
                                                  + "algorithm has not been set in configuration "
                                                  + "so the default value SunX509 will be used."));
        
        assertTrue("Trust manager factory is being being read from somewhere unknown", 
                   handler.checkLogContainsString("The truststore key manager factory "
                                                  + "algorithm has not been set in configuration "
                                                  + "so the default value PKIX will be used."));  
        
        assertTrue("Ciphersuites is being being read from somewhere unknown", 
                   handler.checkLogContainsString("The cipher suite has not been set, default values "
                                                  + "will be used.")); 
        assertTrue("Truststore type not being read", 
                   handler.checkLogContainsString("The key store type has been set in "
                                                  + "configuration to JKS"));          

        assertTrue("Secure socket protocol not being read", 
                   handler.checkLogContainsString("The secure socket protocol has been set to TLSv1."));
        assertTrue("Session caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLClientPolicy property : SessionCaching"));
        assertTrue("SessionCacheKey caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLClientPolicy property : SessionCacheKey"));
        assertTrue("MaxChainLength caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLClientPolicy property : MaxChainLength"));
        assertTrue("CertValidator caching set but no warning about not supported", 
                   handler.checkLogContainsString("Unsupported SSLClientPolicy property : CertValidator"));
    }

    public void testAllValidDataJKS() {
        
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("defaultkeypass");
        sslClientPolicy.setKeystorePassword("defaultkeypass");
        
        sslClientPolicy.setKeystoreType("JKS");
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslClientConfigurer jettySslClientConfigurer = 
                            createJettySslClientConfigurer(sslClientPolicy, 
                                                           "https://dummyurl",
                                                           handler);

        jettySslClientConfigurer.configure();
        SSLSocketFactory sSLSocketFactory = 
                jettySslClientConfigurer.getHttpsConnection().getSSLSocketFactory();
        
        assertTrue(sSLSocketFactory instanceof SSLSocketFactoryWrapper);
        assertTrue("Keystore loaded success message not present", 
                   handler.checkLogContainsString("Successfully loaded keystore"));
        assertTrue("Trust store loaded success message not present", 
                   handler.checkLogContainsString("Successfully loaded trust store"));
        
    }
    
    public void testAllValidDataPKCS12() {
        
        String keyStoreStr = getPath("resources/celtix.p12");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("celtixpass");
        sslClientPolicy.setKeystorePassword("celtixpass");
        
        sslClientPolicy.setKeystoreType("PKCS12");
        String trustStoreStr = getPath("resources/abigcompany_ca.pem");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslClientConfigurer jettySslClientConfigurer = 
                            createJettySslClientConfigurer(sslClientPolicy, 
                                                           "https://dummyurl",
                                                           handler);

        jettySslClientConfigurer.configure();
        SSLSocketFactory sSLSocketFactory = 
                jettySslClientConfigurer.getHttpsConnection().getSSLSocketFactory();
        
        assertTrue(sSLSocketFactory instanceof SSLSocketFactoryWrapper);
        assertTrue("Keystore loaded success message not present", 
                   handler.checkLogContainsString("Successfully loaded keystore"));
        assertTrue("Trust store loaded success message not present", 
                   handler.checkLogContainsString("Successfully loaded trust store"));
        
    }

    
    public void testNonExistentKeystoreJKS() {
        
        String keyStoreStr = getPath("resources/defaultkeystoredontexist");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("defaultkeypass");
        sslClientPolicy.setKeystorePassword("defaultkeypass");
        
        sslClientPolicy.setKeystoreType("JKS");
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslClientConfigurer jettySslClientConfigurer = 
                            createJettySslClientConfigurer(sslClientPolicy, 
                                                           "https://dummyurl",
                                                           handler);
        jettySslClientConfigurer.configure();
        SSLSocketFactory sSLSocketFactory = 
                jettySslClientConfigurer.getHttpsConnection().getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be null", !isNewSocketFactory);
        assertTrue("SSLContext should have failed, invalid keystore location", 
                   handler.checkLogContainsString("Problem initializing ssl for the outbound request"));

        
    }
    
    public void testNonExistentKeystorePKCS12() {
        
        String keyStoreStr = getPath("resources/defaultkeystoredontexist");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("celtixpass");
        sslClientPolicy.setKeystorePassword("celtixpass");
        
        String trustStoreStr = getPath("resources/abigcompany_ca.pem");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslClientConfigurer jettySslClientConfigurer = 
                            createJettySslClientConfigurer(sslClientPolicy, 
                                                           "https://dummyurl",
                                                           handler);
        jettySslClientConfigurer.configure();
        SSLSocketFactory sSLSocketFactory = 
                jettySslClientConfigurer.getHttpsConnection().getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be null", !isNewSocketFactory);
        assertTrue("SSLContext should have failed, invalid keystore location", 
                   handler.checkLogContainsString("Problem initializing ssl for the outbound request"));
        
    }    
    
    public void testWrongKeystorePasswordJKS() {
        
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("defaultkeypass");
        sslClientPolicy.setKeystorePassword("defaultkeypasswrong");
        
        sslClientPolicy.setKeystoreType("JKS");
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslClientConfigurer jettySslClientConfigurer = 
                            createJettySslClientConfigurer(sslClientPolicy, 
                                                           "https://dummyurl",
                                                           handler);
        jettySslClientConfigurer.configure();
        SSLSocketFactory sSLSocketFactory = 
                jettySslClientConfigurer.getHttpsConnection().getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be non and a new one", isNewSocketFactory);
        assertTrue("SSLContext init should have passed, but keystore initialization failed, invalid "
                   + "keystore password", 
                   handler.checkLogContainsString("Loading the keystore failed "));
        assertTrue("SSLContext init should have passed, but keystore initialization failed, invalid "
                   + "keystore password",
                   handler.checkLogContainsString("Keystore was tampered with, or password was incorrect"));
        assertTrue("SSLContext init should have passed, but looks like trustore not loaded", 
                   handler.checkLogContainsString("Successfully loaded trust store"));
        assertTrue("Check to ensure keystore password and keypassword same failed",
                   handler.checkLogContainsString("The value specified for the keystore password"
                                                  + " is different to the key password. Currently "
                                                  + "limitations in JSSE requires that they should be the "
                                                  + "same. The keystore password value will be used only."));
        
    }
    
    public void testWrongKeystorePasswordPKCS12() {
        
        String keyStoreStr = getPath("resources/celtix.p12");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("celtixpass");
        sslClientPolicy.setKeystorePassword("celtixpasswrong");
        
        String trustStoreStr = getPath("resources/abigcompany_ca.pem");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslClientConfigurer jettySslClientConfigurer = 
                            createJettySslClientConfigurer(sslClientPolicy, 
                                                           "https://dummyurl",
                                                           handler);
        jettySslClientConfigurer.configure();
        SSLSocketFactory sSLSocketFactory = 
                jettySslClientConfigurer.getHttpsConnection().getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be non and a new one", isNewSocketFactory);
        assertTrue("SSLContext init should have passed, but keystore initialization failed, invalid "
                   + "keystore password", 
                   handler.checkLogContainsString("Loading the keystore failed "));
        assertTrue("SSLContext init should have passed, but keystore initialization failed, invalid "
                   + "keystore password",
                   handler.checkLogContainsString("Loading the keystore failed"));
        assertTrue("SSLContext init should have passed, but looks like trustore not loaded", 
                   handler.checkLogContainsString("Successfully loaded trust store"));
        assertTrue("Check to ensure keystore password and keypassword same failed",
                   handler.checkLogContainsString("The value specified for the keystore password"
                                                  + " is different to the key password. Currently "
                                                  + "limitations in JSSE requires that they should be the "
                                                  + "same. The keystore password value will be used only."));
        
    }
    
    public void testWrongKeyPasswordJKS() {
        
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("defaultkeypasswrong");
        sslClientPolicy.setKeystorePassword("defaultkeypass");
        
        sslClientPolicy.setKeystoreType("JKS");
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslClientConfigurer jettySslClientConfigurer = 
                            createJettySslClientConfigurer(sslClientPolicy, 
                                                           "https://dummyurl",
                                                           handler);
        jettySslClientConfigurer.configure();
        SSLSocketFactory sSLSocketFactory = 
                jettySslClientConfigurer.getHttpsConnection().getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be non and a new one", isNewSocketFactory);
        assertTrue("SSLContext init should have passed, and keystore initialization succeedeed, "
                   + "EVEN THOUGH invalid key password", 
                   handler.checkLogContainsString("Successfully loaded keystore"));
        assertTrue("SSLContext init should have passed, but looks like trustore not loaded", 
                   handler.checkLogContainsString("Successfully loaded trust store"));
        assertTrue("Check to ensure keystore password and keypassword same failed",
                   handler.checkLogContainsString("The value specified for the keystore password"
                                                  + " is different to the key password. Currently "
                                                  + "limitations in JSSE requires that they should be the "
                                                  + "same. The keystore password value will be used only."));
        
    }
    
    public void testWrongKeyPasswordPKCS12() {
        
        String keyStoreStr = getPath("resources/celtix.p12");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("celtixpasswrong");
        sslClientPolicy.setKeystorePassword("celtixpass");
        
        String trustStoreStr = getPath("resources/abigcompany_ca.pem");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslClientConfigurer jettySslClientConfigurer = 
                            createJettySslClientConfigurer(sslClientPolicy, 
                                                           "https://dummyurl",
                                                           handler);
        jettySslClientConfigurer.configure();
        SSLSocketFactory sSLSocketFactory = 
                jettySslClientConfigurer.getHttpsConnection().getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be non and a new one", isNewSocketFactory);
        assertTrue("SSLContext init should have passed, and keystore initialization succeedeed, "
                   + "EVEN THOUGH invalid key password", 
                   handler.checkLogContainsString("Successfully loaded keystore"));
        assertTrue("SSLContext init should have passed, but looks like trustore not loaded", 
                   handler.checkLogContainsString("Successfully loaded trust store"));
        assertTrue("Check to ensure keystore password and keypassword same failed",
                   handler.checkLogContainsString("The value specified for the keystore password"
                                                  + " is different to the key password. Currently "
                                                  + "limitations in JSSE requires that they should be the "
                                                  + "same. The keystore password value will be used only."));
        
    }
    
    
    public void testAllElementsHaveSetupMethod() {
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        TestHandler handler = new TestHandler(); 
        JettySslClientConfigurer jettySslClientConfigurer = 
            createJettySslClientConfigurer(sslClientPolicy, 
                                           "https://dummyurl",
                                           handler);
        assertTrue("A new element has been "
                   + "added to SSLClientPolicy without a corresponding "
                   + "setup method in the configurer.",
                   jettySslClientConfigurer.testAllDataHasSetupMethod());
    }
    
    
    private JettySslClientConfigurer createJettySslClientConfigurer(
                                             SSLClientPolicy sslClientPolicy,
                                             String urlStr, 
                                             TestHandler handler) {
        try {
            DummyHttpsConnection connection = new DummyHttpsConnection(null);
            JettySslClientConfigurer jettySslClientConfigurer = 
                new JettySslClientConfigurer(sslClientPolicy,
                                             connection,
                                             configuration);
            
            jettySslClientConfigurer.addLogHandler(handler);
            return jettySslClientConfigurer;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    protected static String getPath(String fileName) {
        URL keystoreURL = JettySslClientConfigurerTest.class.getResource(".");
        String str = keystoreURL.getFile(); 
        str += DROP_BACK_SRC_DIR  + fileName;
        return str;
    }
}


