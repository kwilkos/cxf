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
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.transport.http.JettyConnectorFactory;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSelectChannelConnector;


public final class JettySslConnectorFactory implements JettyConnectorFactory {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LogUtils.getL7dLogger(JettySslConnectorFactory.class);    
    
    private static final String[] UNSUPPORTED =
    {"SessionCaching", "SessionCacheKey", "MaxChainLength",
     "CertValidator", "TrustStoreAlgorithm", "TrustStoreType"};
    
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
    public SelectChannelConnector createConnector(int port) {
        SslSelectChannelConnector secureConnector = new SslSelectChannelConnector();
        secureConnector.setPort(port);
        decorate(secureConnector);
        return secureConnector;
    }
    
    /**
     * Decorate listener with applicable SSL settings.
     * 
     * @param listener the secure listener
     */
    public void decorate(SslSelectChannelConnector secureConnector) {
        secureConnector.setKeystore(
            SSLUtils.getKeystore(sslPolicy.getKeystore(), LOG));
        secureConnector.setKeystoreType(
            SSLUtils.getKeystoreType(sslPolicy.getKeystoreType(), LOG));
        secureConnector.setPassword(
            SSLUtils.getKeystorePassword(sslPolicy.getKeystorePassword(),
                                         LOG));
        secureConnector.setKeyPassword(
            SSLUtils.getKeyPassword(sslPolicy.getKeyPassword(), LOG));
        secureConnector.setAlgorithm(
            SSLUtils.getKeystoreAlgorithm(sslPolicy.getKeystoreAlgorithm(),
                                          LOG));
        secureConnector.setCipherSuites(
            SSLUtils.getCiphersuites(sslPolicy.getCiphersuites(), LOG));
        System.setProperty("javax.net.ssl.trustStore",
                           SSLUtils.getTrustStore(sslPolicy.getTrustStore(),
                                                  LOG));
        secureConnector.setProtocol(
            SSLUtils.getSecureSocketProtocol(sslPolicy.getSecureSocketProtocol(),
                                             LOG));
        /*secureConnector.setWantClientAuth(
            SSLUtils.getWantClientAuthentication(
                                   sslPolicy.isSetWantClientAuthentication(),
                                   sslPolicy.isWantClientAuthentication(),
                                   LOG));*/
        secureConnector.setNeedClientAuth(
            SSLUtils.getRequireClientAuthentication(
                                sslPolicy.isSetRequireClientAuthentication(),
                                sslPolicy.isRequireClientAuthentication(),
                                LOG));
        
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

   
}
