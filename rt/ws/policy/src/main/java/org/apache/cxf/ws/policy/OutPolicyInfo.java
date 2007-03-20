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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * 
 */
public class OutPolicyInfo {
    
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(OutPolicyInfo.class);
    
    protected Policy policy;     
    protected Collection<Assertion> chosenAlternative;
    protected List<Interceptor> interceptors;
    
    public void initialise(EndpointPolicyInfo epi, PolicyEngine engine) {
        policy = epi.getPolicy();
        chosenAlternative = epi.getChosenAlternative();
        initialiseInterceptors(engine);  
    }
    
    void initialise(Endpoint e, 
                    BindingOperationInfo boi, 
                    PolicyEngine engine, 
                    Assertor assertor,
                    boolean requestor) {
        initialisePolicy(e, boi, engine, requestor);
        chooseAlternative(engine, assertor);
        initialiseInterceptors(engine);  
    }
    
    void initialise(Endpoint e, 
                    BindingFaultInfo bfi, 
                    PolicyEngine engine, 
                    Assertor assertor) {
        initialisePolicy(e, bfi, engine);
        chooseAlternative(engine, assertor);
        initialiseInterceptors(engine);  
    }
   
    public Policy getPolicy() {
        return policy;        
    }
    
    public List<Interceptor> getInterceptors() {
        return interceptors;
    }
    
    public Collection<Assertion> getChosenAlternative() {
        return chosenAlternative;
    }
    
    
    
    void initialisePolicy(Endpoint e,
                          BindingOperationInfo boi,  
                          PolicyEngine engine, 
                          boolean requestor) {
        BindingMessageInfo bmi = requestor ? boi.getInput() : boi.getOutput();
        if (requestor) {
            policy = engine.getEndpointPolicyInfo(e, (Conduit)null).getPolicy();
        } else {
            policy = engine.getEndpointPolicyInfo(e, (Destination)null).getPolicy();
        }
        
        policy = policy.merge(engine.getAggregatedOperationPolicy(boi));
        if (null != bmi) {
            policy = policy.merge(engine.getAggregatedMessagePolicy(bmi));
        }
        policy = (Policy)policy.normalize(true);
    }
    
    void initialisePolicy(Endpoint e, BindingFaultInfo bfi, PolicyEngine engine) {
        BindingOperationInfo boi = bfi.getBindingOperation();
        policy = engine.getEndpointPolicyInfo(e, (Destination)null).getPolicy();         
        policy = policy.merge(engine.getAggregatedOperationPolicy(boi));
        policy = policy.merge(engine.getAggregatedFaultPolicy(bfi));
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

    void initialiseInterceptors(PolicyEngine engine) {
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
        setInterceptors(out);
    }
    
    void checkEffectivePolicy(AssertionInfoMap aim) {
        Iterator alternatives = policy.getAlternatives();
        while (alternatives.hasNext()) {      
            List<Assertion> alternative = CastUtils.cast((List)alternatives.next(), Assertion.class);
            if (alternativeSupported(alternative, aim)) {
                return;
            }
        }
        
        throw new PolicyException(new Message("NO_ALTERNATIVE_EXC", BUNDLE));
    }
    
    boolean alternativeSupported(List<Assertion> alternative, AssertionInfoMap aim) {
        
        for (Assertion a : alternative) {
            boolean asserted = false;
            Collection<AssertionInfo> ais = aim.get(a.getName());
            if (null != ais) {
                for (AssertionInfo ai : ais) {
                    // if (ai.getAssertion() == a && ai.isAsserted()) {
                    if (ai.getAssertion().equal(a) && ai.isAsserted()) {
                        asserted = true;
                        break;
                    }
                }
            }
            if (!asserted) {
                return false;
            }
        }
        
        return true;
    }
    
    // for tests
    
    void setPolicy(Policy ep) {
        policy = ep;
    }
    
    void setChosenAlternative(Collection<Assertion> c) {
        chosenAlternative = c;
    }
    
    void setInterceptors(List<Interceptor> out) {
        interceptors = out;
    }
   
}
