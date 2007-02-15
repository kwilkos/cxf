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
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * 
 */
public class ClientRequestPolicyInfo  {
    
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ClientRequestPolicyInfo.class);
    
    private Policy requestPolicy;     
    private List<Assertion> chosenAlternative;
    private List<Interceptor> outInterceptors;
    
    void initialise(BindingOperationInfo boi, EndpointInfo ei, PolicyEngine engine, Assertor assertor) {
        initialiseRequestPolicy(boi, ei, engine);
        chooseAlternative(engine, assertor);
        initialiseInterceptors(engine);  
    }
   
    public Policy getRequestPolicy() {
        return requestPolicy;        
    }
    
    public List<Interceptor> getOutInterceptors() {
        return outInterceptors;
    }
    
    public List<Assertion> getChosenAlternative() {
        return chosenAlternative;
    }
    
    
    
    void initialiseRequestPolicy(BindingOperationInfo boi, EndpointInfo ei, PolicyEngine engine) {
        BindingMessageInfo bmi = boi.getInput();

        requestPolicy = engine.getAggregatedMessagePolicy(bmi);
        requestPolicy = requestPolicy.merge(engine.getAggregatedOperationPolicy(boi));
        requestPolicy = requestPolicy.merge(engine.getAggregatedEndpointPolicy(ei));
        requestPolicy = requestPolicy.merge(engine.getAggregatedServicePolicy(ei.getService()));
        requestPolicy = (Policy)requestPolicy.normalize(true);
    }

    void chooseAlternative(PolicyEngine engine, Assertor assertor) {
        Iterator alternatives = requestPolicy.getAlternatives();
        while (alternatives.hasNext()) {
            List<Assertion> alternative = CastUtils.cast((List)alternatives.next(), Assertion.class);
            if (engine.supportsAlternative(alternative, assertor)) {
                setChosenAlternative(alternative);
                return;
            }
        }
        throw new PolicyException(new Message("CLIENT_OUT_NO_ALTERNATIVE_EXC", BUNDLE));

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
        setOutInterceptors(out);
    }
    
    // for tests
    
    void setRequestPolicy(Policy ep) {
        requestPolicy = ep;
    }
    
    void setChosenAlternative(List<Assertion> c) {
        chosenAlternative = c;
    }
    
    void setOutInterceptors(List<Interceptor> out) {
        outInterceptors = out;
    }
   
}
