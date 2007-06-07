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
package org.apache.cxf.configuration.jsse.spring;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.configuration.security.KeyManagersType;
import org.apache.cxf.configuration.security.KeyStoreType;
import org.apache.cxf.configuration.security.SecureRandomParameters;
import org.apache.cxf.configuration.security.TrustManagersType;

/**
 * This class provides some functionality to convert the JAXB 
 * generated types in the security.xsd to the items needed
 * to programatically configure the HTTPConduit and HTTPDesination
 * with TLSClientParameters and TLSServerParameters respectively.
 */
public final class TLSParameterJaxBUtils {

    private TLSParameterJaxBUtils() {
        // empty
    }
    /**
     * This method converts the JAXB generated type into a SecureRandom.
     */
    public static SecureRandom getSecureRandom(
            SecureRandomParameters secureRandomParams
    ) throws GeneralSecurityException {

        SecureRandom secureRandom = null;
        if (secureRandomParams != null) {
            String secureRandomAlg = 
                secureRandomParams.getAlgorithm();
            String randomProvider =
                secureRandomParams.getProvider();
            if (randomProvider != null) {
                secureRandom = secureRandomAlg != null
                               ? SecureRandom.getInstance(
                                       secureRandomAlg, 
                                       randomProvider)
                               : null;
            } else {
                secureRandom = secureRandomAlg != null
                               ? SecureRandom.getInstance(
                                       secureRandomAlg)
                               : null;
            }
        }
        return secureRandom;
    }
    /**
     * This method converts a JAXB generated KeyStoreType into a KeyStore.
     */
    public static KeyStore getKeyStore(KeyStoreType kst)
        throws GeneralSecurityException,
               IOException {
        
        if (kst == null) {
            return null;
        }
        
        String type = kst.isSetType()
                    ? kst.getType()
                    : KeyStore.getDefaultType();
                    
        char[] password = kst.isSetPassword()
                    ? kst.getPassword().toCharArray()
                    : null;

        KeyStore keyStore = !kst.isSetProvider()
                    ? KeyStore.getInstance(type)
                    : KeyStore.getInstance(type, kst.getProvider());
        
        if (kst.isSetFile()) {
            keyStore.load(new FileInputStream(kst.getFile()), password);
        }
        if (kst.isSetResource()) {
            keyStore.load(kst.getClass().getClassLoader().getResourceAsStream(kst.getResource()), password);
        }
        if (kst.isSetUrl()) {
            keyStore.load(new URL(kst.getUrl()).openStream(), password);
        }
        return keyStore;
    }

    /**
     * This method converts the JAXB KeyManagersType into a list of 
     * JSSE KeyManagers.
     */
    public static KeyManager[] getKeyManagers(KeyManagersType kmc) 
        throws GeneralSecurityException,
               IOException {
        
        KeyStore keyStore = getKeyStore(kmc.getKeyStore());
        
        if (keyStore == null) {
            return null;
        }
        
        String alg = kmc.isSetFactoryAlgorithm() 
                     ? kmc.getFactoryAlgorithm()
                     : KeyManagerFactory.getDefaultAlgorithm();
        
        char[] keyPass = kmc.isSetKeyPassword()
                     ? kmc.getKeyPassword().toCharArray()
                     : null;
                     
        KeyManagerFactory fac = 
                     kmc.isSetProvider()
                     ? KeyManagerFactory.getInstance(alg, kmc.getProvider())
                     : KeyManagerFactory.getInstance(alg);
                     
        fac.init(keyStore, keyPass);
        
        return fac.getKeyManagers();
    }

    /**
     * This method converts the JAXB KeyManagersType into a list of 
     * JSSE TrustManagers.
     */
    public static TrustManager[] getTrustManagers(TrustManagersType kmc) 
        throws GeneralSecurityException,
               IOException {
        
        KeyStore keyStore = getKeyStore(kmc.getKeyStore());
        
        if (keyStore == null) {
            return null;
        }
        
        String alg = kmc.isSetFactoryAlgorithm()
                     ? kmc.getFactoryAlgorithm()
                     : KeyManagerFactory.getDefaultAlgorithm();
        
        TrustManagerFactory fac = 
                     kmc.isSetProvider()
                     ? TrustManagerFactory.getInstance(alg, kmc.getProvider())
                     : TrustManagerFactory.getInstance(alg);
                     
        fac.init(keyStore);
        
        return fac.getTrustManagers();
    }
}
