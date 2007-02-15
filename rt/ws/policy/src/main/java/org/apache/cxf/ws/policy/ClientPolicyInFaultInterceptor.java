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

import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;

/**
 * 
 */
public class ClientPolicyInFaultInterceptor extends AbstractPhaseInterceptor<Message> {

    private Bus bus;
    
    public ClientPolicyInFaultInterceptor() {
        setId(PolicyConstants.CLIENT_POLICY_IN_FAULT_INTERCEPTOR_ID);
        setPhase(Phase.RECEIVE);
    }
        
    public void setBus(Bus b) {
        bus = b;
    }
    
    public Bus getBus() {
        return bus;
    }
    
    public void handleMessage(Message msg) {        
        if (!PolicyUtils.isRequestor(msg)) {
            return;
        }
        
        EndpointInfo ei = msg.get(EndpointInfo.class);
        if (null == ei) {
            return;
        }
        
        PolicyEngine pe = bus.getExtension(PolicyEngine.class);
        if (null == pe) {
            return;
        }
        
        Conduit conduit = msg.getConduit();
        
        // We do not know the underlying message type yet - so we pre-emptively add interceptors 
        // that can deal with the response and all of the operation's possible fault messages.
        
        List<Interceptor> policyInFaultInterceptors = pe.getClientInFaultInterceptors(ei, conduit);
        for (Interceptor poi : policyInFaultInterceptors) {
            msg.getInterceptorChain().add(poi);
        }
    }
}
