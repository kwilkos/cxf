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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.cxf.Bus;
import org.apache.cxf.extension.BusExtensionRegistrar;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.policy.attachment.wsdl11.Wsdl11AttachmentPolicyProvider;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * 
 */
public class PolicyEngine {
    
    private Bus bus;
    private PolicyBuilder builder;
    private List<PolicyProvider> policyProviders;

    private Map<BindingOperationInfo, ClientPolicyInfo> clientInfo 
        = new ConcurrentHashMap<BindingOperationInfo, ClientPolicyInfo>();

    public PolicyEngine() {
        this(null);
    }
    public PolicyEngine(BusExtensionRegistrar registrar) {
        if (null != registrar) {
            registrar.registerExtension(this, PolicyEngine.class);
            bus = registrar.getBus();
        }
    }
    
    public void setBus(Bus b) {
        bus = b;
    }
    
    public Bus getBus() {
        return bus;
    }
    
    public void setPolicyProviders(List<PolicyProvider> p) {
        policyProviders = p;
    }
   
    public List<PolicyProvider> getPolicyProviders() {
        return policyProviders;
    }
    
    public void setBuilder(PolicyBuilder b) {
        builder = b;
    }
    
    public PolicyBuilder getBuilder() {
        return builder;
    }
        
    @PostConstruct
    void init() {
        if (null == builder && null != bus) {
            builder = new PolicyBuilder();
            builder.setAssertionBuilderRegistry(bus.getExtension(AssertionBuilderRegistry.class));
        }

        if (null == policyProviders) {
            // TODO:
            // include attachment provider for wsdl 2.0 and
            // for external attachments
            Wsdl11AttachmentPolicyProvider wpp = new Wsdl11AttachmentPolicyProvider();
            wpp.setBuilder(builder);
            policyProviders = Collections.singletonList((PolicyProvider)wpp);
        }    
    }
    
    @PostConstruct
    void addBusInterceptors() {
        if (null == bus) {
            return;
        }

        ClientPolicyOutInterceptor clientOut = new ClientPolicyOutInterceptor();
        clientOut.setBus(bus);
        bus.getOutInterceptors().add(clientOut);
        ClientPolicyInInterceptor clientIn = new ClientPolicyInInterceptor();
        clientIn.setBus(bus);
        bus.getInInterceptors().add(clientIn);
        ClientPolicyInFaultInterceptor clientInFault = new ClientPolicyInFaultInterceptor();
        clientInFault.setBus(bus);
        bus.getInFaultInterceptors().add(clientInFault);
        
        // TODO: server side
    }

    public List<Interceptor> getClientOutInterceptors(BindingOperationInfo boi, 
                                                      EndpointInfo ei, Conduit conduit) {
        return getClientPolicyInfo(boi, ei, conduit).getOutInterceptors();
    }
    
    public List<Interceptor> getClientInInterceptors(BindingOperationInfo boi, 
                                                     EndpointInfo ei, Conduit conduit) {
        return getClientPolicyInfo(boi, ei, conduit).getInInterceptors();
    }
    
    public List<Interceptor> getClientInFaultInterceptors(BindingOperationInfo boi, 
                                                     EndpointInfo ei, Conduit conduit) {
        return getClientPolicyInfo(boi, ei, conduit).getInFaultInterceptors();
    }

    public Policy getAggregatedServicePolicy(ServiceInfo si) {
        Policy aggregated = null;
        for (PolicyProvider pp : getPolicyProviders()) {
            Policy p = pp.getEffectivePolicy(si);
            if (null == aggregated) {
                aggregated = p;
            } else {
                aggregated = aggregated.merge(p);
            }
        }
        return aggregated == null ? new Policy() : aggregated;
    }

    public Policy getAggregatedEndpointPolicy(EndpointInfo ei) {
        Policy aggregated = null;
        for (PolicyProvider pp : getPolicyProviders()) {
            Policy p = pp.getEffectivePolicy(ei);
            if (null == aggregated) {
                aggregated = p;
            } else {
                aggregated = aggregated.merge(p);
            }
        }
        return aggregated == null ? new Policy() : aggregated;
    }
    
    public Policy getAggregatedOperationPolicy(BindingOperationInfo boi) {
        Policy aggregated = null;
        for (PolicyProvider pp : getPolicyProviders()) {
            Policy p = pp.getEffectivePolicy(boi);
            if (null == aggregated) {
                aggregated = p;
            } else {
                aggregated = aggregated.merge(p);
            }
        }
        return aggregated == null ? new Policy() : aggregated;
    }
    
    public Policy getAggregatedMessagePolicy(BindingMessageInfo bmi) {
        Policy aggregated = null;
        for (PolicyProvider pp : getPolicyProviders()) {
            Policy p = pp.getEffectivePolicy(bmi);
            if (null == aggregated) {
                aggregated = p;
            } else {
                aggregated = aggregated.merge(p);
            }
        }
        return aggregated == null ? new Policy() : aggregated;
    }
    
    public Policy getAggregatedFaultPolicy(BindingFaultInfo bfi) {
        Policy aggregated = null;
        for (PolicyProvider pp : getPolicyProviders()) {
            Policy p = pp.getEffectivePolicy(bfi);
            if (null == aggregated) {
                aggregated = p;
            } else {
                aggregated = aggregated.merge(p);
            }
        }
        return aggregated == null ? new Policy() : aggregated;
    }

    /**
     * Check if a given list of assertions can be supported by the conduit or
     * interceptors (which, if necessary, can be added to the interceptor
     * chain).
     * 
     * @param alternative the policy alternative
     * @param conduit the conduit
     * @return true iff the alternative can be supported
     */
    boolean supportsAlternative(List<Assertion> alternative, Conduit conduit) {
        PolicyInterceptorProviderRegistry pipr = bus.getExtension(PolicyInterceptorProviderRegistry.class);
        for (Assertion a : alternative) {
            if (!(a.isOptional() 
                || (null != pipr.get(a.getName())) 
                || ((conduit instanceof Assertor) && ((Assertor)conduit).asserts(a)))) {
                return false;
            }
        }
        return true;
    }
    
    ClientPolicyInfo getClientPolicyInfo(BindingOperationInfo boi, EndpointInfo ei, Conduit conduit) {
        ClientPolicyInfo cpi = clientInfo.get(boi);
        if (null == cpi) {
            cpi = new ClientPolicyInfo();
            cpi.initialise(boi, ei, conduit, this);
            clientInfo.put(boi, cpi);
        }
        return cpi;
    }


}
