package org.objectweb.celtix.bus.transports.https;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;

import org.mortbay.http.SslListener;
import org.objectweb.celtix.bus.configuration.security.SSLServerPolicy;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;


public final class JettySslListenerConfigurer {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LogUtils.getL7dLogger(JettySslListenerConfigurer.class);
    private static final String DEFAUL_KEYSTORE_TYPE = "PKCS12";
    private static final String DEFAULT_SECURE_SOCKET_PROTOCOL = "TLSv1";
    private static final boolean DEFAULT_REQUIRE_CLIENT_AUTHENTICATION = false;
    private static final boolean DEFAULT_WANT_CLIENT_AUTHENTICATION = true;
    
    
    private Configuration config;
    private SSLServerPolicy sslPolicy;
    private SslListener secureListener;
    
        
    public JettySslListenerConfigurer(Configuration configParam, 
                                    SSLServerPolicy sslPolicyParam, 
                                    SslListener secureListenerParam) {
       
        this.config = configParam;
        this.sslPolicy = sslPolicyParam;
        this.secureListener = secureListenerParam; 
    }
    
    public void configure() {
        setupSecurityConfigurer();
        setupKeystore();
        setupKeystoreType();
        setupKeystorePassword();
        setupKeyPassword();
        setupWantClientAuthentication();
        setupRequireClientAuthentication();
        setupKeystoreAlgorithm();
        setupCiphersuites();
        setupTrustStore();
        setupTrustStoreType();
        setupSecureSocketProtocol();
        setupTrustStoreAlgorithm();
        setupSessionCaching();
        setupSessionCacheKey();
        setupMaxChainLength();
        setupCertValidator();
        
    }
    
    public boolean setupKeystore() {
        String keyStoreLocation = null;
        if (sslPolicy.isSetKeystore()) {
            keyStoreLocation = sslPolicy.getKeystore();
            secureListener.setKeystore(keyStoreLocation);
            LogUtils.log(LOG, Level.INFO, "KEY_STORE_SET", new Object[] {keyStoreLocation});
            return true;           
        }
        keyStoreLocation = System.getProperty("javax.net.ssl.keyStore");
        if (keyStoreLocation != null) {
            LogUtils.log(LOG, Level.INFO, "KEY_STORE_SET", new Object[] {keyStoreLocation});
            secureListener.setKeystore(keyStoreLocation);
            return true;
        }

        keyStoreLocation = System.getProperty("user.home") + "/.keystore"; 
        secureListener.setKeystore(keyStoreLocation);
        LogUtils.log(LOG, Level.INFO, "KEY_STORE_NOT_SET", new Object[] {keyStoreLocation});
        return true;

    }
    
    public boolean setupKeystoreType() {
        
        if (!sslPolicy.isSetKeystoreType()) {
            LogUtils.log(LOG, Level.INFO, "KEY_STORE_TYPE_NOT_SET", new Object[] {DEFAUL_KEYSTORE_TYPE});
            //Can default to JKs so return true
            secureListener.setKeystoreType(DEFAUL_KEYSTORE_TYPE);
            return true;
        }
        String keyStoreType = sslPolicy.getKeystoreType();
        LogUtils.log(LOG, Level.INFO, "KEY_STORE_TYPE_SET", new Object[] {keyStoreType});
        secureListener.setKeystoreType(keyStoreType);
        return true;
    }  
    
    public boolean setupKeystorePassword() {
        String keyStorePassword = null;
        if (sslPolicy.isSetKeystorePassword()) {
            keyStorePassword = sslPolicy.getKeystorePassword();
            secureListener.setPassword(keyStorePassword);
            return true;           
        }
        keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
        if (keyStorePassword != null) {
            secureListener.setPassword(keyStorePassword);
            return true;
        }
        LogUtils.log(LOG, Level.SEVERE, "KEY_STORE_PASSWORD_NOT_SET");
        return false;

    }
    
    public void setupKeystoreAlgorithm() {
        String keyManagerFactoryAlgorithm  = null;
        if (sslPolicy.isSetKeystoreAlgorithm()) {
            keyManagerFactoryAlgorithm = sslPolicy.getKeystoreAlgorithm(); 
            secureListener.setAlgorithm(keyManagerFactoryAlgorithm);
            LogUtils.log(LOG, Level.INFO, 
                         "KEY_STORE_ALGORITHM_SET", 
                         new Object[] {keyManagerFactoryAlgorithm});
        }
        keyManagerFactoryAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        LogUtils.log(LOG, Level.INFO, 
                     "KEY_STORE_ALGORITHM_NOT_SET", 
                     new Object[] {keyManagerFactoryAlgorithm});
    } 
    
    public void setupTrustStoreAlgorithm() {
        if (sslPolicy.isSetTrustStoreAlgorithm()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_SERVER_POLICY_DATA", 
                         new Object[]{"TrustStoreAlgorithm"});
        }
    } 
    
    public boolean setupKeyPassword() {
        String keyPassword = null;
        if (sslPolicy.isSetKeyPassword()) {
            keyPassword = sslPolicy.getKeyPassword();
            secureListener.setKeyPassword(keyPassword);
            return true;
        }
        keyPassword = System.getProperty("javax.net.ssl.keyStorePassword");
        if (keyPassword == null) {
            LogUtils.log(LOG, Level.INFO, "KEY_PASSWORD_NOT_SET");
        }
        secureListener.setKeyPassword(keyPassword);
        return true;
    }
    
    public boolean setupRequireClientAuthentication() {
        if (!sslPolicy.isSetRequireClientAuthentication()) {
            LogUtils.log(LOG, Level.WARNING, "REQUIRE_CLIENT_AUTHENTICATION_NOT_SET");
            secureListener.setNeedClientAuth(DEFAULT_REQUIRE_CLIENT_AUTHENTICATION);
            return true;
        }
        Boolean holder = sslPolicy.isRequireClientAuthentication();
        boolean setRequireClientAuthentication = holder.booleanValue();
        LogUtils.log(LOG, Level.INFO, "REQUIRE_CLIENT_AUTHENTICATION_SET", 
                     new Object[]{setRequireClientAuthentication});
        secureListener.setNeedClientAuth(setRequireClientAuthentication);
        return true;
    }
    
    public boolean setupWantClientAuthentication() {
        if (!sslPolicy.isSetWantClientAuthentication()) {
            LogUtils.log(LOG, Level.WARNING, "WANT_CLIENT_AUTHENTICATION_NOT_SET");
            secureListener.setWantClientAuth(DEFAULT_WANT_CLIENT_AUTHENTICATION);            
            return true;
        }
         
        Boolean holder = sslPolicy.isWantClientAuthentication();
        boolean setWantClientAuthentication = holder.booleanValue();
        LogUtils.log(LOG, Level.INFO, "WANT_CLIENT_AUTHENTICATION_SET", 
                     new Object[]{setWantClientAuthentication});
        secureListener.setWantClientAuth(setWantClientAuthentication);
        return true;
    }    
    
    public boolean setupCiphersuites() {
        if (sslPolicy.isSetCiphersuites()) {
            
            List<String> cipherSuites = sslPolicy.getCiphersuites();
            int numCipherSuites = cipherSuites.size();
            String[] ciphs = new String[numCipherSuites];
            String ciphsStr = null;
            for (int i = 0; i < numCipherSuites; i++) {
                ciphs[i] = cipherSuites.get(i);
                if (ciphsStr == null) {
                    ciphsStr = ciphs[i];
                } else {
                    ciphsStr += ", " + ciphs[i];
                }
                
            }
            LogUtils.log(LOG, Level.INFO, "CIPHERSUITE_SET", new Object[]{ciphsStr});
            secureListener.setCipherSuites(ciphs);
        }
        LogUtils.log(LOG, Level.INFO, "CIPHERSUITE_NOT_SET");
        return true;
    }         
    
    public boolean setupTrustStore() {
        String trustStore = null;
        if (sslPolicy.isSetTrustStore()) {
            trustStore = sslPolicy.getTrustStore();
            LogUtils.log(LOG, Level.INFO, "TRUST_STORE_SET", 
                             new Object[]{trustStore});
        }
        if (trustStore == null) {
            trustStore = System.getProperty("javax.net.ssl.trustStore");
        }
        if (trustStore == null) {
            
            trustStore = System.getProperty("java.home") + "/lib/security/cacerts";
            LogUtils.log(LOG, Level.INFO, "TRUST_STORE_NOT_SET", new Object[]{trustStore});
        } 

        System.setProperty("javax.net.ssl.trustStore", trustStore);
        return true;
    }    
    
    public boolean setupTrustStoreType() {
        if (sslPolicy.isSetTrustStoreType()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_SERVER_POLICY_DATA", 
                         new Object[]{"TrustStoreType"});
            return true;
        }
        return true;
    }
    
    public void setupSecureSocketProtocol() {
        String secureSocketProtocol = null;
        if (!sslPolicy.isSetSecureSocketProtocol()) {
            LogUtils.log(LOG, Level.INFO, "SECURE_SOCKET_PROTOCOL_NOT_SET");
            secureSocketProtocol = DEFAULT_SECURE_SOCKET_PROTOCOL;
            return;
        }
        secureSocketProtocol = sslPolicy.getSecureSocketProtocol();
        secureListener.setProtocol(secureSocketProtocol);
        LogUtils.log(LOG, Level.INFO, "SECURE_SOCKET_PROTOCOL_SET", new Object[] {secureSocketProtocol});
    } 
    
    public boolean setupSessionCaching() {
        if (sslPolicy.isSetSessionCaching()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_SERVER_POLICY_DATA", 
                         new Object[]{"SessionCaching"});
        }
        return true;
    }  
    
    public boolean setupSessionCacheKey() {
        if (sslPolicy.isSetSessionCacheKey()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_SERVER_POLICY_DATA", 
                         new Object[]{"SessionCacheKey"});
        }
        return true;
    }  
    
    public boolean setupMaxChainLength() {
        if (sslPolicy.isSetMaxChainLength()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_SERVER_POLICY_DATA", 
                         new Object[]{"MaxChainLength"});
        }
        return true;
    }  
    
    public boolean setupCertValidator() {
        if (sslPolicy.isSetCertValidator()) {
            LogUtils.log(LOG, Level.WARNING, "UNSUPPORTED_SSL_SERVER_POLICY_DATA", 
                         new Object[]{"CertValidator"});
        }
        return true;
    }  
    
    public void setupSecurityConfigurer() {
        String systemProperty = "celtix.security.configurer.celtix."
                                + config.getId();
        String securityConfigurerName = System.getProperty(systemProperty);
        if ((securityConfigurerName == null) 
            || (securityConfigurerName.equals(""))) {
            return;
        }
        LogUtils.log(LOG, Level.WARNING, "UNOFFICIAL_SECURITY_CONFIGURER");
        try {
            Class clazz = Class.forName(securityConfigurerName);
            Method configure = clazz.getDeclaredMethod("configure", SSLServerPolicy.class);
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
    
    /* 
     * For development only
     */
    protected  boolean testAllDataHasSetupMethod() {
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
                    return false;
                }
                
            }
        }
        return true;
    }
    
    protected SslListener getSslListener() {
        return secureListener;
    }
    
    protected void addLogHandler(Handler handler) {
        LOG.addHandler(handler);
    }
}
