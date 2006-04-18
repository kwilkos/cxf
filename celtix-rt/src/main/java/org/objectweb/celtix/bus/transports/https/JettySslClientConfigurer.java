package org.objectweb.celtix.bus.transports.https;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
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

import org.objectweb.celtix.bus.configuration.security.SSLClientPolicy;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;



public final class JettySslClientConfigurer {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LogUtils.getL7dLogger(JettySslClientConfigurer.class);
    private static final String DEFAUL_KEYSTORE_TYPE = "PKCS12";
    private static final String DEFAUL_TRUST_STORE_TYPE = "JKS";
    private static final String DEFAULT_SECURE_SOCKET_PROTOCOL = "TLSv1";
    private static final String CERTIFICATE_FACTORY_TYPE    = "X.509";
    private static final String PKCS12_TYPE = "PKCS12";
    
    SSLClientPolicy sslPolicy;
    
    private String keyStoreLocation;
    private String keyStorePassword;
    private String keyPassword;
    private String keyStoreType = DEFAUL_KEYSTORE_TYPE;
    private String[] cipherSuites;
    private String trustStoreLocation;
    private String trustStoreType = DEFAUL_TRUST_STORE_TYPE;
    private String keystoreKeyManagerFactoryAlgorithm;
    private String trustStoreKeyManagerFactoryAlgorithm;
    private HttpsURLConnection httpsConnection;
    private String secureSocketProtocol;
    private Configuration config;
    
    public JettySslClientConfigurer(SSLClientPolicy sslPolicyParam,
                                       URLConnection connection,
                                       Configuration configurationParam) {
        
        this.sslPolicy = sslPolicyParam;
        this.httpsConnection = (HttpsURLConnection)connection;
        
        config = configurationParam;
        
    }
    
    public void configure() {
        setupSecurityConfigurer();
        setupKeystore();
        setupKeystoreType();
        setupKeystorePassword();
        setupKeyPassword();
        setupKeystoreAlgorithm();
        setupTrustStoreAlgorithm();
        setupCiphersuites();
        setupTrustStore();
        setupTrustStoreType();
        setupSecureSocketProtocol();
        setupSessionCaching();
        setupSessionCacheKey();
        setupMaxChainLength();
        setupCertValidator();
        setupProxyHost();
        setupProxyPort();
        
        if (keyStoreType.equalsIgnoreCase(PKCS12_TYPE)) { 
            setupSSLContextPKCS12();
        } else {
            setupSSLContext();
        }

    }
    
    private boolean setupSSLContext() {
        
        //TODO for performance reasons we should cache the KeymanagerFactory and TrustManagerFactory 
        if ((keyStorePassword != null) && (keyPassword != null) && (!keyStorePassword.equals(keyPassword))) {
            LogUtils.log(LOG, Level.WARNING, "KEY_PASSWORD_NOT_SAME_KEYSTORE_PASSWORD");
        }
        try {
            SSLContext sslctx = SSLContext.getInstance(secureSocketProtocol);

            KeyManagerFactory kmf = 
                KeyManagerFactory.getInstance(keystoreKeyManagerFactoryAlgorithm);  
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            FileInputStream fis = new FileInputStream(keyStoreLocation);
            DataInputStream dis = new DataInputStream(fis);
            byte[] bytes = new byte[dis.available()];
            dis.readFully(bytes);
            ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
            
            KeyManager[] keystoreManagers = null;
            if (keyStorePassword != null) {
                try {
                    ks.load(bin, keyStorePassword.toCharArray());
                    kmf.init(ks, keyStorePassword.toCharArray());
                    keystoreManagers = kmf.getKeyManagers();
                    LogUtils.log(LOG, Level.INFO, "LOADED_KEYSTORE", new Object[]{keyStoreLocation});
                } catch (Exception e) {
                    LogUtils.log(LOG, Level.WARNING, "FAILED_TO_LOAD_KEYSTORE", new Object[]{e});
                }  
            }
            
            // ************************* Load Trusted CA file *************************
            
            TrustManager[] trustStoreManagers = null;
            KeyStore trustedCertStore = KeyStore.getInstance(trustStoreType);
            
            trustedCertStore.load(new FileInputStream(trustStoreLocation), null);
            TrustManagerFactory tmf  = 
                TrustManagerFactory.getInstance(trustStoreKeyManagerFactoryAlgorithm);
            try {
                tmf.init(trustedCertStore);
                trustStoreManagers = tmf.getTrustManagers();
                LogUtils.log(LOG, Level.INFO, "LOADED_TRUST_STORE", new Object[]{trustStoreLocation});
            } catch (Exception e) {
                LogUtils.log(LOG, Level.WARNING, "FAILED_TO_LOAD_TRUST_STORE", new Object[]{e.getMessage()});
            } 
            sslctx.init(keystoreManagers, trustStoreManagers, null);
            
            httpsConnection.setSSLSocketFactory(new SSLSocketFactoryWrapper(sslctx.getSocketFactory(), 
                                                                            cipherSuites));
            
            
            
        } catch (Exception e) {
            LogUtils.log(LOG, Level.SEVERE, "SSL_CONTEXT_INIT_FAILURE", new Object[]{e.getMessage()});
            return false;
        }   
        return true;
    }
    
    
    private boolean setupSSLContextPKCS12() {
        
        //TODO for performance reasons we should cache the KeymanagerFactory and TrustManagerFactory 
        if ((keyStorePassword != null) && (keyPassword != null) && (!keyStorePassword.equals(keyPassword))) {
            LogUtils.log(LOG, Level.WARNING, "KEY_PASSWORD_NOT_SAME_KEYSTORE_PASSWORD");
        }
        try {
            SSLContext sslctx = SSLContext.getInstance(secureSocketProtocol);
            KeyManagerFactory kmf = 
                KeyManagerFactory.getInstance(keystoreKeyManagerFactoryAlgorithm);  
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            KeyManager[] keystoreManagers = null;
            
            
            byte[] sslCert = loadClientCredential(keyStoreLocation);
            
            if (sslCert != null && sslCert.length > 0 && keyStorePassword != null) {
                ByteArrayInputStream bin = new ByteArrayInputStream(sslCert);
                try {
                    ks.load(bin, keyStorePassword.toCharArray());
                    kmf.init(ks, keyStorePassword.toCharArray());
                    keystoreManagers = kmf.getKeyManagers();
                    LogUtils.log(LOG, Level.INFO, "LOADED_KEYSTORE", new Object[]{keyStoreLocation});
                } catch (Exception e) {
                    LogUtils.log(LOG, Level.WARNING, "FAILED_TO_LOAD_KEYSTORE", new Object[]{e});
                } 
            }             
            
            // ************************* Load Trusted CA file *************************
            //TODO could support multiple trust cas
            TrustManager[] trustStoreManagers = new TrustManager[1];
             
            KeyStore trustedCertStore = KeyStore.getInstance(trustStoreType);
            trustedCertStore.load(null, "".toCharArray());
            CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_FACTORY_TYPE);
            byte[] caCert = loadCACert(trustStoreLocation);
            if (caCert != null) {
                ByteArrayInputStream cabin = new ByteArrayInputStream(caCert);
                X509Certificate cert = (X509Certificate) cf.generateCertificate(cabin);
                trustedCertStore.setCertificateEntry(cert.getIssuerDN().toString(), cert);
                cabin.close();
            }
            TrustManagerFactory tmf  = 
                TrustManagerFactory.getInstance(trustStoreKeyManagerFactoryAlgorithm);

            tmf.init(trustedCertStore);
            LogUtils.log(LOG, Level.INFO, "LOADED_TRUST_STORE", new Object[]{trustStoreLocation});
            
            trustStoreManagers = tmf.getTrustManagers();

 
            sslctx.init(keystoreManagers, trustStoreManagers, null);  
            httpsConnection.setSSLSocketFactory(new SSLSocketFactoryWrapper(sslctx.getSocketFactory(), 
                                                                            cipherSuites)); 
            
        } catch (Exception e) {
            LogUtils.log(LOG, Level.SEVERE, "SSL_CONTEXT_INIT_FAILURE", new Object[]{e.getMessage()});
            return false;
        }   
        return true;
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

    
    public void setupKeystore() {
        if (sslPolicy.isSetKeystore()) {
            keyStoreLocation = sslPolicy.getKeystore();
            LogUtils.log(LOG, Level.INFO, "KEY_STORE_SET", new Object[]{keyStoreLocation});
            return;
        }
        keyStoreLocation = System.getProperty("javax.net.ssl.keyStore");
        if (keyStoreLocation != null) {
            LogUtils.log(LOG, Level.INFO, "KEY_STORE_SYSTEM_PROPERTY_SET", new Object[]{keyStoreLocation});
            return;
        }

        //Not returning false because should default
        keyStoreLocation = System.getProperty("user.home") + "/.keystore";
        LogUtils.log(LOG, Level.INFO, "KEY_STORE_NOT_SET", new Object[]{keyStoreLocation});

    }
    
    public void setupKeystoreType() {
        if (!sslPolicy.isSetKeystoreType()) {
            LogUtils.log(LOG, Level.INFO, "KEY_STORE_TYPE_NOT_SET", new Object[]{DEFAUL_KEYSTORE_TYPE});
            //Not returning false because does not have to be set
            return;
        }
        keyStoreType = sslPolicy.getKeystoreType();
        LogUtils.log(LOG, Level.INFO, "KEY_STORE_TYPE_SET", new Object[]{keyStoreType});
    }  
    
    public void setupKeystorePassword() {
        if (sslPolicy.isSetKeystorePassword()) {
            LogUtils.log(LOG, Level.INFO, "KEY_STORE_PASSWORD_SET");
            keyStorePassword = sslPolicy.getKeystorePassword();
            return;
        }
        keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
        if (keyStorePassword != null) {
            LogUtils.log(LOG, Level.INFO, "KEY_STORE_PASSWORD_SYSTEM_PROPERTY_SET");
            return;
        }
        LogUtils.log(LOG, Level.INFO, "KEY_STORE_PASSWORD_NOT_SET");

    }
    
    public void setupKeyPassword() {
        if (sslPolicy.isSetKeyPassword()) {
            LogUtils.log(LOG, Level.INFO, "KEY_PASSWORD_SET");
            keyPassword = sslPolicy.getKeyPassword();
            return;
        }
        keyPassword = System.getProperty("javax.net.ssl.keyStorePassword");
        if (keyPassword != null) {
            LogUtils.log(LOG, Level.INFO, "KEY_PASSWORD_SYSTEM_PROPERTY_SET");
            return;
        }

        LogUtils.log(LOG, Level.INFO, "KEY_PASSWORD_NOT_SET");
    }
   
    
    
    public void setupKeystoreAlgorithm() {
        if (sslPolicy.isSetKeystoreAlgorithm()) {
            keystoreKeyManagerFactoryAlgorithm = sslPolicy.getKeystoreAlgorithm(); 
            LogUtils.log(LOG, Level.INFO, 
                         "KEY_STORE_ALGORITHM_SET", 
                         new Object[] {keystoreKeyManagerFactoryAlgorithm});
            return;
        }
        keystoreKeyManagerFactoryAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        LogUtils.log(LOG, Level.INFO, 
                     "KEY_STORE_ALGORITHM_NOT_SET", 
                     new Object[] {keystoreKeyManagerFactoryAlgorithm});
    } 
    
    public void setupTrustStoreAlgorithm() {
        if (sslPolicy.isSetKeystoreAlgorithm()) {
            trustStoreKeyManagerFactoryAlgorithm = sslPolicy.getTrustStoreAlgorithm(); 
            LogUtils.log(LOG, Level.INFO, 
                         "TRUST_STORE_ALGORITHM_SET", 
                         new Object[] {trustStoreKeyManagerFactoryAlgorithm});
            return;
        }
        trustStoreKeyManagerFactoryAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        LogUtils.log(LOG, Level.INFO, 
                     "TRUST_STORE_ALGORITHM_NOT_SET", 
                     new Object[] {trustStoreKeyManagerFactoryAlgorithm});
    }    
    
    public void setupCiphersuites() {
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
            return;
        }
        LogUtils.log(LOG, Level.INFO, "CIPHERSUITE_NOT_SET");
    }         
    
    public void setupTrustStore() {
        if (sslPolicy.isSetTrustStore()) {
            trustStoreLocation = sslPolicy.getTrustStore();
            LogUtils.log(LOG, Level.INFO, "TRUST_STORE_SET", new Object[]{trustStoreLocation});
            return;
        }
        
        trustStoreLocation = System.getProperty("javax.net.ssl.trustStore");
        if (trustStoreLocation != null) {
            LogUtils.log(LOG, Level.INFO, "TRUST_STORE_SYSTEM_PROPERTY_SET", 
                         new Object[]{trustStoreLocation});
            return;
        }

        trustStoreLocation = System.getProperty("java.home") + "/lib/security/cacerts";
        LogUtils.log(LOG, Level.INFO, "TRUST_STORE_NOT_SET", new Object[]{trustStoreLocation});
        
    }
    
    public void setupTrustStoreType() {
        if (!sslPolicy.isSetTrustStoreType()) {
            LogUtils.log(LOG, Level.INFO, "TRUST_STORE_TYPE_NOT_SET", new Object[]{DEFAUL_TRUST_STORE_TYPE});
            //Can default to JKS so return
            return;
        }
        trustStoreType = sslPolicy.getTrustStoreType();
        LogUtils.log(LOG, Level.INFO, "TRUST_STORE_TYPE_SET", new Object[]{trustStoreType});
    }

    
    public void setupSecureSocketProtocol() {
        if (!sslPolicy.isSetSecureSocketProtocol()) {
            LogUtils.log(LOG, Level.INFO, "SECURE_SOCKET_PROTOCOL_NOT_SET");
            secureSocketProtocol = DEFAULT_SECURE_SOCKET_PROTOCOL;
            return;
        }
        secureSocketProtocol = sslPolicy.getSecureSocketProtocol();
        LogUtils.log(LOG, Level.INFO, "SECURE_SOCKET_PROTOCOL_SET", new Object[] {secureSocketProtocol});
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
    
    
    public void setupSecurityConfigurer() {
        String systemProperty = "celtix.security.configurer.celtix."
            + config.getId() + ".http-client";
        String securityConfigurerName = 
            System.getProperty(systemProperty);
        if ((securityConfigurerName == null) 
            || (securityConfigurerName.equals(""))) {
            return;
        }

        try {
            Class clazz = Class.forName(securityConfigurerName);
            Method configure = clazz.getDeclaredMethod("configure", SSLClientPolicy.class);
            Object[] params = new Object[]{sslPolicy};
            Object configurer = clazz.newInstance();
            configure.invoke(configurer, params);
            LogUtils.log(LOG, Level.INFO, "SUCCESS_INVOKING_SECURITY_CONFIGURER", 
                         new Object[]{securityConfigurerName});
        } catch (Exception e) {
            LogUtils.log(LOG, Level.SEVERE, "ERROR_INVOKING_SECURITY_CONFIGURER", 
                         new Object[]{securityConfigurerName, e.getMessage()});
        }
    }
    
    protected HttpsURLConnection getHttpsConnection() {
        return httpsConnection;
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

