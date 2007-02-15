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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * 
 */
public class EndpointPolicyInfo {
    
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(EndpointPolicyInfo.class);
    
    private Policy policy;
    private List<Assertion> chosenAlternative;
    private List<Interceptor> inInterceptors;
    private List<Interceptor> inFaultInterceptors;
    
    void initialise(EndpointInfo ei, boolean isServer, PolicyEngine engine, Assertor assertor) {
        initialisePolicy(ei, engine);
        chooseAlternative(engine, assertor);
        initialiseInterceptors(ei, isServer, engine);  
    }
   
    public Policy getPolicy() {
        return policy;        
    }
    
    public List<Assertion> getChosenAlternative() {
        return chosenAlternative;
    }
    
    public List<Interceptor> getInInterceptors() {
        return inInterceptors;
    }
    
    public List<Interceptor> getInFaultInterceptors() {
        return inFaultInterceptors;
    }
      
    void initialisePolicy(EndpointInfo ei, PolicyEngine engine) {
        policy = engine.getAggregatedServicePolicy(ei.getService());
        policy = policy.merge(engine.getAggregatedEndpointPolicy(ei));
        policy = (Policy)policy.normalize(true);
    }

    void chooseAlternative(PolicyEngine engine, Assertor assertor) {
        Iterator alternatives = policy.getAlternatives();
        while (alternatives.hasNext()) {
            List<Assertion> alternative = CastUtils.cast((List)alternatives.next(), Assertion.class);
            if (engine.supportsAlternative(alternative, assertor)) {
                setChosenAlternative(alternative);
                return;
            }
        }
        throw new PolicyException(new Message("NO_ALTERNATIVE_EXC", BUNDLE));
    }

    void initialiseInterceptors(EndpointInfo ei, boolean isServer, PolicyEngine engine) {
        PolicyInterceptorProviderRegistry reg 
            = engine.getBus().getExtension(PolicyInterceptorProviderRegistry.class);
        inInterceptors = new ArrayList<Interceptor>();
        inFaultInterceptors = new ArrayList<Interceptor>();
        
        // the minimal set of interceptors required to satisfy the effective policy for
        // any inbound message 
        for (Assertion a : getChosenAlternative()) {
            if (a.isOptional()) {
                continue;
            }
            QName qn = a.getName();
            PolicyInterceptorProvider pp = reg.get(qn);
            if (null != pp) {
                inInterceptors.addAll(pp.getInInterceptors());
                inFaultInterceptors.addAll(pp.getInInterceptors());
            }
        }
        
        // add the interceptors that may be needed to support the additional assertions
        // for specific inbound (in case of a server endpoint) or outbound (in case of a client endpoint)
        // messages
        
        Set<QName> vocabulary = null;
  
        for (BindingOperationInfo boi : ei.getBinding().getOperations()) {
            Policy p = engine.getAggregatedOperationPolicy(boi);
            Set<QName> v = engine.getVocabulary(p, false);
            if (null == vocabulary) {
                vocabulary = v;
            } else {
                vocabulary.addAll(v); 
            }
            if (isServer) {
                p = engine.getAggregatedMessagePolicy(boi.getInput());
                vocabulary.addAll(engine.getVocabulary(p, false));
            } else if (null != boi.getOutput()) {
                for (BindingFaultInfo bfi : boi.getFaults()) {
                    p = engine.getAggregatedFaultPolicy(bfi);
                    vocabulary.addAll(engine.getVocabulary(p, false)); 
                }
            }
        }
        
        Set<QName> clientVocabulary = isServer ? null : new HashSet<QName>();
        if (!isServer) {
            clientVocabulary.addAll(vocabulary);
            for (BindingOperationInfo boi : ei.getBinding().getOperations()) {
                if (null != boi.getOutput()) {
                    Policy p = engine.getAggregatedMessagePolicy(boi.getOutput());
                    clientVocabulary.addAll(engine.getVocabulary(p, false)); 
                }
            }
        }

        for (QName qn : vocabulary) {
            PolicyInterceptorProvider pp = reg.get(qn);
            if (null != pp) {
                if (isServer) {
                    inInterceptors.addAll(pp.getInInterceptors());
                } else {
                    inFaultInterceptors.addAll(pp.getInFaultInterceptors());
                }
            }
        }
        if (!isServer) {
            for (QName qn : clientVocabulary) {
                PolicyInterceptorProvider pp = reg.get(qn);
                if (null != pp) {
                    inInterceptors.addAll(pp.getInInterceptors());
                }
            }
        }
    }
    
    // for test
    
    void setPolicy(Policy ep) {
        policy = ep;
    }
    
    void setChosenAlternative(List<Assertion> c) {
        chosenAlternative = c;
    }
    
    void setInInterceptors(List<Interceptor> in) {
        inInterceptors = in;
    }
    
    void setInFaultInterceptors(List<Interceptor> inFault) {
        inFaultInterceptors = inFault;
    }
    
    
}
