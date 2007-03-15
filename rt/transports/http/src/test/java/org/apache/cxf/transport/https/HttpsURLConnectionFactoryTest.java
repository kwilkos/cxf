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

import junit.framework.TestCase;

import org.apache.cxf.configuration.security.SSLClientPolicy;


public class HttpsURLConnectionFactoryTest extends TestCase {

    protected static final String DROP_BACK_SRC_DIR = 
        "../../../../../../../"
        + "src/test/java/org/apache/cxf/transport/https/";

    private TestHttpsURLConnection connection;
    
    public HttpsURLConnectionFactoryTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HttpsURLConnectionFactoryTest.class);
    }

    public void setUp() throws Exception {
        connection = new TestHttpsURLConnection(null);
    }

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

        assertTrue("Ciphersuites is being being read from somewhere unknown", handler
            .checkLogContainsString("The cipher suite has not been set, default values " + "will be used."));
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
