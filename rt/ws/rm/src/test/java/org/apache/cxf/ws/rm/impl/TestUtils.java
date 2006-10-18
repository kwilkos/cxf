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

import org.apache.cxf.ws.addressing.VersionTransformer;

/**
 * 
 */
public final class TestUtils {

    /**
     * Prevents construction.
     */
    private TestUtils() {
        
    }
    
    public static org.apache.cxf.ws.addressing.EndpointReferenceType getAnonymousReference() {
        return getReference(org.apache.cxf.ws.addressing.Names.WSA_ANONYMOUS_ADDRESS);
    }
    
    public static org.apache.cxf.ws.addressing.v200408.EndpointReferenceType getAnonymousReference2004() {
        return VersionTransformer.convert(getAnonymousReference());
    }
    
    public static org.apache.cxf.ws.addressing.EndpointReferenceType getNoneReference() {
        return getReference(org.apache.cxf.ws.addressing.Names.WSA_NONE_ADDRESS);
    }
    
    public static org.apache.cxf.ws.addressing.v200408.EndpointReferenceType getNoneReference2004() {
        return VersionTransformer.convert(getNoneReference());
    }
    
    public static org.apache.cxf.ws.addressing.EndpointReferenceType getReference(String address) {
        org.apache.cxf.ws.addressing.ObjectFactory factory = 
            new org.apache.cxf.ws.addressing.ObjectFactory();
        org.apache.cxf.ws.addressing.EndpointReferenceType epr = factory.createEndpointReferenceType();
        org.apache.cxf.ws.addressing.AttributedURIType uri = factory.createAttributedURIType();
        uri.setValue(address);
        epr.setAddress(uri);        
        return epr;        
    }
    
    public static org.apache.cxf.ws.addressing.v200408.EndpointReferenceType 
    getReference2004(String address) {
        org.apache.cxf.ws.addressing.v200408.ObjectFactory factory = 
            new org.apache.cxf.ws.addressing.v200408.ObjectFactory();
        org.apache.cxf.ws.addressing.v200408.EndpointReferenceType epr = 
            factory.createEndpointReferenceType();
        org.apache.cxf.ws.addressing.v200408.AttributedURI uri = factory.createAttributedURI();
        uri.setValue(address);
        epr.setAddress(uri);
        return epr;
    } 
}
