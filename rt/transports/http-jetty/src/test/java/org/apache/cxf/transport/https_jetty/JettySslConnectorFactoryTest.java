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

package org.apache.cxf.transport.https_jetty;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.configuration.security.ObjectFactory;
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.transport.https.SSLUtils;

//import org.mortbay.jetty.security.SslSelectChannelConnector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.security.SslSelectChannelConnector;
//import org.mortbay.jetty.security.SslSocketConnector;


public class JettySslConnectorFactoryTest extends Assert {
    protected static final String DROP_BACK_SRC_DIR = 
        "../../../../../../../../"
        + "http/src/test/java/org/apache/cxf/transport/https/";

    private static final String[] EXPORT_CIPHERS =
    {"SSL_RSA_WITH_NULL_MD5", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_WITH_DES_CBC_SHA"};
    private static final String[] NON_EXPORT_CIPHERS =
    {"SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_3DES_EDE_CBC_SHA"};

    private SslSelectChannelConnector sslConnector;
    
    @Before
    public void setUp() throws Exception {
        sslConnector = new SslSelectChannelConnector();
    }

    @After
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
    
    @Test
    public void testSetAllData() throws Exception {       
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
        JettySslConnectorFactory factory = null;
        String oldHome = overrideHome();
        try {            
            factory = createFactory(sslServerPolicy, 
                                    "https://dummyurl",
                                    handler);

            factory.decorate(sslConnector);
        } finally {
            restoreHome(oldHome);
        }
        
        assertTrue("Keystore not set properly", 
                   sslConnector.getKeystore().contains("resources/defaultkeystore"));
        String trustStr = System.getProperty("javax.net.ssl.trustStore");
        assertTrue("Trust store loaded success message not present", 
                   trustStr.contains("resources/defaulttruststore"));
        assertTrue("Keystore type not being read", 
                   sslConnector.getKeystoreType().equals("JKS"));
        assertTrue("Keystore password not being read", 
                   sslServerPolicy.getKeystorePassword().equals("defaultkeypass"));
        assertTrue("Key password not being read", 
                   sslServerPolicy.getKeyPassword().equals("defaultkeypass"));  
        
        //assertNotNull("Default filtered ciphersuites not set on listener", 
        //              sslConnector.getExcludeCipherSuites()); 

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
    
    @Test
    public void testSetAllDataExceptKeystoreAndTrustStore() throws Exception {        
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
        JettySslConnectorFactory factory = null;
        String oldHome = overrideHome();
        try {            
            factory = createFactory(sslServerPolicy, 
                                    "https://dummyurl",
                                    handler);

            factory.decorate(sslConnector);
        } finally {
            restoreHome(oldHome);
        }
        
        assertTrue("Keystore not set properly, sslListener.getKeystore() = " + sslConnector.getKeystore(), 
                   sslConnector.getKeystore().contains(".keystore"));
        String trustStr = System.getProperty("javax.net.ssl.trustStore");
        assertTrue("Trust store loaded success message not present", 
                   trustStr.contains("cacerts"));
        assertTrue("Keystore type not being read", 
                   sslConnector.getKeystoreType().equals("JKS"));
        assertTrue("Keystore password not being read", 
                   sslServerPolicy.getKeystorePassword().equals("defaultkeypass"));
        assertTrue("Key password not being read", 
                   sslServerPolicy.getKeyPassword().equals("defaultkeypass"));  
        
        assertNull("Ciphersuites is being being read from somewhere unknown", 
                   sslConnector.getCipherSuites());        
 
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
    @Test
    public void testConfiguredCipherSuites() throws Exception {       
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
        sslServerPolicy.setKeystore(keyStoreStr);
        sslServerPolicy.setKeystoreType("JKS");
        
        sslServerPolicy.setKeyPassword("defaultkeypass");
        sslServerPolicy.setKeystorePassword("defaultkeypass");
        sslServerPolicy.setTrustStoreType("JKS");
        sslServerPolicy.setTrustStoreAlgorithm("JKS");
        sslServerPolicy.setSecureSocketProtocol("TLSv1");        
        for (int i = 0; i < EXPORT_CIPHERS.length; i++) {
            sslServerPolicy.getCiphersuites().add(EXPORT_CIPHERS[i]);
        }
        for (int i = 0; i < NON_EXPORT_CIPHERS.length; i++) {
            sslServerPolicy.getCiphersuites().add(NON_EXPORT_CIPHERS[i]);
        }

        String trustStoreStr = getPath("resources/defaulttruststore");
        sslServerPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        JettySslConnectorFactory factory = createFactory(sslServerPolicy, 
                                                        "https://dummyurl",
                                                        handler);

        factory.decorate(sslConnector);
        
        assertNotNull("Configured ciphersuites not set on listener", 
                      sslConnector.getCipherSuites()); 
        assertFalse("Ciphersuites config not picked up", handler
                    .checkLogContainsString("The cipher suites have not been configured, " 
                                            + "default values will be used."));        
        assertFalse("Unexpected included ciphersuite filter",
                    handler.checkLogContainsString("suite is included by the filter."));
        assertFalse("Unexpected excluded ciphersuite fuilter",
                    handler.checkLogContainsString("suite is excluded by the filter."));
        assertFalse("Unexpected ciphersuite filtering",
                    handler.checkLogContainsString("The enabled cipher suites have been filtered down to"));
    }


    @Test
    public void testDefaultedCipherSuiteFilters() throws Exception {       
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
        sslServerPolicy.setKeystore(keyStoreStr);
        sslServerPolicy.setKeystoreType("JKS");
        
        sslServerPolicy.setKeyPassword("defaultkeypass");
        sslServerPolicy.setKeystorePassword("defaultkeypass");
        sslServerPolicy.setTrustStoreType("JKS");
        sslServerPolicy.setTrustStoreAlgorithm("JKS");
        sslServerPolicy.setSecureSocketProtocol("TLSv1");

        String trustStoreStr = getPath("resources/defaulttruststore");
        sslServerPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        JettySslConnectorFactory factory = createFactory(sslServerPolicy, 
                                                        "https://dummyurl",
                                                        handler);

        factory.decorate(sslConnector);
                
        //assertNotNull("Ciphersuites is being being read from somewhere unknown", 
        //              sslConnector.getExcludeCipherSuites()); 
        assertTrue("Ciphersuites config not picked up", 
                   handler.checkLogContainsString("The cipher suites have not been configured," 
                                                  + " falling back to cipher suite filters."));
        assertTrue("Expected defaulted ciphersuite filters", 
                   handler.checkLogContainsString("The cipher suite filters have not been configured,"
                                                  + " falling back to default filters."));
        for (int i = 0; i < EXPORT_CIPHERS.length; i++) {
            assertTrue("Expected included ciphersuite not included: " + EXPORT_CIPHERS[i],
                       handler.checkLogContainsString(EXPORT_CIPHERS[i]
                                                      + " cipher suite is included by the filter."));
        }
        for (int i = 0; i < NON_EXPORT_CIPHERS.length; i++) {
            assertTrue("Expected excluded ciphersuite not included: " + NON_EXPORT_CIPHERS[i],
                       handler.checkLogContainsString(NON_EXPORT_CIPHERS[i]
                                                      + " cipher suite is excluded by the filter."));
        }
        assertTrue("Expected excluded ciphersuite not included",
                   handler.checkLogContainsString("The enabled cipher suites have been filtered down to")); 
    }

    @Test
    public void testNonDefaultedCipherSuiteFilters() throws Exception {       
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
        sslServerPolicy.setKeystore(keyStoreStr);
        sslServerPolicy.setKeystoreType("JKS");
        
        sslServerPolicy.setKeyPassword("defaultkeypass");
        sslServerPolicy.setKeystorePassword("defaultkeypass");
        sslServerPolicy.setTrustStoreType("JKS");
        sslServerPolicy.setTrustStoreAlgorithm("JKS");
        sslServerPolicy.setSecureSocketProtocol("TLSv1");

        // reverse default sense of include/exlcude
        FiltersType filters = new ObjectFactory().createFiltersType();
        for (int i = 0; i < NON_EXPORT_CIPHERS.length; i++) {
            filters.getInclude().add(NON_EXPORT_CIPHERS[i]);
        }
        for (int i = 0; i < EXPORT_CIPHERS.length; i++) {
            filters.getExclude().add(EXPORT_CIPHERS[i]);
        }
        sslServerPolicy.setCiphersuiteFilters(filters);
        
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslServerPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        JettySslConnectorFactory factory = createFactory(sslServerPolicy, 
                                                        "https://dummyurl",
                                                        handler);

        factory.decorate(sslConnector);
                
        //assertNotNull("Ciphersuites is being being read from somewhere unknown", 
        //              sslConnector.getExcludeCipherSuites()); 
        assertTrue("Ciphersuites config not picked up",
                   handler.checkLogContainsString("The cipher suites have not been configured," 
                                                  + " falling back to cipher suite filters."));
        assertFalse("Expected defaulted ciphersuite filters", 
                    handler.checkLogContainsString("The cipher suite filters have not been configured,"
                                                   + " falling back to default filters."));
        for (int i = 0; i < NON_EXPORT_CIPHERS.length; i++) {
            assertTrue("Expected included ciphersuite not included: " + NON_EXPORT_CIPHERS[i],
                       handler.checkLogContainsString(NON_EXPORT_CIPHERS[i]
                                                      + " cipher suite is included by the filter."));
        }
        for (int i = 0; i < EXPORT_CIPHERS.length; i++) {
            assertTrue("Expected excluded ciphersuite not included: " + EXPORT_CIPHERS[i],
                       handler.checkLogContainsString(EXPORT_CIPHERS[i]
                                                      + " cipher suite is excluded by the filter."));
        }
        assertTrue("Expected excluded ciphersuite not included",
                   handler.checkLogContainsString("The enabled cipher suites have been filtered down to")); 
    }

    @Test
    public void testAllValidDataJKS() throws Exception {        
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
        sslServerPolicy.setKeystore(keyStoreStr);
        sslServerPolicy.setKeyPassword("defaultkeypass");
        sslServerPolicy.setKeystorePassword("defaultkeypass");
        
        sslServerPolicy.setKeystoreType("JKS");
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslServerPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        JettySslConnectorFactory factory = createFactory(sslServerPolicy, 
                                                        "https://dummyurl",
                                                        handler);

        factory.decorate(sslConnector);
    }
    
    @Test
    public void testAllValidDataPKCS12() throws Exception {
        String keyStoreStr = getPath("resources/celtix.p12");
        SSLServerPolicy sslServerPolicy = new SSLServerPolicy();
        sslServerPolicy.setKeystore(keyStoreStr);
        sslServerPolicy.setKeyPassword("celtixpass");
        sslServerPolicy.setKeystorePassword("celtixpass");
        
        sslServerPolicy.setKeystoreType("PKCS12");
        String trustStoreStr = getPath("resources/abigcompany_ca.pem");
        sslServerPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        JettySslConnectorFactory factory = createFactory(sslServerPolicy, 
                                                        "https://dummyurl",
                                                        handler);

        factory.decorate(sslConnector);
    }

    @Test
    public void testAllElementsHaveSetupMethod() throws Exception {
        SSLServerPolicy policy = new SSLServerPolicy();
        TestLogHandler handler = new TestLogHandler(); 
        JettySslConnectorFactory factory = createFactory(policy, 
                                                        "https://dummyurl",
                                                         handler);
        assertTrue("A new element has been "
                   + "added to SSLServerPolicy without a corresponding "
                   + "setup method in the configurer.",
                   SSLUtils.testAllDataHasSetupMethod(policy,
                                                      factory.getUnSupported(),
                                                      factory.getDerivative()));
    }
    
    private JettySslConnectorFactory createFactory(SSLServerPolicy policy,
                                                  String urlStr, 
                                                  TestLogHandler handler) {
        JettySslConnectorFactory factory =
            new JettySslConnectorFactory(policy);
        factory.addLogHandler(handler);
        return factory;
    }
    
    private static String overrideHome() {
        String oldHome = System.getProperty("user.home");
        String tmpHome = "" + System.getProperty("java.io.tmpdir")
                         + File.separator
                         + System.getProperty("user.name")
                         + File.separator
                         + System.currentTimeMillis();
        System.setProperty("user.home", tmpHome);
        return oldHome;
    }
   
    private static void restoreHome(String oldHome) {
        System.setProperty("user.home", oldHome);
    }

    
    protected static String getPath(String fileName) throws URISyntaxException {
        URL keystoreURL = JettySslConnectorFactoryTest.class.getResource(".");
        String str = keystoreURL.toURI().getPath(); 
        str += DROP_BACK_SRC_DIR  + fileName;
        return str;
    }
    
    
    static class TestLogHandler extends Handler {
        String log;
        
        public TestLogHandler() {
            log = "";
        }
        
        public void publish(LogRecord record) {
            log += record.getMessage();
            
        }

        public void flush() {
            
        }

        public void close() throws SecurityException {
            
        }
        
        boolean checkLogContainsString(String str) {
            if (log.indexOf(str) == -1) {
                return false;
            }
            return true;
        }    
    }    
}


