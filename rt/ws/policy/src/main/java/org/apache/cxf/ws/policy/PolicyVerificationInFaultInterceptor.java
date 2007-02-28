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

import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingFaultInfo;

/**
 * 
 */
public class PolicyVerificationInFaultInterceptor extends AbstractPolicyInterceptor {

    private static final Logger LOG 
        = LogUtils.getL7dLogger(PolicyVerificationInFaultInterceptor.class);

    public PolicyVerificationInFaultInterceptor() {
        setPhase(Phase.PRE_INVOKE);
    }

    /** 
     * Determines the effective policy, and checks if one of its alternatives  
     * is supported.
     *  
     * @param message
     */
    public void handleMessage(Message message) throws Fault {
        
        if (!PolicyUtils.isRequestor(message)) {
            return; 
        }
        
        Exchange exchange = message.getExchange();
        assert null != exchange;
        
        BindingFaultInfo bfi = message.get(BindingFaultInfo.class);
        if (null == bfi) {
            LOG.fine("No binding fault info.");
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
        
        AssertionInfoMap aim = message.get(AssertionInfoMap.class);
        if (null == aim) {
            return;
        }
        
        OutPolicyInfo opi = pe.getClientFaultPolicyInfo(e, bfi);
        opi.checkEffectivePolicy(aim);
        LOG.fine("Verified policies for inbound message.");
    }

}
