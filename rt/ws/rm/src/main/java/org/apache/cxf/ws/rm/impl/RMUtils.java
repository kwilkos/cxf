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

package org.apache.cxf.ws.rm.impl;

import org.apache.cxf.ws.addressing.AddressingConstants;
import org.apache.cxf.ws.addressing.AddressingConstantsImpl;
import org.apache.cxf.ws.addressing.VersionTransformer;
import org.apache.cxf.ws.addressing.v200408.AttributedURI;
import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;

import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.PolicyConstantsImpl;
import org.apache.cxf.ws.rm.RMConstants;
// import org.apache.cxf.ws.rm.persistence.PersistenceUtils;

public final class RMUtils {
   
    private static final org.apache.cxf.ws.addressing.v200408.ObjectFactory WSA_FACTORY;
    private static final org.apache.cxf.ws.rm.ObjectFactory WSRM_FACTORY;
    private static final RMConstants WSRM_CONSTANTS;
    private static final AddressingConstants WSA_CONSTANTS; 
    private static final PolicyConstants WSP_CONSTANTS;
    // private static final PersistenceUtils WSRM_PERSISTENCE_UTILS;
    
    static {
        WSA_FACTORY = new org.apache.cxf.ws.addressing.v200408.ObjectFactory();
        WSRM_FACTORY = new org.apache.cxf.ws.rm.ObjectFactory();
        WSRM_CONSTANTS = new RMConstantsImpl();        
        WSA_CONSTANTS = new AddressingConstantsImpl();
        WSP_CONSTANTS = new PolicyConstantsImpl();
        // WSRM_PERSISTENCE_UTILS = new PersistenceUtils();       
    }
    
    protected RMUtils() {        
    }
    
    public static org.apache.cxf.ws.addressing.v200408.ObjectFactory getWSAFactory() {
        return WSA_FACTORY;
    }
    
    public static org.apache.cxf.ws.rm.ObjectFactory getWSRMFactory() {
        return WSRM_FACTORY;
    }
    
    public static RMConstants getRMConstants() {
        return WSRM_CONSTANTS;
    }
    
    public static AddressingConstants getAddressingConstants() {
        return WSA_CONSTANTS;
    }
    
    public static PolicyConstants getPolicyConstants() {
        return WSP_CONSTANTS;
    }
    
    /*
    public static PersistenceUtils getPersistenceUtils() {
        return WSRM_PERSISTENCE_UTILS;
    }
    */
    
    public static EndpointReferenceType createReference(String address) {
        EndpointReferenceType ref = 
            VersionTransformer.Names200408.WSA_OBJECT_FACTORY.createEndpointReferenceType();
        AttributedURI value =
            VersionTransformer.Names200408.WSA_OBJECT_FACTORY.createAttributedURI();
        value.setValue(address);
        ref.setAddress(value);
        return ref;
    }
}
