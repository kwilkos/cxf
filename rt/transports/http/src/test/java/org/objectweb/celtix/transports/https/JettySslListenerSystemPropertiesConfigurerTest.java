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

public class JettySslListenerSystemPropertiesConfigurerTest extends TestCase {

    
    private static final String DROP_BACK_SRC_DIR = 
        "../../../../../../../src/test/java/org/objectweb/celtix/transports/https/";

    Bus bus;

    


    public JettySslListenerSystemPropertiesConfigurerTest(String arg0) {
        super(arg0);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(JettySslListenerSystemPropertiesConfigurerTest.class);
        return new TestSetup(suite) {
            protected void tearDown() throws Exception {
                super.tearDown();
            }
        };
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(JettySslListenerSystemPropertiesConfigurerTest.class);
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
 
    public void testSetAllData() {
        
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
        System.setProperty("javax.net.ssl.keyStore", keyStoreStr);
        System.setProperty("javax.net.ssl.keyStorePassword", "defaultkeypass"); 
        sslServerPolicy.setTrustStoreType("JKS");
        sslServerPolicy.setTrustStoreAlgorithm("JKS");
        sslServerPolicy.setSecureSocketProtocol("TLSv1");
        sslServerPolicy.setKeystoreAlgorithm("Anything");
        sslServerPolicy.setSessionCacheKey("Anything");
        sslServerPolicy.setSessionCaching(true);
        sslServerPolicy.setMaxChainLength(new Long(2));
        sslServerPolicy.setCertValidator("Anything");

        
        String trustStoreStr = getPath("resources/defaulttruststore");
        System.setProperty("javax.net.ssl.trustStore", trustStoreStr);
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
                   sslListener.getKeystoreType().equals("PKCS12"));
        assertTrue("Couldn't deal with case when SSLServerPolicy keystore password  is null", 
                   sslServerPolicy.getKeystorePassword() == null);
        assertTrue("Couldn't deal with case when SSLServerPolicy key password  is null", 
                   sslServerPolicy.getKeyPassword() == null);  
        
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

    
    private JettySslListenerConfigurer createJettySslListenerConfigurer(
                                             SSLServerPolicy sslServerPolicy,
                                             String urlStr, 
                                             TestHandler handler) {
        try {
            Configuration configuration = EasyMock.createMock(Configuration.class);
            
            
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
        URL keystoreURL = JettySslListenerSystemPropertiesConfigurerTest.class.getResource(".");
        String str = keystoreURL.getFile(); 
        str += DROP_BACK_SRC_DIR  + fileName;
        return str;
    }
}


