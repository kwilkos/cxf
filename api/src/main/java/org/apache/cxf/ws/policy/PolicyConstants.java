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
    
    public static final String NAMESPACE_URI = 
        "http://schemas.xmlsoap.org/ws/2004/09/policy";
    
    public static final String POLICY_ELEM_NAME = "Policy";
    
    public static final String POLICYREF_ELEM_NAME = "PolicyReference";
    
    public static final String OPTIONAL_ATTR_NAME = "Optional"; 
    
    public static final QName POLICY_ELEM_QNAME = 
        new QName(NAMESPACE_URI, POLICY_ELEM_NAME);
    
    public static final QName POLICYREF_ELEM_QNAME = 
        new QName(NAMESPACE_URI, POLICYREF_ELEM_NAME);
    
    public static final QName OPTIONAL_ATTR_QNAME = 
        new QName(NAMESPACE_URI, OPTIONAL_ATTR_NAME);
    
    public static final String CLIENT_POLICY_OUT_INTERCEPTOR_ID
        = "org.apache.cxf.ws.policy.ClientPolicyOutInterceptor";
    public static final String CLIENT_POLICY_IN_INTERCEPTOR_ID
        = "org.apache.cxf.ws.policy.ClientPolicyInInterceptor";
    public static final String CLIENT_POLICY_IN_FAULT_INTERCEPTOR_ID
        = "org.apache.cxf.ws.policy.ClientPolicyInFaultInterceptor";
    
    public static final String SERVER_POLICY_IN_INTERCEPTOR_ID
        = "org.apache.cxf.ws.policy.ServerPolicyInInterceptor";
    public static final String SERVER_POLICY_OUT_INTERCEPTOR_ID
        = "org.apache.cxf.ws.policy.ServerPolicyOutInterceptor";
    public static final String SERVER_POLICY_OUT_FAULT_INTERCEPTOR_ID
        = "org.apache.cxf.ws.policy.ServerPolicyOutFaultInterceptor";
    
    /**
     * Prevents instantiation.
     */
    
    private PolicyConstants() {
    }
    
}
