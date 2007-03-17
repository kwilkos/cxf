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

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.security.SSLClientPolicy;
import org.apache.cxf.transport.http.URLConnectionFactory;


public final class HttpsURLConnectionFactory implements URLConnectionFactory {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG =
        LogUtils.getL7dLogger(HttpsURLConnectionFactory.class);
    
    private static final String[] UNSUPPORTED =
    {"SessionCaching", "SessionCacheKey", "MaxChainLength",
     "CertValidator", "ProxyHost", "ProxyPort"};
    
    private static final String[] DERIVATIVE = {"CiphersuiteFilters"};
    
    SSLClientPolicy sslPolicy;
    
    /**
     * Constructor.
     * 
     * @param policy the applicable SSLClientPolicy (guaranteed non-null)
     */
    public HttpsURLConnectionFactory(SSLClientPolicy policy) {
        sslPolicy = policy;
    }
    
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
    
    /**
     * Decorate connection with applicable SSL settings.
     * 
     * @param secureConnection the secure connection
     */
    protected void decorate(HttpsURLConnection secureConnection) {
        String keyStoreLocation =
            SSLUtils.getKeystore(sslPolicy.getKeystore(), LOG);
        String keyStoreType =
            SSLUtils.getKeystoreType(sslPolicy.getKeystoreType(), LOG);
        String keyStorePassword =
            SSLUtils.getKeystorePassword(sslPolicy.getKeystorePassword(), LOG);
        String keyPassword =
            SSLUtils.getKeyPassword(sslPolicy.getKeyPassword(), LOG);
        String keyStoreMgrFactoryAlgorithm =
            SSLUtils.getKeystoreAlgorithm(sslPolicy.getKeystoreAlgorithm(),
                                          LOG);
        String trustStoreMgrFactoryAlgorithm =
            SSLUtils.getTrustStoreAlgorithm(sslPolicy.getTrustStoreAlgorithm(),
                                            LOG);
        String trustStoreLocation =
            SSLUtils.getTrustStore(sslPolicy.getTrustStore(), LOG);
        String trustStoreType =
            SSLUtils.getTrustStoreType(sslPolicy.getTrustStoreType(), LOG);
        String secureSocketProtocol =
            SSLUtils.getSecureSocketProtocol(sslPolicy.getSecureSocketProtocol(),
                                             LOG);
        
        try {
            boolean pkcs12 =
                keyStoreType.equalsIgnoreCase(SSLUtils.PKCS12_TYPE);
            SSLContext ctx = SSLUtils.getSSLContext(
                secureSocketProtocol,
                SSLUtils.getKeyStoreManagers(keyStoreLocation,
                                             keyStoreType,
                                             keyStorePassword,
                                             keyPassword,
                                             keyStoreMgrFactoryAlgorithm,
                                             secureSocketProtocol,
                                             LOG),
                SSLUtils.getTrustStoreManagers(pkcs12,
                                               trustStoreType,
                                               trustStoreLocation,
                                               trustStoreMgrFactoryAlgorithm,
                                               LOG));
            
            String[] cipherSuites =
                SSLUtils.getCiphersuites(sslPolicy.getCiphersuites(),
                                         SSLUtils.getSupportedCipherSuites(ctx),
                                         sslPolicy.getCiphersuiteFilters(),
                                         LOG, false);
            secureConnection.setSSLSocketFactory(
                new SSLSocketFactoryWrapper(ctx.getSocketFactory(),
                                            cipherSuites));
        } catch (Exception e) {
            LogUtils.log(LOG, Level.SEVERE, "SSL_CONTEXT_INIT_FAILURE", e);
        }
        
        SSLUtils.logUnSupportedPolicies(sslPolicy,
                                        true,
                                        UNSUPPORTED,
                                        LOG);
    }
    
    /*
     *  For development and testing only
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

