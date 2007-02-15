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
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * 
 */
public class ServerResponsePolicyInfo {
    
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ServerResponsePolicyInfo.class);
    
    private Policy responsePolicy;     
    private List<Assertion> chosenAlternative;
    private List<Interceptor> outInterceptors;
    private List<Interceptor> outFaultInterceptors;
    
       
    public Policy getRequestPolicy() {
        return responsePolicy;
    }
    
    public List<Assertion> getChosenAlternative() {
        return chosenAlternative;
    }
    
    public List<Interceptor> getOutInterceptors() {
        return outInterceptors;
    } 
    
    public List<Interceptor> getOutFaultInterceptors() {
        return outFaultInterceptors;
    }   
    
    public void initialise(BindingOperationInfo boi, EndpointInfo ei, 
                           PolicyEngine engine, Assertor assertor) {
        initialiseResponsePolicy(boi, ei, engine);
        chooseAlternative(engine, ei, assertor);
        initialiseOutInterceptors(boi, engine);
        initialiseOutFaultInterceptors(boi, ei, engine);
    }
    
    void initialiseResponsePolicy(BindingOperationInfo boi, EndpointInfo ei, PolicyEngine engine) {
        responsePolicy = engine.getEndpointPolicyInfo(ei, (Destination)null).getPolicy(); 
        responsePolicy = responsePolicy.merge(engine.getAggregatedOperationPolicy(boi));
        if (null != boi.getOutput()) {
            responsePolicy = responsePolicy.merge(engine.getAggregatedMessagePolicy(boi.getOutput()));
        }
        responsePolicy = (Policy)responsePolicy.normalize(true);
    }

    void chooseAlternative(PolicyEngine engine, EndpointInfo ei, Assertor assertor) {
        EndpointPolicyInfo epi = engine.getEndpointPolicyInfo(ei, (Destination)null);
        Iterator alternatives = responsePolicy.getAlternatives();
        while (alternatives.hasNext()) {
            List<Assertion> alternative = CastUtils.cast((List)alternatives.next(), Assertion.class);
            if (engine.supportsAlternative(alternative, assertor)
                && alternative.contains(epi.getChosenAlternative())) {
                setChosenAlternative(alternative);
                return;
            }
        }
        throw new PolicyException(new Message("NO_ALTERNATIVE_EXC", BUNDLE));
    }

    void initialiseOutInterceptors(BindingOperationInfo boi, PolicyEngine engine) {
        outInterceptors = new ArrayList<Interceptor>();
        
        if (null == boi.getOutput()) {
            return;
        }     
        
        PolicyInterceptorProviderRegistry reg = engine.getBus()
            .getExtension(PolicyInterceptorProviderRegistry.class);

        for (Assertion a : chosenAlternative) {
            if (a.isOptional()) {
                continue;
            }
            QName qn = a.getName();
            PolicyInterceptorProvider pp = reg.get(qn);
            if (null != pp) {
                outInterceptors.addAll(pp.getOutInterceptors());
            }
        }
    }
    
    void initialiseOutFaultInterceptors(BindingOperationInfo boi, EndpointInfo ei, PolicyEngine engine) {
        outFaultInterceptors = new ArrayList<Interceptor>();
        
        if (null == boi.getOutput()) {
            return;
        }     
        
        PolicyInterceptorProviderRegistry reg = engine.getBus()
            .getExtension(PolicyInterceptorProviderRegistry.class);

        // the minimal set of interceptors required to satisfy the effective
        // policy for
        // any outbound fault message
        for (Assertion a : getChosenAlternative()) {
            if (a.isOptional()) {
                continue;
            }
            QName qn = a.getName();
            PolicyInterceptorProvider pp = reg.get(qn);
            if (null != pp) {
                outFaultInterceptors.addAll(pp.getOutFaultInterceptors());
            }
        }

        // add the interceptors that may be needed to support the additional
        // assertions for specific faults

        
        Policy p = engine.getAggregatedOperationPolicy(boi);
        Set<QName> vocabulary = engine.getVocabulary(p, false);
        for (BindingFaultInfo bfi : boi.getFaults()) {
            p = engine.getAggregatedFaultPolicy(bfi);
            vocabulary.addAll(engine.getVocabulary(p, false));
        }
        
        for (QName qn : vocabulary) {
            PolicyInterceptorProvider pp = reg.get(qn);
            if (null != pp) {
                outFaultInterceptors.addAll(pp.getOutFaultInterceptors());
            }
        }
    }
    
    // for test
    
    void setOutInterceptors(List<Interceptor> out) {
        outInterceptors = out;
    }
    
    void setOutFaultInterceptors(List<Interceptor> outFault) {
        outFaultInterceptors = outFault;
    }
    
    void setResponsePolicy(Policy ep) {
        responsePolicy = ep;
    }
    
    void setChosenAlternative(List<Assertion> c) {
        chosenAlternative = c;
    }
}
