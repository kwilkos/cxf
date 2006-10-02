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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.security.SSLClientPolicy;
import org.apache.cxf.transport.http.URLConnectionFactory;


public final class HttpsURLConnectionFactory implements URLConnectionFactory {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG =
        LogUtils.getL7dLogger(HttpsURLConnectionFactory.class);
    private static final String DEFAULT_KEYSTORE_TYPE = "PKCS12";
    private static final String DEFAULT_TRUST_STORE_TYPE = "JKS";
    private static final String DEFAULT_SECURE_SOCKET_PROTOCOL = "TLSv1";
    private static final String CERTIFICATE_FACTORY_TYPE = "X.509";
    private static final String PKCS12_TYPE = "PKCS12";
    
    // REVISIT inject this resource
    SSLClientPolicy sslPolicy;
    
    /**
     * Create a URLConnection, proxified if neccessary.
     * 
     * @param proxy non-null if connection should be proxified
     * @param url the target URL
     * @return an appropriate URLConnection
     */
    public URLConnection createConnection(Proxy proxy, URL url)
        throws IOException {
        URLConnection connection = proxy != null 
                                   ? url.openConnection(proxy)
                                   : url.openConnection();
        if (connection instanceof HttpsURLConnection) {
            decorate((HttpsURLConnection)connection);
        }
        return connection;
    }
    
    protected void setSSLPolicy(SSLClientPolicy p) {
        sslPolicy = p;
    }
    
    protected void decorate(HttpsURLConnection connection) {
        String keyStoreLocation = setupKeystore();
        String keyStoreType = setupKeystoreType();
        String keyStorePassword = setupKeystorePassword();
        String keyPassword = setupKeyPassword();
        String keystoreKeyManagerFactoryAlgorithm = setupKeystoreAlgorithm();
        String trustStoreKeyManagerFactoryAlgorithm = setupTrustStoreAlgorithm();
        String[] cipherSuites = setupCiphersuites();
        String trustStoreLocation = setupTrustStore();
        String trustStoreType = setupTrustStoreType();
        String secureSocketProtocol = setupSecureSocketProtocol();
        setupSessionCaching();
        setupSessionCacheKey();
        setupMaxChainLength();
        setupCertValidator();
        setupProxyHost();
        setupProxyPort();
        
        try {
            SSLContext sslctx = SSLContext.getInstance(secureSocketProtocol);
            boolean pkcs12 = keyStoreType.equalsIgnoreCase(PKCS12_TYPE);
            sslctx.init(getKeyStoreManagers(pkcs12,
                                            keyStoreLocation,
                                            keyStoreType,
                                            keyStorePassword,
                                            keyPassword,
                                            keystoreKeyManagerFactoryAlgorithm,
                                            secureSocketProtocol),
                        getTrustStoreManagers(pkcs12,
                                              trustStoreType,
                                              trustStoreLocation,
                                              trustStoreKeyManagerFactoryAlgorithm),
                        null);
            connection.setSSLSocketFactory(new SSLSocketFactoryWrapper(sslctx.getSocketFactory(), 
                                                                       cipherSuites));
        } catch (Exception e) {
            LogUtils.log(LOG, Level.SEVERE, "SSL_CONTEXT_INIT_FAILURE", e);
        }
    }
    
    private KeyManager[] getKeyStoreManagers(boolean pkcs12,
                                             String keyStoreLocation,
                                             String keyStoreType,
                                             String keyStorePassword,
                                             String keyPassword,
                                             String keystoreKeyManagerFactoryAlgorithm,
                                             String secureSocketProtocol)
        throws Exception {
        //TODO for performance reasons we should cache
        // the KeymanagerFactory and TrustManagerFactory 
        if ((keyStorePassword != null)
            && (keyPassword != null) 
            && (!keyStorePassword.equals(keyPassword))) {
            LogUtils.log(LOG, Level.WARNING, "KEY_PASSWORD_NOT_SAME_KEYSTORE_PASSWORD");
        }
        KeyManager[] keystoreManagers = null;        
        KeyManagerFactory kmf = 
            KeyManagerFactory.getInstance(keystoreKeyManagerFactoryAlgorithm);  
        KeyStore ks = KeyStore.getInstance(keyStoreType);
        
        if (pkcs12) {
            FileInputStream fis = new FileInputStream(keyStoreLocation);
            DataInputStream dis = new DataInputStream(fis);
            byte[] bytes = new byte[dis.available()];
            dis.readFully(bytes);
            ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
            
            if (keyStorePassword != null) {
                keystoreManagers =
                    loadKeyStore(kmf, ks, bin, keyStoreLocation, keyStorePassword);
            }
        } else {        
            byte[] sslCert = loadClientCredential(keyStoreLocation);
            
            if (sslCert != null && sslCert.length > 0 && keyStorePassword != null) {
                ByteArrayInputStream bin = new ByteArrayInputStream(sslCert);
                keystoreManagers =
                    loadKeyStore(kmf, ks, bin, keyStoreLocation, keyStorePassword);
            }  
        }
        if ((keyStorePassword == null) && (keyStoreLocation != null)) {
            LogUtils.log(LOG, Level.WARNING,
                         "FAILED_TO_LOAD_KEYSTORE_NULL_PASSWORD", 
                         new Object[]{keyStoreLocation});
        }
        return keystoreManagers;
    }

    private KeyManager[] loadKeyStore(KeyManagerFactory kmf,
                                      KeyStore ks,
                                      ByteArrayInputStream bin,
                                      String keyStoreLocation,
                                      String keyStorePassword) {
        KeyManager[] keystoreManagers = null;
        try {
            ks.load(bin, keyStorePassword.toCharArray());
            kmf.init(ks, keyStorePassword.toCharArray());
            keystoreManagers = kmf.getKeyManagers();
            LogUtils.log(LOG, Level.INFO, "LOADED_KEYSTORE", new Object[]{keyStoreLocation});
        } catch (Exception e) {
            LogUtils.log(LOG, Level.WARNING, "FAILED_TO_LOAD_KEYSTORE", 
                     new Object[]{keyStoreLocation, e.getMessage()});
        } 
        return keystoreManagers;
    }

    private TrustManager[] getTrustStoreManagers(boolean pkcs12,
                                                 String trustStoreType,
                                                 String trustStoreLocation,
                                                 String trustStoreKeyManagerFactoryAlgorithm)
        throws Exception {
        // ************************* Load Trusted CA file *************************
        
        TrustManager[] trustStoreManagers = null;
        KeyStore trustedCertStore = KeyStore.getInstance(trustStoreType);

        if (pkcs12) {
            //TODO could support multiple trust cas
            trustStoreManagers = new TrustManager[1];
            
            trustedCertStore.load(null, "".toCharArray());
            CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_FACTORY_TYPE);
            byte[] caCert = loadCACert(trustStoreLocation);
            try {
                if (caCert != null) {
                    ByteArrayInputStream cabin = new ByteArrayInputStream(caCert);
                    X509Certificate cert = (X509Certificate)cf.generateCertificate(cabin);
                    trustedCertStore.setCertificateEntry(cert.getIssuerDN().toString(), cert);
                    cabin.close();
                }
            } catch (Exception e) {
                LogUtils.log(LOG, Level.WARNING, "FAILED_TO_LOAD_TRUST_STORE", 
                             new Object[]{trustStoreLocation, e.getMessage()});
            } 
        } else {
            trustedCertStore.load(new FileInputStream(trustStoreLocation), null);
        }
        
        TrustManagerFactory tmf  = 
            TrustManagerFactory.getInstance(trustStoreKeyManagerFactoryAlgorithm);
        tmf.init(trustedCertStore);
        LogUtils.log(LOG, Level.INFO, "LOADED_TRUST_STORE", new Object[]{trustStoreLocation});            
        trustStoreManagers = tmf.getTrustManagers();

        return trustStoreManagers;
    }
    
    private static byte[] loadClientCredential(String fileName) throws IOException {
        if (fileName == null) {
            return null;
        }
        FileInputStream in = new FileInputStream(fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int i = in.read(buf);
        while (i  > 0) {
            out.write(buf, 0, i);
            i = in.read(buf);
        }
        in.close();
        return out.toByteArray();
    }

    private static byte[] loadCACert(String fileName) throws IOException {
        if (fileName == null) {
            return null;
        }
        FileInputStream in = new FileInputStream(fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int i = in.read(buf);
        
        while (i > 0) {
            out.write(buf, 0, i);
            i = in.read(buf);
        }
        in.close();
        return out.toByteArray();
    }

    public String setupKeystore() {
        String keyStoreLocation = null;
        String logMsg = null;
        if (sslPolicy.isSetKeystore()) {
            keyStoreLocation = sslPolicy.getKeystore();
            logMsg = "KEY_STORE_SET";
        } else {
            keyStoreLocation = System.getProperty("javax.net.ssl.keyStore");
            if (keyStoreLocation != null) {
                logMsg = "KEY_STORE_SYSTEM_PROPERTY_SET";
            } else {
                keyStoreLocation = System.getProperty("user.home") + "/.keystore";
                logMsg = "KEY_STORE_NOT_SET";
            }
        }
        LogUtils.log(LOG, Level.INFO, logMsg, new Object[]{keyStoreLocation});
        return keyStoreLocation;
    }
    
    public String setupKeystoreType() {
        String keyStoreType = null;
        String logMsg = null;
        if (sslPolicy.isSetKeystoreType()) {
            keyStoreType = sslPolicy.getKeystoreType();
            logMsg = "KEY_STORE_TYPE_SET";
        } else {
            keyStoreType = DEFAULT_KEYSTORE_TYPE;
            logMsg = "KEY_STORE_TYPE_NOT_SET";
        }
        LogUtils.log(LOG, Level.INFO, logMsg, new Object[]{keyStoreType});
        return keyStoreType;
    }  
    
    public String setupKeystorePassword() {
        String keyStorePassword = null;
        String logMsg = null;
        if (sslPolicy.isSetKeystorePassword()) {
            logMsg = "KEY_STORE_PASSWORD_SET";
            keyStorePassword = sslPolicy.getKeystorePassword();
        } else {
            keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
            logMsg = keyStorePassword != null
                     ? "KEY_STORE_PASSWORD_SYSTEM_PROPERTY_SET"
                     : "KEY_STORE_PASSWORD_NOT_SET";
        }
        LogUtils.log(LOG, Level.INFO, logMsg);
        return keyStorePassword;        
    }
    
    public String setupKeyPassword() {
        String keyPassword = null;
        String logMsg = null;
        if (sslPolicy.isSetKeyPassword()) {
            logMsg = "KEY_PASSWORD_SET";
            keyPassword = sslPolicy.getKeyPassword();
        } else {
            keyPassword = System.getProperty("javax.net.ssl.keyStorePassword");
            logMsg = keyPassword != null
                     ? "KEY_PASSWORD_SYSTEM_PROPERTY_SET"
                     : "KEY_PASSWORD_NOT_SET";
        }
        LogUtils.log(LOG, Level.INFO, logMsg);
        return keyPassword;
    }

    public String setupKeystoreAlgorithm() {
        String keystoreKeyManagerFactoryAlgorithm = null;
        String logMsg = null;
        if (sslPolicy.isSetKeystoreAlgorithm()) {
            keystoreKeyManagerFactoryAlgorithm = sslPolicy.getKeystoreAlgorithm(); 
            logMsg = "KEY_STORE_ALGORITHM_SET";
        } else {
            keystoreKeyManagerFactoryAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            logMsg = "KEY_STORE_ALGORITHM_NOT_SET";
        }
        LogUtils.log(LOG, Level.INFO, logMsg, 
                     new Object[] {keystoreKeyManagerFactoryAlgorithm});
        return keystoreKeyManagerFactoryAlgorithm;
    } 
    
    public String setupTrustStoreAlgorithm() {
        String trustStoreKeyManagerFactoryAlgorithm = null;
        String logMsg = null;
        if (sslPolicy.isSetKeystoreAlgorithm()) {
            trustStoreKeyManagerFactoryAlgorithm = sslPolicy.getTrustStoreAlgorithm(); 
            logMsg = "TRUST_STORE_ALGORITHM_SET";
        } else {
            trustStoreKeyManagerFactoryAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            logMsg = "TRUST_STORE_ALGORITHM_NOT_SET";
        }
        LogUtils.log(LOG, Level.INFO, logMsg, 
                     new Object[] {trustStoreKeyManagerFactoryAlgorithm});
        return trustStoreKeyManagerFactoryAlgorithm;
    }    
    
    public String[] setupCiphersuites() {
        String[] cipherSuites = null;
        if (sslPolicy.isSetCiphersuites()) {
            
            List<String> cipherSuitesList = sslPolicy.getCiphersuites();
            int numCipherSuites = cipherSuitesList.size();
            cipherSuites = new String[numCipherSuites];
            String ciphsStr = null;
            for (int i = 0; i < numCipherSuites; i++) {
                cipherSuites[i] = cipherSuitesList.get(i);
                if (ciphsStr == null) {
                    ciphsStr = cipherSuites[i];
                } else {
                    ciphsStr += ", " + cipherSuites[i];
                }
            }
            LogUtils.log(LOG, Level.INFO, "CIPHERSUITE_SET", new Object[]{ciphsStr});
        } else {
            LogUtils.log(LOG, Level.INFO, "CIPHERSUITE_NOT_SET");
        }
        return cipherSuites;
    }         
    
    public String setupTrustStore() {
        String trustStoreLocation;
        String logMsg = null;
        if (sslPolicy.isSetTrustStore()) {
            trustStoreLocation = sslPolicy.getTrustStore();
            logMsg = "TRUST_STORE_SET";
        } else {
            
            trustStoreLocation = System.getProperty("javax.net.ssl.trustStore");
            if (trustStoreLocation != null) {
                logMsg = "TRUST_STORE_SYSTEM_PROPERTY_SET";
            } else {
                trustStoreLocation =
                    System.getProperty("java.home") + "/lib/security/cacerts";
                logMsg = "TRUST_STORE_NOT_SET";
            }
        }
        LogUtils.log(LOG, Level.INFO, logMsg, new Object[]{trustStoreLocation});
        return trustStoreLocation;
    }
    
    public String setupTrustStoreType() {
        String trustStoreType = null;
        String logMsg = null;
        if (sslPolicy.isSetTrustStoreType()) {
            trustStoreType = sslPolicy.getTrustStoreType();
            logMsg = "TRUST_STORE_TYPE_SET";
        } else {
            //Can default to JKS
            trustStoreType = DEFAULT_TRUST_STORE_TYPE;
            logMsg = "TRUST_STORE_TYPE_NOT_SET";
        }
        LogUtils.log(LOG, Level.INFO, logMsg, new Object[]{trustStoreType});
        return trustStoreType;
    }
    
    public String setupSecureSocketProtocol() {
        String secureSocketProtocol = null;
        if (!sslPolicy.isSetSecureSocketProtocol()) {
            LogUtils.log(LOG, Level.INFO, "SECURE_SOCKET_PROTOCOL_NOT_SET");
            secureSocketProtocol = DEFAULT_SECURE_SOCKET_PROTOCOL;
        } else {
            secureSocketProtocol = sslPolicy.getSecureSocketProtocol();
            LogUtils.log(LOG,
                         Level.INFO,
                         "SECURE_SOCKET_PROTOCOL_SET",
                         new Object[] {secureSocketProtocol});
        }
        return secureSocketProtocol;
    }
    
    public boolean setupSessionCaching() {
        if (sslPolicy.isSetSessionCaching()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_CLIENT_POLICY_DATA", 
                         new Object[]{"SessionCaching"});
        }
        return true;
    }  
    
    public boolean setupSessionCacheKey() {
        if (sslPolicy.isSetSessionCacheKey()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_CLIENT_POLICY_DATA", 
                         new Object[]{"SessionCacheKey"});
        }
        return true;
    }  
    
    public boolean setupMaxChainLength() {
        if (sslPolicy.isSetMaxChainLength()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_CLIENT_POLICY_DATA", 
                         new Object[]{"MaxChainLength"});
        }
        return true;
    }  
    
    public boolean setupCertValidator() {
        if (sslPolicy.isSetCertValidator()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_CLIENT_POLICY_DATA", 
                         new Object[]{"CertValidator"});
        }
        return true;
    }      
    
    public boolean setupProxyHost() {
        if (sslPolicy.isSetProxyHost()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_CLIENT_POLICY_DATA", 
                         new Object[]{"ProxyHost"});
        }
        return true;
    } 

    public boolean setupProxyPort() {
        if (sslPolicy.isSetProxyPort()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_CLIENT_POLICY_DATA", 
                         new Object[]{"ProxyPort"});
        }
        return true;
    } 
    
    /*
     *  For development and testing only
     */   
    protected boolean testAllDataHasSetupMethod() {
        Method[] sslPolicyMethods = sslPolicy.getClass().getDeclaredMethods();
        Class[] classArgs = null;

        for (int i = 0; i < sslPolicyMethods.length; i++) {
            String sslPolicyMethodName = sslPolicyMethods[i].getName();
            if (sslPolicyMethodName.startsWith("isSet")) {
                String dataName = 
                    sslPolicyMethodName.substring("isSet".length(), sslPolicyMethodName.length());
                String thisMethodName = "setup" + dataName;
                try {
                    this.getClass().getMethod(thisMethodName, classArgs);
                } catch (Exception e) {
                    e.printStackTrace(); 
                    return false;
                }
                
            }
        }
        return true;
    }
    
    protected void addLogHandler(Handler handler) {
        LOG.addHandler(handler);
    }
}

