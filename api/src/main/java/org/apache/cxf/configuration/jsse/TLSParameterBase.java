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
package org.apache.cxf.configuration.jsse;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.apache.cxf.configuration.security.FiltersType;
/**
 * This class is the base class for TLS parameters that are common
 * to both client and server sides.
 */
public class TLSParameterBase {
    private KeyManager[]    keyManagers;
    private TrustManager[]  trustManagers;
    private String          provider;
    private List<String>    ciphersuites = new ArrayList<String>();
    private FiltersType     cipherSuiteFilters;
    private SecureRandom    secureRandom;
    private String          protocol;
    
    /**
     * This parameter configures the JSSE provider. If not set, it
     * uses system default.
     */
    public final void setJsseProvider(String prov) {
        provider = prov;
    }
    
    /**
     * This parameter configures to use the following KeyManagers.
     * This parameter may be set to null for system default behavior.
     */
    public final void setKeyManagers(KeyManager[] keyMgrs) {
        keyManagers = keyMgrs;
    }

    /**
     * This parameter configures to use the following TrustManagers.
     * This parameter may be set to null for system default behavior.
     */
    public final void setTrustManagers(TrustManager[] trustMgrs) {
        trustManagers = trustMgrs;
    }
    
    /**
     * This parameter sets the cipher suites list to use. If left unset
     * it uses system defaults.
     */
    public final void setCipherSuites(List<String> cs) {
        ciphersuites = cs;
    }
    
    /**
     * This parameter sets the filter to include and/or exclude the 
     * cipher suites to use from the set list or system defaults.
     */
    public final void setCipherSuitesFilter(FiltersType filters) {
        cipherSuiteFilters = filters;
    }
    
    /**
     * This sets the protocol to use. The system default is usually
     * "TLS".
     */
    public final void setSecureSocketProtocol(String proto) {
        protocol = proto;
    }

    /**
     * This sets the secure random provider and alogorithm. If left unset or set
     * to null, it uses the system default.
     */
    public final void setSecureRandom(SecureRandom random) {
        secureRandom = random;
    }

    /**
     * This sets the secure random alogorithm. If left unset or set
     * to null, it uses the system default.
     */
    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

    /**
     * This sets the protocol to use. The system default is usually
     * "TLS".
     */
    public String getSecureSocketProtocol() {
        return protocol;
    }

    /**
     * This parameter configures the JSSE provider. If not set, it
     * uses system default.
     */
    public String getJsseProvider() {
        return provider;
    }

    /**
     * This parameter configures to use the following KeyManagers.
     * This parameter may be set to null for system default behavior.
     */
    public KeyManager[] getKeyManagers() {
        return keyManagers;
    }

    /**
     * This parameter configures to use the following TrustManagers.
     * This parameter may be set to null for system default behavior.
     */
    public TrustManager[] getTrustManagers() {
        return trustManagers;
    }

    /**
     * This parameter sets the cipher suites list to use. If left unset
     * it uses system defaults.
     */
    public List<String> getCipherSuites() {
        if (ciphersuites == null) {
            ciphersuites = new ArrayList<String>();
        }
        return ciphersuites;
    }

    /**
     * This parameter sets the filter to include and/or exclude the 
     * cipher suites to use from the set list or system defaults.
     */
    public FiltersType getCipherSuitesFilter() {
        return cipherSuiteFilters;
    }
}
