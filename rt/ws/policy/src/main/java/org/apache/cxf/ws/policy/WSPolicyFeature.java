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
import java.util.List;

import org.w3c.dom.Element;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.neethi.Policy;

/**
 * Configures a Server, Client, Bus with the specified policies. If a series of 
 * Policy <code>Element</code>s are supplied, these will be loaded into a Policy
 * class using the <code>PolicyBuilder</code> extension on the bus. If the 
 * PolicyEngine has not been started, this feature will start it.
 *
 * @see PolicyBuilder
 * @see AbstractFeature
 */
public class WSPolicyFeature extends AbstractFeature {
    private Collection<Policy> policies;
    private Collection<Element> policyElements;
    
    public WSPolicyFeature() {
        super();
    }

    public WSPolicyFeature(Policy... ps) {
        super();
        policies = new ArrayList<Policy>();
        Collections.addAll(policies, ps);
    }

    @Override
    public void initialize(Client client, Bus bus) {
        Endpoint endpoint = client.getEndpoint();
        
        intializeEndpoint(endpoint, bus);
    }

    @Override
    public void initialize(Server server, Bus bus) {
        Endpoint endpoint = server.getEndpoint();
        
        intializeEndpoint(endpoint, bus);
    }

    private void intializeEndpoint(Endpoint endpoint, Bus bus) {
        Collection<Policy> loadedPolicies = null;
        if (policyElements != null) {
            loadedPolicies = new ArrayList<Policy>();
            PolicyBuilder builder = bus.getExtension(PolicyBuilder.class);
            
            for (Element e : policyElements) {
                loadedPolicies.add(builder.getPolicy(e));
            }
        } 
        
        ensurePolicyEngineActivated(bus);
        
        List<ServiceInfo> sis = endpoint.getService().getServiceInfos();
        for (ServiceInfo si : sis) {
            if (policies != null) {
                for (Policy p : policies) {
                    si.addExtensor(p);
                }
            }
            
            if (loadedPolicies != null) {
                for (Policy p : loadedPolicies) {
                    si.addExtensor(p);
                }
            }
        }
    }

    private void ensurePolicyEngineActivated(Bus bus) {
        PolicyEngine pe = bus.getExtension(PolicyEngine.class);
        
        // Create a PolicyEngine and enable it if there isn't one
        if (pe == null) {
            PolicyEngineImpl pei = new PolicyEngineImpl();
            pei.setBus(bus);
            bus.setExtension(pei, PolicyEngine.class);
            pe = pei;
        }
        
        synchronized (pe) {
            if (!pe.isEnabled()) {
                pe.setEnabled(true);
            }
        }
    }

    public Collection<Policy> getPolicies() {
        if (policies == null) {
            policies = new ArrayList<Policy>();
        }
        return policies;
    }

    public void setPolicies(Collection<Policy> policies) {
        this.policies = policies;
    }

    public Collection<Element> getPolicyElements() {
        if (policyElements == null) {
            policyElements = new ArrayList<Element>();
        }
        return policyElements;
    }

    public void setPolicyElements(Collection<Element> policyElements) {
        this.policyElements = policyElements;
    }
}
