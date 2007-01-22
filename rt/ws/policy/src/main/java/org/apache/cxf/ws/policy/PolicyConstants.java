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

package org.apache.cxf.ws.policy;

import javax.xml.namespace.QName;

/**
 * Encapsulation of version-specific WS-Policy constants.
 */
public final class PolicyConstants {
    
    private static final String NAMESPACE_URI = 
        "http://schemas.xmlsoap.org/ws/2004/09/policy";
    
    private static final String POLICY_ELEM_NAME = "Policy";
    
    private static final String POLICYREF_ELEM_NAME = "PolicyReference";
    
    private static final QName POLICY_ELEM_QNAME = 
        new QName(NAMESPACE_URI, POLICY_ELEM_NAME);
    
    private static final QName POLICYREF_QNAME = 
        new QName(NAMESPACE_URI, POLICYREF_ELEM_NAME);
    
    
    /**
     * Prevents instantiation.
     */
    
    private PolicyConstants() {
    }
    
    /**
     * @return namespace defined by the WS-Policy schema
     */
    public static String getNamespaceURI() {
        return NAMESPACE_URI;
    }
    
    /**
     * @return the QName of the Policy element
     */
    public static QName getPolicyQName() {
        return POLICY_ELEM_QNAME;
    }
    
    /**
     * @return the QName of the PolicyReference element
     */
    public static QName getPolicyReferenceQName() {
        return POLICYREF_QNAME;
    }
    
}
