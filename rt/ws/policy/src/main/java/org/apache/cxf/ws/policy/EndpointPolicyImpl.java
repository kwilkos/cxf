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
import java.util.Collection;
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
public class EndpointPolicyImpl implements EndpointPolicy {
    
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(EndpointPolicyImpl.class);
    
    private Policy policy;
    private Collection<Assertion> chosenAlternative;
    private Collection<Assertion> vocabulary;
    private Collection<Assertion> faultVocabulary;
    private List<Interceptor> interceptors;
    private List<Interceptor> faultInterceptors;
    
    public Policy getPolicy() {
        return policy;        
    }
    
    public Collection<Assertion> getChosenAlternative() {
        return chosenAlternative;
    }
    
    public Collection<Assertion> getVocabulary() {
        return vocabulary;
    }
    
    public Collection<Assertion> getFaultVocabulary() {
        return faultVocabulary;
    }    
    
    public List<Interceptor> getInterceptors() {
        return interceptors;
    }
    
    public List<Interceptor> getFaultInterceptors() {
        return faultInterceptors;
    }
    
    
    void initialise(EndpointInfo ei, boolean isRequestor, PolicyEngineImpl engine, Assertor assertor) {
        initialisePolicy(ei, engine);
        chooseAlternative(engine, assertor);
        initialiseVocabulary(ei, isRequestor, engine);
        initialiseInterceptors(ei, isRequestor, engine); 
    }
   
    void initialisePolicy(EndpointInfo ei, PolicyEngineImpl engine) {
        policy = engine.getAggregatedServicePolicy(ei.getService());
        policy = policy.merge(engine.getAggregatedEndpointPolicy(ei));
        policy = (Policy)policy.normalize(true);
    }

    void chooseAlternative(PolicyEngineImpl engine, Assertor assertor) {
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
    
    void initialiseVocabulary(EndpointInfo ei, boolean requestor, PolicyEngineImpl engine) {
        vocabulary = new ArrayList<Assertion>();
        if (requestor) {
            faultVocabulary = new ArrayList<Assertion>();
        }
       
        // vocabulary of alternative chosen for endpoint
        
        for (Assertion a : getChosenAlternative()) {
            if (a.isOptional()) {
                continue;
            }
            vocabulary.add(a);            
            if (null != faultVocabulary) {
                faultVocabulary.add(a);
            }
        }
   
        // add assertions for specific inbound (in case of a server endpoint) or outbound 
        // (in case of a client endpoint) messages
        
        for (BindingOperationInfo boi : ei.getBinding().getOperations()) {
            Policy p = engine.getAggregatedOperationPolicy(boi);
            Collection<Assertion> c = engine.getAssertions(p, false);
            vocabulary.addAll(c);
            if (null != faultVocabulary) {
                faultVocabulary.addAll(c);
            }
 
            if (!requestor) {
                p = engine.getAggregatedMessagePolicy(boi.getInput());
                vocabulary.addAll(engine.getAssertions(p, false));
            } else if (null != boi.getOutput()) {
                p = engine.getAggregatedMessagePolicy(boi.getOutput());
                vocabulary.addAll(engine.getAssertions(p, false));
                
                for (BindingFaultInfo bfi : boi.getFaults()) { 
                    p = engine.getAggregatedFaultPolicy(bfi);
                    faultVocabulary.addAll(engine.getAssertions(p, false));
                }
            }
        }
    }

    void initialiseInterceptors(EndpointInfo ei, boolean requestor, PolicyEngineImpl engine) {
        PolicyInterceptorProviderRegistry reg 
            = engine.getBus().getExtension(PolicyInterceptorProviderRegistry.class);
        interceptors = new ArrayList<Interceptor>();
        if (requestor) {
            faultInterceptors = new ArrayList<Interceptor>();
        }
        
        Set<QName> v = new HashSet<QName>();
        for (Assertion a : vocabulary) {
            v.add(a.getName());
        }
        
        for (QName qn : v) {
            PolicyInterceptorProvider pp = reg.get(qn);
            if (null != pp) {
                interceptors.addAll(pp.getInInterceptors());
            }
        }
        
        if (!requestor) {
            return;
        }
        
        Set<QName> faultV = new HashSet<QName>();
        for (Assertion a : faultVocabulary) {
            faultV.add(a.getName());
        }
        
        for (QName qn : faultV) {
            PolicyInterceptorProvider pp = reg.get(qn);
            if (null != pp) {
                faultInterceptors.addAll(pp.getInFaultInterceptors());
            }
        }        
    }
    
    // for test
    
    void setPolicy(Policy ep) {
        policy = ep;
    }
    
    void setChosenAlternative(Collection<Assertion> c) {
        chosenAlternative = c;
    }
    
    void setVocabulary(Collection<Assertion> v) {
        vocabulary = v;
    }
    
    void setFaultVocabulary(Collection<Assertion> v) {
        faultVocabulary = v;
    }
    
    void setInterceptors(List<Interceptor> in) {
        interceptors = in;
    }
    
    void setFaultInterceptors(List<Interceptor> inFault) {
        faultInterceptors = inFault;
    }
    
    
}
