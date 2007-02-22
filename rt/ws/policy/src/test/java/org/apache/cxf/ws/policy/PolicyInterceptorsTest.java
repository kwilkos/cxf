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

import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.neethi.Assertion;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * 
 */
public class PolicyInterceptorsTest extends TestCase {
    
    private IMocksControl control;
    private Message message;
    private Exchange exchange;
    private BindingOperationInfo boi;
    private Endpoint endpoint;
    private Bus bus;
    private PolicyEngine pe;
    private Conduit conduit;
    private Destination destination;
    
    
    public void setUp() {
        control = EasyMock.createNiceControl();
        bus = control.createMock(Bus.class);       
    } 
    
    public void testClientPolicyOutInterceptor() {
        ClientPolicyOutInterceptor interceptor = new ClientPolicyOutInterceptor();
        interceptor.setBus(bus);
       
        doTestBasics(interceptor, true, true);
        
        control.reset();
        setupMessage(true, true, true, true, true, true);
        Interceptor i = control.createMock(Interceptor.class);
        EasyMock.expect(pe.getClientOutInterceptors(endpoint, boi, conduit))
            .andReturn(CastUtils.cast(Collections.singletonList(i), Interceptor.class));
        InterceptorChain ic = control.createMock(InterceptorChain.class);
        EasyMock.expect(message.getInterceptorChain()).andReturn(ic);
        ic.add(i);
        EasyMock.expectLastCall();
        Collection<Assertion> assertions = CastUtils.cast(Collections.EMPTY_LIST, Assertion.class);
        EasyMock.expect(pe.getClientOutAssertions(endpoint, boi, conduit)).andReturn(assertions);
        EasyMock.expect(message.put(PolicyConstants.CLIENT_OUT_ASSERTIONS, assertions)).andReturn(null);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();        
    }
    
    public void testClientPolicyInInterceptor() {
        ClientPolicyInInterceptor interceptor = new ClientPolicyInInterceptor();
        interceptor.setBus(bus);
        
        doTestBasics(interceptor, true, false);
        
        control.reset();
        setupMessage(true, true, false, false, true, true);
        Interceptor i = control.createMock(Interceptor.class);
        EasyMock.expect(pe.getClientInInterceptors(endpoint, conduit))
            .andReturn(CastUtils.cast(Collections.singletonList(i), Interceptor.class));
        InterceptorChain ic = control.createMock(InterceptorChain.class);
        EasyMock.expect(message.getInterceptorChain()).andReturn(ic);
        ic.add(i);
        EasyMock.expectLastCall();
        control.replay();
        interceptor.handleMessage(message);
        control.verify();        
    }

    public void testClientPolicyInFaultInterceptor() {
        ClientPolicyInFaultInterceptor interceptor = new ClientPolicyInFaultInterceptor();
        interceptor.setBus(bus);
        
        doTestBasics(interceptor, true, false);
        
        control.reset();
        setupMessage(true, true, false, false, true, true);
        Interceptor i = control.createMock(Interceptor.class);
        EasyMock.expect(pe.getClientInFaultInterceptors(endpoint, conduit))
            .andReturn(CastUtils.cast(Collections.singletonList(i), Interceptor.class));
        InterceptorChain ic = control.createMock(InterceptorChain.class);
        EasyMock.expect(message.getInterceptorChain()).andReturn(ic);
        ic.add(i);
        EasyMock.expectLastCall();
        control.replay();
        interceptor.handleMessage(message);
        control.verify();        
    }

    public void testServerPolicyInInterceptor() {
        ServerPolicyInInterceptor interceptor = new ServerPolicyInInterceptor();
        interceptor.setBus(bus);
        
        doTestBasics(interceptor, false, false);
        
        control.reset();
        setupMessage(false, false, false, false, true, true);
        Interceptor i = control.createMock(Interceptor.class);
        EasyMock.expect(pe.getServerInInterceptors(endpoint, destination))
            .andReturn(CastUtils.cast(Collections.singletonList(i), Interceptor.class));
        InterceptorChain ic = control.createMock(InterceptorChain.class);
        EasyMock.expect(message.getInterceptorChain()).andReturn(ic);
        ic.add(i);
        EasyMock.expectLastCall();
        control.replay();
        interceptor.handleMessage(message);
        control.verify();        
    }
    
    public void testServerPolicyOutInterceptor() {
        ServerPolicyOutInterceptor interceptor = new ServerPolicyOutInterceptor();
        interceptor.setBus(bus);
        
        doTestBasics(interceptor, false, true);
        
        control.reset();
        setupMessage(false, false, true, true, true, true);
        Interceptor i = control.createMock(Interceptor.class);
        EasyMock.expect(pe.getServerOutInterceptors(endpoint, boi, destination))
            .andReturn(CastUtils.cast(Collections.singletonList(i), Interceptor.class));
        InterceptorChain ic = control.createMock(InterceptorChain.class);
        EasyMock.expect(message.getInterceptorChain()).andReturn(ic);
        ic.add(i);
        EasyMock.expectLastCall();
        Collection<Assertion> assertions = CastUtils.cast(Collections.EMPTY_LIST, Assertion.class);
        EasyMock.expect(pe.getServerOutAssertions(endpoint, boi, destination)).andReturn(assertions);
        EasyMock.expect(message.put(PolicyConstants.SERVER_OUT_ASSERTIONS, assertions)).andReturn(null);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();        
    }
    
    public void testServerPolicyOutFaultInterceptor() {
        ServerPolicyOutFaultInterceptor interceptor = new ServerPolicyOutFaultInterceptor();
        interceptor.setBus(bus);
        
        doTestBasics(interceptor, false, true);
        
        control.reset();
        setupMessage(false, false, true, true, true, true);
        Interceptor i = control.createMock(Interceptor.class);
        EasyMock.expect(pe.getServerOutFaultInterceptors(endpoint, boi, destination))
            .andReturn(CastUtils.cast(Collections.singletonList(i), Interceptor.class));
        InterceptorChain ic = control.createMock(InterceptorChain.class);
        EasyMock.expect(message.getInterceptorChain()).andReturn(ic);
        ic.add(i);
        EasyMock.expectLastCall();
        control.replay();
        interceptor.handleMessage(message);
        control.verify();        
    }
    
    private void doTestBasics(Interceptor<Message> interceptor, boolean isClient, boolean usesOperationInfo) {
        setupMessage(!isClient, isClient, usesOperationInfo, false, false, false);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
        
        control.reset();
        setupMessage(isClient, isClient, usesOperationInfo, !usesOperationInfo, false, false);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
        
        control.reset();
        setupMessage(true, isClient, usesOperationInfo, usesOperationInfo, false, false);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
            
        control.reset();
        setupMessage(true, isClient, usesOperationInfo, usesOperationInfo, true, false);
        control.replay();
        interceptor.handleMessage(message);
        control.verify();
    }
    
    void setupMessage(boolean setupRequestor,
                      boolean isClient,
                      boolean usesOperationInfo,
                      boolean setupOperation, 
                      Boolean setupEndpoint, 
                      Boolean setupEngine) {

        message = control.createMock(Message.class);
        EasyMock.expect(message.get(Message.REQUESTOR_ROLE))
            .andReturn(setupRequestor ? Boolean.TRUE : Boolean.FALSE);
        if (setupRequestor != isClient) {
            return;
        }
        
        exchange = control.createMock(Exchange.class);
        EasyMock.expect(message.getExchange()).andReturn(exchange);
        
        if (usesOperationInfo) {
            if (null == boi && setupOperation) {
                boi = control.createMock(BindingOperationInfo.class);
            }
            EasyMock.expect(exchange.get(BindingOperationInfo.class)).andReturn(setupOperation ? boi : null);
            if (!setupOperation) {
                return;
            }
        }
        
        if (null == endpoint && setupEndpoint) {
            endpoint = control.createMock(Endpoint.class);
        }
        EasyMock.expect(exchange.get(Endpoint.class)).andReturn(setupEndpoint ? endpoint : null);
        if (!setupEndpoint) {
            return;
        }
        
        /*
        if (null == ei) {
            ei = control.createMock(EndpointInfo.class);            
        }
        EasyMock.expect(endpoint.getEndpointInfo()).andReturn(ei);
        */
        
        if (null == pe && setupEngine) {
            pe = control.createMock(PolicyEngine.class);
        }
        EasyMock.expect(bus.getExtension(PolicyEngine.class)).andReturn(setupEngine ? pe : null);
        if (!setupEngine) {
            return;
        }
            
        if (isClient) {
            conduit = control.createMock(Conduit.class);
            EasyMock.expect(message.getConduit()).andReturn(conduit);
        } else {
            destination = control.createMock(Destination.class);
            EasyMock.expect(message.getDestination()).andReturn(destination);
        }
      
    }
}
