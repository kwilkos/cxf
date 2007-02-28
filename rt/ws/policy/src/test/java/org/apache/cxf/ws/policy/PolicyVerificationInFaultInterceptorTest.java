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

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * 
 */
public class PolicyVerificationInFaultInterceptorTest extends TestCase {
 
    private IMocksControl control;
    private Bus bus;
    private Message message;
    private Exchange exchange;
    private BindingFaultInfo bfi;
    private Endpoint endpoint;
    private PolicyEngine engine;
    private AssertionInfoMap aim;
    
    public void setUp() {
        control = EasyMock.createNiceControl(); 
        bus = control.createMock(Bus.class);  
    } 
    
    public void testHandleMessage() {
        PolicyVerificationInFaultInterceptor interceptor = 
            new PolicyVerificationInFaultInterceptor();
        interceptor.setBus(bus);
        
        setupMessage(false, false, false, false, false);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
        
        control.reset();
        setupMessage(true, false, false, false, false);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
        
        control.reset();
        setupMessage(true, true, false, false, false);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
        
        control.reset();
        setupMessage(true, true, true, false, false);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
        
        control.reset();
        setupMessage(true, true, true, true, false);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
        
        control.reset();
        setupMessage(true, true, true, true, true);
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);        
        EasyMock.expect(engine.getClientFaultPolicyInfo(endpoint, bfi)).andReturn(opi);
        opi.checkEffectivePolicy(aim);
        EasyMock.expectLastCall();
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
    }
    
    void setupMessage(boolean requestor,
                      boolean setupBindingFaultInfo,
                      boolean setupEndpoint,
                      boolean setupPolicyEngine,
                      boolean setupAssertionInfoMap) {
        if (null == message) {
            message = control.createMock(Message.class); 
        }
        if (null == exchange) {
            exchange = control.createMock(Exchange.class);            
        }
        EasyMock.expect(message.get(Message.REQUESTOR_ROLE)).andReturn(
            requestor ? Boolean.TRUE : Boolean.FALSE);
        if (!requestor) {
            return;
        }
        
        EasyMock.expect(message.getExchange()).andReturn(exchange);
        if (setupBindingFaultInfo && null == bfi) {
            bfi = control.createMock(BindingFaultInfo.class);
        }
        EasyMock.expect(message.get(BindingFaultInfo.class)).andReturn(bfi);
        if (!setupBindingFaultInfo) {
            return;
        }
        if (setupEndpoint && null == endpoint) {
            endpoint = control.createMock(Endpoint.class);
        }
        EasyMock.expect(exchange.get(Endpoint.class)).andReturn(endpoint);
        if (!setupEndpoint) {
            return;
        }
        
        if (setupPolicyEngine && null == engine) {
            engine = control.createMock(PolicyEngine.class);
        }
        EasyMock.expect(bus.getExtension(PolicyEngine.class)).andReturn(engine);
        if (!setupPolicyEngine) {
            return;           
        }
        if (setupAssertionInfoMap && null == aim) {
            aim = control.createMock(AssertionInfoMap.class);
        }
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(aim);
    }

}
