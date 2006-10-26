/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.transport.https;

import java.net.URL;
import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.cxf.configuration.security.SSLServerPolicy;

import org.mortbay.http.SslListener;


public class JettySslListenerFactoryTest extends TestCase {
    
    private SslListener sslListener;
    
    public JettySslListenerFactoryTest(String arg0) {
        super(arg0);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(JettySslListenerFactoryTest.class);
        return new TestSetup(suite) {
            protected void tearDown() throws Exception {
                super.tearDown();
            }
        };
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JettySslListenerFactoryTest.class);
    }
    
    public void setUp() throws Exception {
        sslListener = new SslListener();
    }

    public void tearDown() throws Exception {
        Properties props = System.getProperties();
        props.remove("javax.net.ssl.trustStore");
        props.remove("javax.net.ssl.keyStore");
    }
    
    /*
    public void testSecurityConfigurer() {
        try {
            SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
            TestLogHandler handler = new TestLogHandler();
            JettySslListenerFactory factory = createFactory(sslServerPolicy, 
                                                            "https://dummyurl",
                                                            handler);    
            factory.decorate(sslListener);
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
            
            System.setProperty("celtix.security.configurer." + HTTP_LISTENER_CONFIG_ID.toString(),
                "org.apache.cxf.bus.transports.https.DoesNotExistSetAllDataSecurityDataProvider");
            SSLServerPolicy sslServerPolicy2 = new SSLServerPolicy();
            TestLogHandler handler2 = new TestLogHandler();
            JettySslListenerFactory factory2 = createFactory(sslServerPolicy2, 
                                                             "https://dummyurl",
                                                             handler2);
            sslServerPolicy2.setKeyPassword("test");
            sslServerPolicy2.setKeystorePassword("test1");
            factory2.decorate(sslListener);
            
            assertTrue("Keystore not set properly", 
                       handler2.checkLogContainsString("Failure invoking on custom security configurer "
                                 + "org.apache.cxf.bus.transports.https."
                                 + "DoesNotExistSetAllDataSecurityDataProvider, "));
        } finally {
            System.getProperties().remove("celtix.security.configurer." + HTTP_LISTENER_CONFIG_ID.toString());
        }
    }
    */
    
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
        TestLogHandler handler = new TestLogHandler();
        JettySslListenerFactory factory = createFactory(sslServerPolicy, 
                                                        "https://dummyurl",
                                                        handler);

        factory.decorate(sslListener);
        
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
        TestLogHandler handler = new TestLogHandler();
        JettySslListenerFactory factory = createFactory(sslServerPolicy, 
                                                        "https://dummyurl",
                                                        handler);

        factory.decorate(sslListener);
        
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
        TestLogHandler handler = new TestLogHandler();
        JettySslListenerFactory factory = createFactory(sslServerPolicy, 
                                                        "https://dummyurl",
                                                        handler);

        factory.decorate(sslListener);
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
        TestLogHandler handler = new TestLogHandler();
        JettySslListenerFactory factory = createFactory(sslServerPolicy, 
                                                        "https://dummyurl",
                                                        handler);

        factory.decorate(sslListener);
    }

    public void testAllElementsHaveSetupMethod() {
        SSLServerPolicy policy = new SSLServerPolicy();
        TestLogHandler handler = new TestLogHandler(); 
        JettySslListenerFactory factory = createFactory(policy, 
                                                        "https://dummyurl",
                                                         handler);
        assertTrue("A new element has been "
                   + "added to SSLServerPolicy without a corresponding "
                   + "setup method in the configurer.",
                   SSLUtils.testAllDataHasSetupMethod(policy, factory.getUnSupported()));
    }
    
    private JettySslListenerFactory createFactory(SSLServerPolicy policy,
                                                  String urlStr, 
                                                  TestLogHandler handler) {
        JettySslListenerFactory factory =
            new JettySslListenerFactory(policy);
        factory.addLogHandler(handler);
        return factory;
    }
    
    protected static String getPath(String fileName) {
        URL keystoreURL = JettySslListenerFactoryTest.class.getResource(".");
        String str = keystoreURL.getFile(); 
        str += HttpsURLConnectionFactoryTest.DROP_BACK_SRC_DIR  + fileName;
        return str;
    }
}


