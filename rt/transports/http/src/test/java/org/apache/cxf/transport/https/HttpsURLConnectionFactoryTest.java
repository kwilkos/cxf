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

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import javax.net.ssl.SSLSocketFactory;

import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.configuration.security.ObjectFactory;
import org.apache.cxf.configuration.security.SSLClientPolicy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class HttpsURLConnectionFactoryTest extends Assert {

    protected static final String DROP_BACK_SRC_DIR = 
        "../../../../../../../"
        + "src/test/java/org/apache/cxf/transport/https/";
    
    private static final String[] EXPORT_CIPHERS =
    {"SSL_RSA_WITH_NULL_MD5", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_WITH_DES_CBC_SHA"};
    private static final String[] NON_EXPORT_CIPHERS =
    {"SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_3DES_EDE_CBC_SHA"};

    private TestHttpsURLConnection connection;
    

    @Before
    public void setUp() throws Exception {
        connection = new TestHttpsURLConnection(null);
    }

    @After
    public void tearDown() throws Exception {
        Properties props = System.getProperties();
        props.remove("javax.net.ssl.trustStore");
        props.remove("javax.net.ssl.keyStore");
        props.remove("javax.net.ssl.keyPassword");
        props.remove("javax.net.ssl.keyStorePassword");
    }

    /*
    public void testSecurityConfigurer() {

        try {
            SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
            TestLogHandler handler = new TestLogHandler();

            HttpsURLConnectionFactory factory = 
                createFactory(sslClientPolicy, "https://dummyurl", handler);

            factory.decorate(connection);

            assertTrue("Keystore loaded success message not present", handler
                .checkLogContainsString("Successfully loaded keystore"));
            assertTrue("Trust store loaded success message not present", handler
                .checkLogContainsString("Successfully loaded trust store"));

            assertTrue("Keystore type not being read", handler
                .checkLogContainsString("The key store type has been set in configuration " + "to JKS"));
            assertTrue("Keystore password not being read", handler
                .checkLogContainsString("The key store password was found to be set in "
                                        + "configuration and will be used."));
            assertTrue("Key password not being read", handler
                .checkLogContainsString("The key password was found to be set in "
                                        + "configuration and will be used."));

            if (this.isIBMJDK()) {

                assertTrue("Key manager factory is being being read from somewhere unknown", handler
                    .checkLogContainsString("The keystore key manager factory "
                                            + "algorithm has not been set in configuration "
                                            + "so the default value IbmX509 will be used."));
            } else {
                assertTrue("Key manager factory is being being read from somewhere unknown", handler
                    .checkLogContainsString("The keystore key manager factory "
                                            + "algorithm has not been set in configuration "
                                            + "so the default value SunX509 will be used."));
            }
            assertTrue("Trust manager factory is being being read from somewhere unknown", handler
                .checkLogContainsString("The truststore key manager factory "
                                        + "algorithm has not been set in configuration "
                                        + "so the default value PKIX will be used."));

            assertTrue("Ciphersuites is being being read from somewhere unknown", handler
                .checkLogContainsString("The cipher suite has not been set, default values "
                                        + "will be used."));
            assertTrue("Truststore type not being read", handler
                .checkLogContainsString("The key store type has been set in " + "configuration to JKS"));

            assertTrue("Secure socket protocol not being read", handler
                .checkLogContainsString("The secure socket protocol has been set to TLSv1."));
            assertTrue("Session caching set but no warning about not supported", handler
                .checkLogContainsString("Unsupported SSLClientPolicy property : " + "SessionCaching"));
            assertTrue("SessionCacheKey caching set but no warning about not supported", handler
                .checkLogContainsString("Unsupported SSLClientPolicy property : " + "SessionCacheKey"));
            assertTrue("MaxChainLength caching set but no warning about not supported", handler
                .checkLogContainsString("Unsupported SSLClientPolicy property : " + "MaxChainLength"));
            assertTrue("CertValidator caching set but no warning about not supported", handler
                .checkLogContainsString("Unsupported SSLClientPolicy property : " + "CertValidator"));
        } finally {
            System.getProperties().remove("celtix.security.configurer." + HTTP_CLIENT_CONFIG_ID.toString());
        }
    }
    */

    @Test
    public void testSetAllData() throws Exception {
        
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeystoreType("JKS");

        sslClientPolicy.setKeyPassword("defaultkeypass");
        sslClientPolicy.setKeystorePassword("defaultkeypass");
        sslClientPolicy.setTrustStoreType("JKS");
        //sslClientPolicy.setTrustStoreAlgorithm("JKS");
        sslClientPolicy.setSecureSocketProtocol("TLSv1");
        sslClientPolicy.setSessionCacheKey("Anything");
        sslClientPolicy.setSessionCaching(true);
        sslClientPolicy.setMaxChainLength(new Long(2));
        sslClientPolicy.setCertValidator("Anything");
        sslClientPolicy.setProxyHost("Anything");
        sslClientPolicy.setProxyPort(new Long(1234));
        for (int i = 0; i < EXPORT_CIPHERS.length; i++) {
            sslClientPolicy.getCiphersuites().add(EXPORT_CIPHERS[i]);
        }
        for (int i = 0; i < NON_EXPORT_CIPHERS.length; i++) {
            sslClientPolicy.getCiphersuites().add(NON_EXPORT_CIPHERS[i]);
        }
        
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(sslClientPolicy,
                                                          "https://dummyurl",
                                                          handler);

        factory.decorate(connection);
        SSLSocketFactory sSLSocketFactory = connection.getSSLSocketFactory();

        assertTrue(sSLSocketFactory instanceof SSLSocketFactoryWrapper);
        assertTrue("Keystore loaded success message not present", handler
            .checkLogContainsString("Successfully loaded keystore"));
        assertTrue("Trust store loaded success message not present", handler
            .checkLogContainsString("Successfully loaded trust store"));
        assertTrue("Keystore type not being read", handler
            .checkLogContainsString("The key store type has been set in configuration to JKS"));
        assertTrue("Keystore password not being read", handler
            .checkLogContainsString("The key store password was found to be set in "
                                    + "configuration and will be used."));
        assertTrue("Key password not being read", handler
            .checkLogContainsString("The key password was found to be set in "
                                    + "configuration and will be used."));
        if (this.isIBMJDK()) {

            assertTrue("Key manager factory is being being read from somewhere unknown", handler
                .checkLogContainsString("The keystore key manager factory "
                                        + "algorithm has not been set in configuration "
                                        + "so the default value IbmX509 will be used."));
        } else {
            assertTrue("Key manager factory is being being read from somewhere unknown", handler
                .checkLogContainsString("The keystore key manager factory "
                                        + "algorithm has not been set in configuration "
                                        + "so the default value SunX509 will be used."));
        }
        assertTrue("Trust manager factory is being being read from somewhere unknown", handler
            .checkLogContainsString("The truststore key manager factory "
                                    + "algorithm has not been set in configuration "
                                    + "so the default value PKIX will be used."));

        assertFalse("Ciphersuites config not picked up", handler
            .checkLogContainsString("The cipher suites have not been configured, " 
                                    + "default values will be used."));        
        assertFalse("Unexpected included ciphersuite filter",
                   handler.checkLogContainsString("suite is included by the filter."));
        assertFalse("Unexpected excluded ciphersuite fuilter",
                   handler.checkLogContainsString("suite is excluded by the filter."));
        assertFalse("Unexpected ciphersuite filtering",
                   handler.checkLogContainsString("The enabled cipher suites have been filtered down to"));
        
        assertTrue("Truststore type not being read", handler
            .checkLogContainsString("The key store type has been set in " + "configuration to JKS"));

        assertTrue("Secure socket protocol not being read", handler
            .checkLogContainsString("The secure socket protocol has been set to TLSv1."));
        assertTrue("Session caching set but no warning about not supported", handler
            .checkLogContainsString("Unsupported SSLClientPolicy property : SessionCaching"));
        assertTrue("SessionCacheKey caching set but no warning about not supported", handler
            .checkLogContainsString("Unsupported SSLClientPolicy property : SessionCacheKey"));
        assertTrue("MaxChainLength caching set but no warning about not supported", handler
            .checkLogContainsString("Unsupported SSLClientPolicy property : MaxChainLength"));
        assertTrue("CertValidator caching set but no warning about not supported", handler
            .checkLogContainsString("Unsupported SSLClientPolicy property : CertValidator"));
    }
    
    @Test
    public void testDefaultedCipherSuiteFilters() throws Exception {
        
        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeystoreType("JKS");

        sslClientPolicy.setKeyPassword("defaultkeypass");
        sslClientPolicy.setKeystorePassword("defaultkeypass");
        sslClientPolicy.setTrustStoreType("JKS");
        sslClientPolicy.setSecureSocketProtocol("TLSv1");

        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(sslClientPolicy,
                                                          "https://dummyurl",
                                                          handler);

        factory.decorate(connection);

        assertTrue("Ciphersuites is being being read from somewhere unknown", 
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
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeystoreType("JKS");

        sslClientPolicy.setKeyPassword("defaultkeypass");
        sslClientPolicy.setKeystorePassword("defaultkeypass");
        sslClientPolicy.setTrustStoreType("JKS");
        sslClientPolicy.setSecureSocketProtocol("TLSv1");

        // reverse default sense of include/exlcude
        FiltersType filters = new ObjectFactory().createFiltersType();
        for (int i = 0; i < NON_EXPORT_CIPHERS.length; i++) {
            filters.getInclude().add(NON_EXPORT_CIPHERS[i]);
        }
        for (int i = 0; i < EXPORT_CIPHERS.length; i++) {
            filters.getExclude().add(EXPORT_CIPHERS[i]);
        }
        sslClientPolicy.setCiphersuiteFilters(filters);
        
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(sslClientPolicy,
                                                          "https://dummyurl",
                                                          handler);

        factory.decorate(connection);

        assertTrue("Ciphersuites is being being read from somewhere unknown",
                   handler.checkLogContainsString("The cipher suites have not been configured," 
                                                  + " falling back to cipher suite filters."));
        assertFalse("Unexpected defaulted ciphersuite filters", 
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
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("defaultkeypass");
        sslClientPolicy.setKeystorePassword("defaultkeypass");

        sslClientPolicy.setKeystoreType("JKS");
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(sslClientPolicy,
                                                          "https://dummyurl",
                                                          handler);

        factory.decorate(connection);

        SSLSocketFactory sSLSocketFactory = connection.getSSLSocketFactory();

        assertTrue(sSLSocketFactory instanceof SSLSocketFactoryWrapper);
        assertTrue("Keystore loaded success message not present", handler
            .checkLogContainsString("Successfully loaded keystore"));
        assertTrue("Trust store loaded success message not present", handler
            .checkLogContainsString("Successfully loaded trust store"));

    }

    @Test
    public void testAllValidDataPKCS12() throws Exception {

        String keyStoreStr = getPath("resources/celtix.p12");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("celtixpass");
        sslClientPolicy.setKeystorePassword("celtixpass");

        sslClientPolicy.setKeystoreType("PKCS12");
        String trustStoreStr = getPath("resources/abigcompany_ca.pem");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(sslClientPolicy,
                                                          "https://dummyurl",
                                                          handler);
        factory.decorate(connection);
        SSLSocketFactory sSLSocketFactory = connection.getSSLSocketFactory();

        assertTrue(sSLSocketFactory instanceof SSLSocketFactoryWrapper);
        assertTrue("Keystore loaded success message not present", handler
            .checkLogContainsString("Successfully loaded keystore"));
        assertTrue("Trust store loaded success message not present", handler
            .checkLogContainsString("Successfully loaded trust store"));

    }

    @Test
    public void testNonExistentKeystoreJKS() throws Exception {

        String keyStoreStr = getPath("resources/defaultkeystoredontexist");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("defaultkeypass");
        sslClientPolicy.setKeystorePassword("defaultkeypass");

        sslClientPolicy.setKeystoreType("JKS");
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(sslClientPolicy,
                                                          "https://dummyurl",
                                                          handler);
        factory.decorate(connection);
        SSLSocketFactory sSLSocketFactory = connection.getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be null", !isNewSocketFactory);
        assertTrue("SSLContext should have failed, invalid keystore location", handler
            .checkLogContainsString("Problem initializing ssl for the outbound request"));

    }

    @Test
    public void testNonExistentKeystorePKCS12() throws Exception {

        String keyStoreStr = getPath("resources/defaultkeystoredontexist");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("celtixpass");
        sslClientPolicy.setKeystorePassword("celtixpass");

        String trustStoreStr = getPath("resources/abigcompany_ca.pem");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(sslClientPolicy,
                                                          "https://dummyurl",
                                                          handler);
        factory.decorate(connection);
        SSLSocketFactory sSLSocketFactory = connection.getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be null", !isNewSocketFactory);
        assertTrue("SSLContext should have failed, invalid keystore location", handler
            .checkLogContainsString("Problem initializing ssl for the outbound request"));

    }

    @Test
    public void testWrongKeystorePasswordJKS() throws Exception {

        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("defaultkeypass");
        sslClientPolicy.setKeystorePassword("defaultkeypasswrong");

        sslClientPolicy.setKeystoreType("JKS");
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(sslClientPolicy,
                                                          "https://dummyurl",
                                                          handler);
        factory.decorate(connection);
        SSLSocketFactory sSLSocketFactory = connection.getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be non and a new one", isNewSocketFactory);
        assertTrue("SSLContext init should have passed, but keystore initialization failed, invalid "
                   + "keystore password", handler.checkLogContainsString("Loading the keystore ")
                                          && handler
                                              .checkLogContainsString("failed with the following problem"));
        assertTrue("SSLContext init should have passed, but keystore initialization failed, invalid "
                   + "keystore password", handler
            .checkLogContainsString("Keystore was tampered with, or password was incorrect"));
        assertTrue("SSLContext init should have passed, but looks like trustore not loaded", handler
            .checkLogContainsString("Successfully loaded trust store"));
        assertTrue("Check to ensure keystore password and keypassword same failed", handler
            .checkLogContainsString("The value specified for the keystore password"
                                    + " is different to the key password. Currently "
                                    + "limitations in JSSE requires that they should be the "
                                    + "same. The keystore password value will be used only."));

    }

    @Test
    public void testWrongKeystorePasswordPKCS12() throws Exception {

        String keyStoreStr = getPath("resources/celtix.p12");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("celtixpass");
        sslClientPolicy.setKeystorePassword("celtixpasswrong");

        String trustStoreStr = getPath("resources/abigcompany_ca.pem");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(sslClientPolicy,
                                                          "https://dummyurl",
                                                          handler);
        factory.decorate(connection);
        SSLSocketFactory sSLSocketFactory = connection.getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be non and a new one", isNewSocketFactory);
        assertTrue("SSLContext init should have passed, but keystore initialization failed, invalid "
                   + "keystore password", handler.checkLogContainsString("Loading the keystore ")
                                          && handler
                                              .checkLogContainsString("failed with the following problem"));
        assertTrue("SSLContext init should have passed, but keystore initialization failed, invalid "
                   + "keystore password", handler.checkLogContainsString("Loading the keystore ")
                                          && handler
                                              .checkLogContainsString("failed with the following problem"));
        assertTrue("SSLContext init should have passed, but looks like trustore not loaded", handler
            .checkLogContainsString("Successfully loaded trust store"));
        assertTrue("Check to ensure keystore password and keypassword same failed", handler
            .checkLogContainsString("The value specified for the keystore password"
                                    + " is different to the key password. Currently "
                                    + "limitations in JSSE requires that they should be the "
                                    + "same. The keystore password value will be used only."));

    }

    @Test
    public void testWrongKeyPasswordJKS() throws Exception {

        String keyStoreStr = getPath("resources/defaultkeystore");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("defaultkeypasswrong");
        sslClientPolicy.setKeystorePassword("defaultkeypass");

        sslClientPolicy.setKeystoreType("JKS");
        String trustStoreStr = getPath("resources/defaulttruststore");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(sslClientPolicy,
                                                          "https://dummyurl",
                                                          handler);
        factory.decorate(connection);
        SSLSocketFactory sSLSocketFactory = connection.getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be non and a new one", isNewSocketFactory);
        assertTrue("SSLContext init should have passed, and keystore initialization succeedeed, "
                   + "EVEN THOUGH invalid key password", handler
            .checkLogContainsString("Successfully loaded keystore"));
        assertTrue("SSLContext init should have passed, but looks like trustore not loaded", handler
            .checkLogContainsString("Successfully loaded trust store"));
        assertTrue("Check to ensure keystore password and keypassword same failed", handler
            .checkLogContainsString("The value specified for the keystore password"
                                    + " is different to the key password. Currently "
                                    + "limitations in JSSE requires that they should be the "
                                    + "same. The keystore password value will be used only."));

    }

    @Test
    public void testWrongKeyPasswordPKCS12() throws Exception {

        String keyStoreStr = getPath("resources/celtix.p12");
        SSLClientPolicy sslClientPolicy = new SSLClientPolicy();
        sslClientPolicy.setKeystore(keyStoreStr);
        sslClientPolicy.setKeyPassword("celtixpasswrong");
        sslClientPolicy.setKeystorePassword("celtixpass");

        String trustStoreStr = getPath("resources/abigcompany_ca.pem");
        sslClientPolicy.setTrustStore(trustStoreStr);
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(sslClientPolicy,
                                                          "https://dummyurl",
                                                          handler);
        factory.decorate(connection);
        SSLSocketFactory sSLSocketFactory = connection.getSSLSocketFactory();
        boolean isNewSocketFactory = sSLSocketFactory instanceof SSLSocketFactoryWrapper;
        assertTrue("sSLSocketFactory should be non and a new one", isNewSocketFactory);
        assertTrue("SSLContext init should have passed, and keystore initialization succeedeed, "
                   + "EVEN THOUGH invalid key password", handler
            .checkLogContainsString("Successfully loaded keystore"));
        assertTrue("SSLContext init should have passed, but looks like trustore not loaded", handler
            .checkLogContainsString("Successfully loaded trust store"));
        assertTrue("Check to ensure keystore password and keypassword same failed", handler
            .checkLogContainsString("The value specified for the keystore password"
                                    + " is different to the key password. Currently "
                                    + "limitations in JSSE requires that they should be the "
                                    + "same. The keystore password value will be used only."));

    }

    @Test
    public void testAllElementsHaveSetupMethod() throws Exception {
        SSLClientPolicy policy = new SSLClientPolicy();
        TestLogHandler handler = new TestLogHandler();
        HttpsURLConnectionFactory factory = createFactory(policy,
                                                          "https://dummyurl",
                                                          handler);
        assertTrue("A new element has been " + "added to SSLClientPolicy without a corresponding "
                   + "setup method in the configurer.",
                   SSLUtils.testAllDataHasSetupMethod(policy,
                                                      factory.getUnSupported(),
                                                      factory.getDerivative()));
    }

    private HttpsURLConnectionFactory createFactory(SSLClientPolicy policy,
                                                    String urlStr,
                                                    TestLogHandler handler) 
        throws Exception {
        HttpsURLConnectionFactory factory =
            new HttpsURLConnectionFactory(policy);
        factory.addLogHandler(handler);
        return factory;
    }

    protected static String getPath(String fileName) throws URISyntaxException {
        URL keystoreURL = HttpsURLConnectionFactoryTest.class.getResource(".");
        String str = keystoreURL.toURI().getPath();
        str += DROP_BACK_SRC_DIR + fileName;
        return str;
    }

    public boolean isIBMJDK() {
        if (System.getProperty("java.vendor").indexOf("IBM") > -1) {
            return true;
        }
        return false;
    }
}
