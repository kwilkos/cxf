package org.objectweb.celtix.bus.transports.https;

import java.net.URL;
import java.net.URLConnection;
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
import org.objectweb.celtix.configuration.Configuration;

public class JettySslClientSystemPropertiesConfigurerTest extends TestCase {

    
    private static final String DROP_BACK_SRC_DIR = 
        "../../../../../../../../src/test/java/org/objectweb/celtix/bus/transports/https/";

    Bus bus;

    


    public JettySslClientSystemPropertiesConfigurerTest(String arg0) {
        super(arg0);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(JettySslClientSystemPropertiesConfigurerTest.class);
        return new TestSetup(suite) {
            protected void tearDown() throws Exception {
                super.tearDown();
            }
        }; 
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(JettySslClientSystemPropertiesConfigurerTest.class);
    }

    public void setUp() throws BusException {
        bus = EasyMock.createMock(Bus.class);
    }

    public void tearDown() throws Exception {
        EasyMock.reset(bus);
        Properties props = System.getProperties();
        props.remove("javax.net.ssl.trustStore");
        props.remove("javax.net.ssl.keyStore");
        props.remove("javax.net.ssl.keyPassword");
        props.remove("javax.net.ssl.keyStorePassword");
    }
    
    public void testSetAllDataSomeSystemProperties() {
        
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        System.setProperty("javax.net.ssl.keyStore", keyStoreStr);
        sslClientPolicy.setKeystoreType("JKS");
        
        System.setProperty("javax.net.ssl.keyStorePassword", "defaultkeypass");
        System.setProperty("javax.net.ssl.keyPassword", "defaultkeypass");
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
        System.setProperty("javax.net.ssl.trustStore", trustStoreStr);
        TestHandler handler = new TestHandler();
        JettySslClientConfigurer jettySslClientConfigurer = 
                            createJettySslClientConfigurer(sslClientPolicy, 
                                                           "https://dummyurl",
                                                           handler);

        jettySslClientConfigurer.configure();
        SSLSocketFactory sSLSocketFactory = 
                jettySslClientConfigurer.getHttpsConnection().getSSLSocketFactory();
        
        assertTrue("sSLSocketFactory not correct, sSLSocketFactory = " + sSLSocketFactory,
                   sSLSocketFactory instanceof SSLSocketFactoryWrapper);
        assertTrue("Keystore loaded success message not present", 
                   handler.checkLogContainsString("Successfully loaded keystore"));
        assertTrue("Trust store loaded success message not present", 
                   handler.checkLogContainsString("Successfully loaded trust store"));
        assertTrue("Keystore type not being read", 
                   handler.checkLogContainsString("The key store type has been set in configuration to JKS"));
        assertTrue("Keystore password not being read", 
                   handler.checkLogContainsString("The key store password was found to be set "
                                                  + "as a system property and will be used."));
        assertTrue("Key password not being read", 
                   handler.checkLogContainsString("The key  password was found to be set as a "
                                                  + "system property and will be used."));
        assertTrue("Key manager factory is being being read from somewhere unknown", 
                   handler.checkLogContainsString("The keystore key manager factory "
                                                  + "algorithm has not been set in configuration "
                                                  + "so the default value SunX509 will be used."));
        
        assertTrue("Trust manager factory is being being read from somewhere unknown", 
                   handler.checkLogContainsString("The truststore key manager factory "
                                                  + "algorithm has not been set in configuration "
                                                  + "so the default value PKIX will be used."));  
        assertTrue("Trust store location not read successfully", 
                   handler.checkLogContainsString("The trust store location has been "
                                                  + "via a system property to"));
        
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
    
    private JettySslClientConfigurer createJettySslClientConfigurer(
                                             SSLClientPolicy sslClientPolicy,
                                             String urlStr, 
                                             TestHandler handler) {
        try {
            URL url  = new URL(urlStr);
            URLConnection connection = new DummyHttpsConnection(url);
            Configuration configuration = EasyMock.createMock(Configuration.class);
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
    
    private String getPath(String fileName) {
        URL keystoreURL = JettySslClientSystemPropertiesConfigurerTest.class.getResource(".");
        String str = keystoreURL.getFile(); 
        str += DROP_BACK_SRC_DIR  + fileName;
        return str;
    }
}

