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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.extension.BusExtension;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyOperator;
import org.apache.neethi.PolicyReference;
import org.apache.neethi.PolicyRegistry;

/**
 * 
 */
public class PolicyEngine implements BusExtension {
    
    private Bus bus;
    private PolicyRegistry registry;
    private Collection<PolicyProvider> policyProviders;
    private boolean registerInterceptors;

    private Map<BindingOperationInfo, ClientRequestPolicyInfo> clientRequestInfo 
        = new ConcurrentHashMap<BindingOperationInfo, ClientRequestPolicyInfo>();
    private Map<EndpointInfo, EndpointPolicyInfo> endpointInfo 
        = new ConcurrentHashMap<EndpointInfo, EndpointPolicyInfo>();
    private Map<BindingOperationInfo, ServerResponsePolicyInfo> serverResponseInfo 
        = new ConcurrentHashMap<BindingOperationInfo, ServerResponsePolicyInfo>();

    public PolicyEngine() {
        registry = new PolicyRegistryImpl();
    }
    
    public Class getRegistrationType() {
        return PolicyEngine.class;
    }
    
    public void setBus(Bus b) {
        bus = b;
    }
    
    public Bus getBus() {
        return bus;
    }
    
    public void setPolicyProviders(Collection<PolicyProvider> p) {
        policyProviders = p;
    }
   
    public Collection<PolicyProvider> getPolicyProviders() {
        return policyProviders;
    }
    
    public void setRegistry(PolicyRegistry r) {
        registry = r;
    }
    
    public PolicyRegistry getRegistry() {
        return registry;
    }
      
    public boolean getRegisterInterceptors() {
        return registerInterceptors;
    }

    public void setRegisterInterceptors(boolean ri) {
        registerInterceptors = ri;
    }
    
    @PostConstruct
    void addBusInterceptors() {
        if (null == bus || !registerInterceptors) {
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
        
        ServerPolicyInInterceptor serverIn = new ServerPolicyInInterceptor();
        serverIn.setBus(bus);
        bus.getInInterceptors().add(serverIn);
        ServerPolicyOutInterceptor serverOut = new ServerPolicyOutInterceptor();
        serverOut.setBus(bus);
        bus.getOutInterceptors().add(serverOut);
        ServerPolicyOutFaultInterceptor serverOutFault = new ServerPolicyOutFaultInterceptor();
        serverOutFault.setBus(bus);
        bus.getOutFaultInterceptors().add(serverOutFault);
        
        // TODO: policy verification interceptors
    }

    public List<Interceptor> getClientOutInterceptors(BindingOperationInfo boi, 
                                                      EndpointInfo ei, Conduit c) {
        return getClientRequestPolicyInfo(boi, ei, c).getOutInterceptors();
    }
    
    public List<Interceptor> getClientInInterceptors(EndpointInfo ei, Conduit c) {        
        return getEndpointPolicyInfo(ei, c).getInInterceptors();
    }
    
    public List<Interceptor> getClientInFaultInterceptors(EndpointInfo ei, Conduit c) {
        return getEndpointPolicyInfo(ei, c).getInFaultInterceptors();
    }
    
    public List<Interceptor> getServerInInterceptors(EndpointInfo ei, Destination d) {
        return getEndpointPolicyInfo(ei, d).getInInterceptors();
    }
    
    public List<Interceptor> getServerOutInterceptors(BindingOperationInfo boi, 
                                                      EndpointInfo ei, Destination d) {
        return getServerResponsePolicyInfo(boi, ei, d).getOutInterceptors();
    }
    
    public List<Interceptor> getServerOutFaultInterceptors(BindingOperationInfo boi, 
                                                      EndpointInfo ei, Destination d) {
        return getServerResponsePolicyInfo(boi, ei, d).getOutFaultInterceptors();
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
     * Return a collection of all assertions used in the given policy component,
     * optionally including optional assertions.
     * The policy need not be normalised, so any policy references will have to be resolved.
     * @param pc the policy component
     * @param includeOptional flag indicating if optional assertions should be included
     * @return the assertions
     */
    public Collection<Assertion> getAssertions(PolicyComponent pc, boolean includeOptional) {
        
        if (Constants.TYPE_ASSERTION == pc.getType()) {
            return Collections.singletonList((Assertion)pc);
        } 
        
        Collection<Assertion> assertions = new ArrayList<Assertion>();
        addAssertions(pc, includeOptional, assertions);
        return assertions;
    }
    
    private void addAssertions(PolicyComponent pc, boolean includeOptional, 
                               Collection<Assertion> assertions) {

        if (Constants.TYPE_ASSERTION == pc.getType()) {
            Assertion a = (Assertion)pc;
            if (includeOptional || !a.isOptional()) {
                assertions.add((Assertion)pc);
                return;
            }
        } 
        
        if (Constants.TYPE_POLICY_REF == pc.getType()) {
            PolicyReference pr = (PolicyReference)pc;
            pc = pr.normalize(registry, false);
        }

        assert Constants.TYPE_POLICY == pc.getType() 
                || Constants.TYPE_POLICY == pc.getType() 
                || Constants.TYPE_EXACTLYONE == pc.getType();

        PolicyOperator po = (PolicyOperator)pc;

        List<PolicyComponent> pcs = CastUtils.cast(po.getPolicyComponents(), PolicyComponent.class);
        for (PolicyComponent child : pcs) {
            addAssertions(child, includeOptional, assertions);
        }
    }
    
    /**
     * Return the vocabulary of a policy component, i.e. the set of QNames of
     * the assertions used in the componente, duplicates removed.
     * @param pc the policy component
     * @param includeOptional flag indicating if optional assertions should be included
     * @return the vocabulary
     */
    public Set<QName> getVocabulary(PolicyComponent pc, boolean includeOptional) {
        Collection<Assertion> assertions = getAssertions(pc, includeOptional);
        Set<QName> vocabulary = new HashSet<QName>();
        for (Assertion a : assertions) {
            vocabulary.add(a.getName());
        }
        return vocabulary;
    }


    /**
     * Check if a given list of assertions can potentially be supported by
     * interceptors or by an already installed assertor (a conduit or transport
     * that implements the Assertor interface).
     * 
     * @param alternative the policy alternative
     * @param Assertor the assertor
     * @return true iff the alternative can be supported
     */
    boolean supportsAlternative(List<Assertion> alternative, Assertor assertor) {
        PolicyInterceptorProviderRegistry pipr = bus.getExtension(PolicyInterceptorProviderRegistry.class);
        for (Assertion a : alternative) {
            if (!(a.isOptional() 
                || (null != pipr.get(a.getName())) 
                || (null != assertor && assertor.asserts(a)))) {
                return false;
            }
        }
        return true;
    }
    
    ClientRequestPolicyInfo getClientRequestPolicyInfo(BindingOperationInfo boi, EndpointInfo ei, Conduit c) {
        ClientRequestPolicyInfo crpi = clientRequestInfo.get(boi);
        if (null == crpi) {
            crpi = new ClientRequestPolicyInfo();
            Assertor assertor = null;
            if (c instanceof Assertor) {
                assertor = (Assertor)c;
            }
            crpi.initialise(boi, ei, this, assertor);
            clientRequestInfo.put(boi, crpi);
        }
        return crpi;
    }
    
    EndpointPolicyInfo getEndpointPolicyInfo(EndpointInfo ei, Conduit conduit) {
        EndpointPolicyInfo epi = endpointInfo.get(ei);
        if (null != epi) {
            return epi;
        }
        Assertor assertor = conduit instanceof Assertor ? (Assertor)conduit : null;
        return createEndpointPolicyInfo(ei, false, assertor);
    }
    
    EndpointPolicyInfo getEndpointPolicyInfo(EndpointInfo ei, Destination destination) {
        EndpointPolicyInfo epi = endpointInfo.get(ei);
        if (null != epi) {
            return epi;
        }
        Assertor assertor = destination instanceof Assertor ? (Assertor)destination : null;
        return createEndpointPolicyInfo(ei, true, assertor);
    }
    
    EndpointPolicyInfo createEndpointPolicyInfo(EndpointInfo ei, boolean isServer, Assertor assertor) {
        EndpointPolicyInfo epi = new EndpointPolicyInfo();
        epi.initialise(ei, isServer, this, assertor);
        endpointInfo.put(ei, epi);

        return epi;
    }
    
    ServerResponsePolicyInfo getServerResponsePolicyInfo(BindingOperationInfo boi, 
                                                         EndpointInfo ei, Destination d) {
        ServerResponsePolicyInfo srpi = serverResponseInfo.get(boi);
        if (null == srpi) {
            srpi = new ServerResponsePolicyInfo();
            Assertor assertor = null;
            if (d instanceof Assertor) {
                assertor = (Assertor)d;
            }
            srpi.initialise(boi, ei, this, assertor);
            serverResponseInfo.put(boi, srpi);
        }
        return srpi;
    }


}
