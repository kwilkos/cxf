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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.AbstractAttributedInterceptorProvider;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * 
 */
public class ClientPolicyInfo extends AbstractAttributedInterceptorProvider  {
    
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ClientPolicyInfo.class);
    
    private Policy requestPolicy;
    private Policy responsePolicy;
     
    private List<Assertion> chosenAlternative;
    private List<Assertion> responseVocabulary;
    
    void initialise(BindingOperationInfo boi, EndpointInfo ei, Conduit conduit, PolicyEngine engine) {
        initialiseRequestPolicy(boi, ei, engine);
        chooseAlternative(conduit, engine);
        initialiseOutInterceptors(engine);  
        initialiseResponsePolicy(boi, ei, engine);
        initialiseInInterceptors(engine);
    }
    
    public Policy getRequestPolicy() {
        return requestPolicy;        
    }
    
    void setRequestPolicy(Policy ep) {
        requestPolicy = ep;
    }
    
    public Policy getResponsePolicy() {
        return responsePolicy;        
    }
    
    void setResponsePolicy(Policy ep) {
        responsePolicy = ep;
    }
    
    public List<Assertion> getChosenAlternative() {
        return chosenAlternative;
    }
    
    void setChosenAlternative(List<Assertion> c) {
        chosenAlternative = c;
    }
    
    public List<Assertion> getResponseVolcabulary() {
        return responseVocabulary;
    }
    
    void setResponseVocabulary(List<Assertion> rv) {
        responseVocabulary = rv;
    }
    
    /**
     * Get the effective policy for the outbound message specified by the binding operation 
     * by merging the policies from the policy providers and normalising the result.
     * 
     * @param boi the binding operation info specifying the outbound message
     * @param engine the policy engine
     * 
     * @return the normalised, effective policy for the outbound message
     */
    void initialiseRequestPolicy(BindingOperationInfo boi, EndpointInfo ei, PolicyEngine engine) {
        BindingMessageInfo bmi = boi.getInput();

        requestPolicy = engine.getAggregatedMessagePolicy(bmi);
        requestPolicy = requestPolicy.merge(engine.getAggregatedOperationPolicy(boi));
        requestPolicy = requestPolicy.merge(engine.getAggregatedEndpointPolicy(ei));
        requestPolicy = requestPolicy.merge(engine.getAggregatedServicePolicy(ei.getService()));
        requestPolicy = (Policy)requestPolicy.normalize(true);
    }

    /**
     * Choose the first alternative for which the vocabulary is supported.
     * 
     * @param conduit the conduit
     * @param engine the policy engine
     * @throws PolicyException if no such alternative exists
     */
    void chooseAlternative(Conduit conduit, PolicyEngine engine) {
        Iterator alternatives = requestPolicy.getAlternatives();
        while (alternatives.hasNext()) {
            List<Assertion> alternative = CastUtils.cast((List)alternatives.next(), Assertion.class);
            if (engine.supportsAlternative(alternative, conduit)) {
                setChosenAlternative(alternative);
                return;
            }
        }
        throw new PolicyException(new Message("CLIENT_OUT_NO_ALTERNATIVE_EXC", BUNDLE));

    }
    

    /** 
     * Based on the chosen alternative, determine the interceptors that need to be present on the
     * outbound chain in order to support the assertions in this alternative.
     * @param engine the policy engine
     */
    void initialiseOutInterceptors(PolicyEngine engine) {
        PolicyInterceptorProviderRegistry reg 
            = engine.getBus().getExtension(PolicyInterceptorProviderRegistry.class);
        List<Interceptor> out = new ArrayList<Interceptor>();
        for (Assertion a : getChosenAlternative()) {
            if (a.isOptional()) {
                continue;
            }
            QName qn = a.getName();
            PolicyInterceptorProvider pp = reg.get(qn);
            if (null != pp) {
                out.addAll(pp.getOutInterceptors());
            }
        }
        setOutInterceptors(out);
    }
    
    void initialiseResponsePolicy(BindingOperationInfo boi, EndpointInfo ei, PolicyEngine engine) {
        BindingMessageInfo bmi = boi.getOutput();
        if (null == bmi) {
            return;
        }
        responsePolicy = engine.getAggregatedMessagePolicy(bmi);
        for (BindingFaultInfo bfi : boi.getFaults()) {
            responsePolicy = responsePolicy.merge(engine.getAggregatedFaultPolicy(bfi));
        }
        responsePolicy = responsePolicy.merge(engine.getAggregatedOperationPolicy(boi));
        responsePolicy = responsePolicy.merge(engine.getAggregatedEndpointPolicy(ei));
        responsePolicy = responsePolicy.merge(engine.getAggregatedServicePolicy(ei.getService()));
        responsePolicy = (Policy)responsePolicy.normalize(true);         
    }
    
    /** 
     * Based on the calculated response policy, determine the interceptors that need to be present on the
     * outbound chain in order to support the assertions in this alternative.
     * @param engine the policy engine
     */
    void initialiseInInterceptors(PolicyEngine engine) {
        if (null == responsePolicy) {
            return;
        }
        PolicyInterceptorProviderRegistry reg 
            = engine.getBus().getExtension(PolicyInterceptorProviderRegistry.class);
        List<Interceptor> in = new ArrayList<Interceptor>();
        List<Interceptor> inFault = new ArrayList<Interceptor>();
        Iterator alternatives = responsePolicy.getAlternatives();
        while (alternatives.hasNext()) {
            List<Assertion> alternative = CastUtils.cast((List)alternatives.next(), Assertion.class);
            for (Assertion a : alternative) {
                if (a.isOptional()) {
                    continue;
                }
                QName qn = a.getName();
                PolicyInterceptorProvider pp = reg.get(qn);
                if (null != pp) {
                    in.addAll(pp.getInInterceptors());
                    inFault.addAll(pp.getInFaultInterceptors());
                }
            }
        }
        setInInterceptors(in);
        setInFaultInterceptors(inFault);
    }
}
