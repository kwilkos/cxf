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
    
    public static final String NAMESPACE_XMLSOAP_200409
        = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    
    public static final String NAMESPACE_W3_200607
        = "http://www.w3.org/2006/07/ws-policy";
    
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
    
    
    private static String namespaceURI; 
    
    private static final String POLICY_ELEM_NAME = "Policy";
    
    private static final String ALL_ELEM_NAME = "All";
    
    private static final String EXACTLYONE_ELEM_NAME = "ExactlyOne";
    
    private static final String POLICYREFERENCE_ELEM_NAME = "PolicyReference";
    
    private static final String POLICYATTACHMENT_ELEM_NAME = "PolicyAttachment";
    
    private static final String APPLIESTO_ELEM_NAME = "AppliesTo";
    
    private static final String OPTIONAL_ATTR_NAME = "Optional"; 
    
    private static QName policyElemQName;
    
    private static QName allElemQName;
    
    private static QName exactlyOneElemQName;
    
    private static QName policyReferenceElemQName;
    
    private static QName policyAttachmentElemQName;
    
    private static QName appliesToElemQName;
    
    private static QName optionalAttrQName;
    
    private static final String WSU_NAMESPACE_URI = 
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    
    private static final String WSU_ID_ATTR_NAME = "Id";
    
    private static final QName WSU_ID_ATTR_QNAME =
        new QName(WSU_NAMESPACE_URI, WSU_ID_ATTR_NAME);
    
    static {
        setNamespace(NAMESPACE_W3_200607);
    }
    
    /**
     * Prevents instantiation.
     */
    
    private PolicyConstants() {
    }
    
    
    
    public static String getNamespace() {
        return namespaceURI;
    } 
    
    public static String getWSUNamespace() {
        return WSU_NAMESPACE_URI;
    }
    
    public static String getPolicyElemName() {
        return POLICY_ELEM_NAME;
    }
    
    public static String getAllElemName() {
        return ALL_ELEM_NAME;
    }
    
    public static String getExactlyOneElemName() {
        return EXACTLYONE_ELEM_NAME;
    }
    
    public static String getPolicyReferenceElemName() {
        return POLICYREFERENCE_ELEM_NAME;
    }
    
    public static String getPolicyAttachmentElemName() {
        return POLICYATTACHMENT_ELEM_NAME;
    }
    
    public static String getAppliesToElemName() {
        return APPLIESTO_ELEM_NAME;
    }
    
    public static String getOptionalAttrName() {
        return OPTIONAL_ATTR_NAME;
    }
    
    public static String getIdAttrName() {
        return WSU_ID_ATTR_NAME;
    }
    
    public static QName getPolicyElemQName() {
        return policyElemQName;
    }
    
    public static QName getAllElemQName() {
        return allElemQName;
    }
    
    public static QName getExactlyOneElemQName() {
        return exactlyOneElemQName;
    }
    
    public static QName getPolicyReferenceElemQName() {
        return policyReferenceElemQName;
    }
    
    public static QName getPolicyAttachmentElemQName() {
        return policyAttachmentElemQName;
    }
    
    public static QName getAppliesToElemQName() {
        return appliesToElemQName;
    }
    
    public static QName getOptionalAttrQName() {
        return optionalAttrQName;
    }
    
    public static QName getIdAttrQName() {
        return WSU_ID_ATTR_QNAME;
    } 
    
    
    public static void setNamespace(String uri) {
        namespaceURI = uri;
        
        // update qnames
        
        policyElemQName = new QName(namespaceURI, POLICY_ELEM_NAME);
        allElemQName = new QName(namespaceURI, ALL_ELEM_NAME);
        exactlyOneElemQName = new QName(namespaceURI, EXACTLYONE_ELEM_NAME);
        policyReferenceElemQName = new QName(namespaceURI, POLICYREFERENCE_ELEM_NAME);
        policyAttachmentElemQName = new QName(namespaceURI, POLICYATTACHMENT_ELEM_NAME);
        appliesToElemQName = new QName(namespaceURI, APPLIESTO_ELEM_NAME);
        optionalAttrQName = new QName(namespaceURI, OPTIONAL_ATTR_NAME);
        
    }
}
