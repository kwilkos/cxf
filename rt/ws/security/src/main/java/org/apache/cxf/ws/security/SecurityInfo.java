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
package org.apache.cxf.ws.security;

import java.security.KeyStore;

public class SecurityInfo {
    private boolean doUsernameToken;
    private boolean doEncryption;
    private boolean doSignature;
    private boolean doTimestamp;

    private KeyStore keyStore;
    private KeyStore trustStore;
    private KeyStore symmetricStore;
    
    public boolean isDoEncryption() {
        return doEncryption;
    }
    public void setDoEncryption(boolean doEncryption) {
        this.doEncryption = doEncryption;
    }
    public boolean isDoSignature() {
        return doSignature;
    }
    public void setDoSignature(boolean doSignature) {
        this.doSignature = doSignature;
    }
    public boolean isDoTimestamp() {
        return doTimestamp;
    }
    public void setDoTimestamp(boolean doTimestamp) {
        this.doTimestamp = doTimestamp;
    }
    public boolean isDoUsernameToken() {
        return doUsernameToken;
    }
    public void setDoUsernameToken(boolean doUsernameToken) {
        this.doUsernameToken = doUsernameToken;
    }
    public KeyStore getKeyStore() {
        return keyStore;
    }
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }
    public KeyStore getSymmetricStore() {
        return symmetricStore;
    }
    public void setSymmetricStore(KeyStore symmetricStore) {
        this.symmetricStore = symmetricStore;
    }
    public KeyStore getTrustStore() {
        return trustStore;
    }
    public void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }
    
    
}
