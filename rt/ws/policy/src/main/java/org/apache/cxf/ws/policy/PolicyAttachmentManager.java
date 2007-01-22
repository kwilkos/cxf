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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.wsdl.extensions.UnknownExtensibilityElement;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.service.model.AbstractDescriptionElement;
import org.apache.cxf.service.model.DescriptionInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyReference;


/**
 * PolicyAttachmentManager provides methods to retrieve element policies and
 * calculate effective policies based on the policy subject's scope.
 * Currently supports WSDL 1.1 only.
 * To transparently support both WSDL 2.0 and WSDL 1.1, convert this class
 * to an interface and provide implementations for WSDL 1.1, WSDL 2.0.
 * The WSDL version can be retrieved from the service model.
 * 
 */
public class PolicyAttachmentManager {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(PolicyAttachmentManager.class);
    
    private PolicyBuilder builder;
    private Map<DescriptionInfo, Map<String, Policy>> resolved = 
        new HashMap<DescriptionInfo, Map<String, Policy>>();
    
    void setBuilder(PolicyBuilder b) {
        builder = b;
    }
    
    Policy getEffectivePolicy(ServiceInfo si) {
        return getSubjectPolicy(si);
    }
    
    Policy getSubjectPolicy(ServiceInfo si) {
        return getElementPolicy(si);
    }
      
    Policy getEffectivePolicy(EndpointInfo ei) {
        Policy p = getSubjectPolicy(ei.getService());
        return p.merge(getSubjectPolicy(ei));
    }
    
    Policy getSubjectPolicy(EndpointInfo ei) {
        Policy p = getElementPolicy(ei);
        p = p.merge(getElementPolicy(ei.getInterface()));
        p = p.merge(getElementPolicy(ei.getBinding()));
        return p;
    }

    Policy getElementPolicy(AbstractDescriptionElement aph) {
        List<UnknownExtensibilityElement> extensions = 
            aph.getExtensors(UnknownExtensibilityElement.class);
        
        Policy elementPolicy = new Policy();
        
        if (null != extensions) {
            
            for (UnknownExtensibilityElement e : extensions) {
                Policy p = null;
                if (PolicyConstants.getPolicyQName().equals(e.getElementType())) {
                    p = builder.getPolicy(e.getElement());

                } else if (PolicyConstants.getPolicyReferenceQName().equals(e.getElementType())) {
                    PolicyReference ref = builder.getPolicyReference(e.getElement());
                    if (null != ref) {
                        p = resolveReference(ref, aph);
                    }
                }
                if (null != p) { 
                    elementPolicy = elementPolicy.merge(p);
                }
            }
        }

        return elementPolicy;
    }
    
    
    Policy resolveReference(PolicyReference ref, AbstractDescriptionElement aph) {
        String uri = ref.getURI();
        if (isExternal(ref)) {
            return resolveExternal(ref);
        }
        
        DescriptionInfo description = aph.getDescription();  
        return resolveLocal(uri, description);
    }

    Policy resolveLocal(String uri, DescriptionInfo description) {
        Policy p = null;
        Map<String, Policy> policyMap = resolved.get(description);
        if (null == policyMap) {
            policyMap = new HashMap<String, Policy>();
            resolved.put(description, policyMap);
        } else {
            p = policyMap.get(uri);
            if (null != p) {
                return p;
            }
        }
        
        List<UnknownExtensibilityElement> extensions = 
            description.getExtensors(UnknownExtensibilityElement.class);
        for (UnknownExtensibilityElement e : extensions) {
            if (PolicyConstants.getPolicyQName().equals(e.getElementType())) {
                p = builder.getPolicy(e.getElement());
                if (uri.equals(p.getId())) {
                    policyMap.put(uri, p);
                    return p;
                }                
            }
        }
        return null;
    }
    
    Policy resolveExternal(PolicyReference ref) {
        throw new PolicyException(new Message("REMOTE_POLICY_RESOLUTION_NOT_SUPPORTED_EXC", BUNDLE));
    }
    
    private boolean isExternal(PolicyReference ref) {
        return !ref.getURI().startsWith("#");
    }

}
