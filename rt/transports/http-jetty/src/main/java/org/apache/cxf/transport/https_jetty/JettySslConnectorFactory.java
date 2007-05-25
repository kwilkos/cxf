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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.transport.http_jetty.JettyConnectorFactory;
import org.apache.cxf.transport.https.SSLUtils;
import org.mortbay.jetty.AbstractConnector;


public final class JettySslConnectorFactory implements JettyConnectorFactory {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LogUtils.getL7dLogger(JettySslConnectorFactory.class);    
    
    @Deprecated
    private static final String[] UNSUPPORTED =
    {"SessionCaching", "SessionCacheKey", "MaxChainLength",
     "CertValidator", "TrustStoreAlgorithm", "TrustStoreType"};

    private static final String[] DERIVATIVE = {"CiphersuiteFilters"};
    
    @Deprecated
    SSLServerPolicy sslPolicy;
        
    TLSServerParameters tlsServerParameters;
    
    /**
     * Constructor.
     * 
     * @param policy the applicable SSLServerPolicy (guaranteed non-null)
     */
    @Deprecated
    public JettySslConnectorFactory(SSLServerPolicy policy) {
        this.sslPolicy = policy;
    }    
    
    public JettySslConnectorFactory(TLSServerParameters params) {
        tlsServerParameters = params;
    }
    
    /**
     * Create a SSL Connector.
     * 
     * @param p the listen port
     */
    public AbstractConnector createConnector(int port) {
        if (tlsServerParameters != null) {
            CXFJettySslSocketConnector secureConnector = 
                new CXFJettySslSocketConnector();
            secureConnector.setPort(port);
            decorateCXFJettySslSocketConnector(secureConnector);
            return secureConnector;
        }
        if (sslPolicy != null) {
            //SslSocketConnector secureConnector = new SslSocketConnector();
            CXFJettySslSocketConnector secureConnector = 
                new CXFJettySslSocketConnector();
            secureConnector.setPort(port);
            decorate(secureConnector);
            return secureConnector;
        }
        assert false;
        return null;
    }
    
    /**
     * This method sets the security properties for the CXF extension
     * of the JettySslConnector.
     */
    private void decorateCXFJettySslSocketConnector(
            CXFJettySslSocketConnector con
    ) {
        con.setKeyManagers(tlsServerParameters.getKeyManagers());
        con.setTrustManagers(tlsServerParameters.getTrustManagers());
        con.setSecureRandom(tlsServerParameters.getSecureRandom());
        con.setClientAuthentication(
                tlsServerParameters.getClientAuthentication());
        con.setProtocol(tlsServerParameters.getSecureSocketProtocol());
        con.setProvider(tlsServerParameters.getJsseProvider());
        con.setCipherSuites(tlsServerParameters.getCipherSuites());
        con.setCipherSuitesFilter(tlsServerParameters.getCipherSuitesFilter());
    }
    
    /**
     * Decorate listener with applicable SSL settings.
     * This method will be deprecated after old SSL configuration is gone.
     * This method has been modified to use the CXF extension 
     * to the JettySslSocketConnector so that we may upgrade to 
     * Jetty 6.1.3.
     * 
     * @param listener the secure listener
     */
    @Deprecated
    public void decorate(CXFJettySslSocketConnector secureListener) {
        
        // This has been modified to work with Jetty 6.1.3 and our
        // extended JettySslSocketConnector, because they have a bug
        // in which processing the TrustStore throws a null pointer
        // exception if the trust store doesn't have a password set.
        
        String keyStoreLocation =
            SSLUtils.getKeystore(sslPolicy.getKeystore(), LOG);
        //secureListener.setKeystore(keyStoreLocation);
        String keyStoreType =
            SSLUtils.getKeystoreType(sslPolicy.getKeystoreType(), LOG);
        //secureListener.setKeystoreType(keyStoreType);
        String keyStorePassword =
            SSLUtils.getKeystorePassword(sslPolicy.getKeystorePassword(), LOG);
        //secureListener.setPassword(keyStorePassword);
        String keyPassword =
            SSLUtils.getKeyPassword(sslPolicy.getKeyPassword(), LOG);
        //secureListener.setKeyPassword(keyPassword);
        String keyStoreMgrFactoryAlgorithm =
            SSLUtils.getKeystoreAlgorithm(sslPolicy.getKeystoreAlgorithm(),
                                          LOG);
        //secureListener.setSslKeyManagerFactoryAlgorithm(keyStoreMgrFactoryAlgorithm);

        String secureSocketProtocol =
            SSLUtils.getSecureSocketProtocol(sslPolicy.getSecureSocketProtocol(),
                                             LOG);
        secureListener.setProtocol(secureSocketProtocol);
        

        secureListener.setWantClientAuth(
            SSLUtils.getWantClientAuthentication(
                                   sslPolicy.isSetWantClientAuthentication(),
                                   sslPolicy.isWantClientAuthentication(),
                                   LOG));
        secureListener.setNeedClientAuth(
            SSLUtils.getRequireClientAuthentication(
                                sslPolicy.isSetRequireClientAuthentication(),
                                sslPolicy.isRequireClientAuthentication(),
                                LOG));
        
        String trustStoreType =
            SSLUtils.getTrustStoreType(sslPolicy.getTrustStoreType(), LOG);
        
        String trustStoreLocation = 
            SSLUtils.getTrustStore(sslPolicy.getTrustStore(), LOG);
        
        String trustStoreMgrFactoryAlgorithm =
            SSLUtils.getTrustStoreAlgorithm(
                    sslPolicy.getTrustStoreAlgorithm(), LOG);

        //System.setProperty("javax.net.ssl.trustStore",
        //                   SSLUtils.getTrustStore(sslPolicy.getTrustStore(),
        //                                          LOG));
        //need to Check it
        try {
            KeyManager[] keyManagers =
                SSLUtils.getKeyStoreManagers(keyStoreLocation,
                                     keyStoreType,
                                     keyStorePassword,
                                     keyPassword,
                                     keyStoreMgrFactoryAlgorithm,
                                     secureSocketProtocol,
                                     LOG);
            secureListener.setKeyManagers(keyManagers);
            
            // On the client side, it was strange that if you Keystore was 
            // of type PCKS12, then your TrustStore location had to point to
            // was a PEM encoded CA Certificate. However, in this code before
            // modification, it didn't seem like the TrustSTore
            // had to be a single PEM CA certificate if the Keystore was
            // of type PKCS12. So, we use false here for pkcs12 parameter.
            
            TrustManager[] trustManagers =
                SSLUtils.getTrustStoreManagers(
                        false, 
                        trustStoreType, trustStoreLocation, 
                        trustStoreMgrFactoryAlgorithm, LOG);

            secureListener.setTrustManagers(trustManagers);
            
            SSLContext ctx = SSLUtils.getSSLContext(
                    secureSocketProtocol, keyManagers, trustManagers);
                
            secureListener.setExcludeCipherSuites(
                SSLUtils.getCiphersuites(
                        sslPolicy.getCiphersuites(),
                        SSLUtils.getServerSupportedCipherSuites(ctx),
                        sslPolicy.getCiphersuiteFilters(),
                        LOG, true));
            
        } catch (Exception e) {
            LogUtils.log(LOG, Level.SEVERE, "SSL_CONTEXT_INIT_FAILURE", e);
        }

        SSLUtils.logUnSupportedPolicies(sslPolicy,
                                        false,
                                        UNSUPPORTED,
                                        LOG);
    }

    /* 
     * For development & testing only
     */
    protected void addLogHandler(Handler handler) {
        LOG.addHandler(handler);
    }
    
    protected String[] getUnSupported() {
        return UNSUPPORTED;
    }
    
    protected String[] getDerivative() {
        return DERIVATIVE;
    }
    
}
