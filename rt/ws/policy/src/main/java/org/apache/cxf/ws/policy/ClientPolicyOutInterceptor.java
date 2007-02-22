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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.Conduit;

/**
 * 
 */
public class ClientPolicyOutInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOG = LogUtils.getL7dLogger(ClientPolicyOutInterceptor.class);
    private Bus bus;
    
    public ClientPolicyOutInterceptor() {
        setId(PolicyConstants.CLIENT_POLICY_OUT_INTERCEPTOR_ID);
        setPhase(Phase.SETUP);
    }
    
    public void setBus(Bus b) {
        bus = b;
    }
    
    public Bus getBus() {
        return bus;
    }
    
    public void handleMessage(Message msg) {
        if (!PolicyUtils.isRequestor(msg)) {
            LOG.fine("Not a requestor.");
            return;
        }
        
        Exchange exchange = msg.getExchange();
        assert null != exchange;
        
        BindingOperationInfo boi = exchange.get(BindingOperationInfo.class);
        if (null == boi) {
            LOG.fine("No binding operation info.");
            return;
        }
        
        Endpoint e = exchange.get(Endpoint.class);
        if (null == e) {
            LOG.fine("No endpoint.");
            return;
        }        
        
        PolicyEngine pe = bus.getExtension(PolicyEngine.class);
        if (null == pe) {
            return;
        }
        
        Conduit conduit = msg.getConduit();
        
        // add the required interceptors
        
        List<Interceptor> policyOutInterceptors = pe.getClientOutInterceptors(e, boi, conduit);
        for (Interceptor poi : policyOutInterceptors) {            
            msg.getInterceptorChain().add(poi);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Added interceptor of type " + poi.getClass().getSimpleName());
            }
        }
        
        // insert assertions of the chosen alternative into the message
        
        msg.put(PolicyConstants.CLIENT_OUT_ASSERTIONS, pe.getClientOutAssertions(e, boi, conduit));
    }
}
