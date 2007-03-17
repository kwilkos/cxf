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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.transport.http.JettyConnectorFactory;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.security.SslSocketConnector;


public final class JettySslConnectorFactory implements JettyConnectorFactory {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LogUtils.getL7dLogger(JettySslConnectorFactory.class);    
    
    private static final String[] UNSUPPORTED =
    {"SessionCaching", "SessionCacheKey", "MaxChainLength",
     "CertValidator", "TrustStoreAlgorithm", "TrustStoreType"};

    private static final String[] DERIVATIVE = {"CiphersuiteFilters"};
    
    SSLServerPolicy sslPolicy;
        
    /**
     * Constructor.
     * 
     * @param policy the applicable SSLServerPolicy (guaranteed non-null)
     */
    public JettySslConnectorFactory(SSLServerPolicy policy) {
        this.sslPolicy = policy;
    }    
    
    
    /**
     * Create a SSL Connector.
     * 
     * @param p the listen port
     */
    public AbstractConnector createConnector(int port) {
        SslSocketConnector secureConnector = new SslSocketConnector();
        secureConnector.setPort(port);
        decorate(secureConnector);
        return secureConnector;
    }
    
    /**
     * Decorate listener with applicable SSL settings.
     * 
     * @param listener the secure listener
     */
    public void decorate(SslSocketConnector secureListener) {
        String keyStoreLocation =
            SSLUtils.getKeystore(sslPolicy.getKeystore(), LOG);
        secureListener.setKeystore(keyStoreLocation);
        String keyStoreType =
            SSLUtils.getKeystoreType(sslPolicy.getKeystoreType(), LOG);
        secureListener.setKeystoreType(keyStoreType);
        String keyStorePassword =
            SSLUtils.getKeystorePassword(sslPolicy.getKeystorePassword(), LOG);
        secureListener.setPassword(keyStorePassword);
        String keyPassword =
            SSLUtils.getKeyPassword(sslPolicy.getKeyPassword(), LOG);
        secureListener.setKeyPassword(keyPassword);
        String keyStoreMgrFactoryAlgorithm =
            SSLUtils.getKeystoreAlgorithm(sslPolicy.getKeystoreAlgorithm(),
                                          LOG);
        secureListener.setSslKeyManagerFactoryAlgorithm(keyStoreMgrFactoryAlgorithm);
        
        System.setProperty("javax.net.ssl.trustStore",
                           SSLUtils.getTrustStore(sslPolicy.getTrustStore(),
                                                  LOG));
        String secureSocketProtocol =
            SSLUtils.getSecureSocketProtocol(sslPolicy.getSecureSocketProtocol(),
                                             LOG);
        secureListener.setProtocol(secureSocketProtocol);
        //need to Check it
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
        
        try {
            SSLContext ctx = SSLUtils.getSSLContext(
                secureSocketProtocol,
                SSLUtils.getKeyStoreManagers(keyStoreLocation,
                                             keyStoreType,
                                             keyStorePassword,
                                             keyPassword,
                                             keyStoreMgrFactoryAlgorithm,
                                             secureSocketProtocol,
                                             LOG),
                null);
            secureListener.setExcludeCipherSuites(
                SSLUtils.getCiphersuites(sslPolicy.getCiphersuites(),
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
