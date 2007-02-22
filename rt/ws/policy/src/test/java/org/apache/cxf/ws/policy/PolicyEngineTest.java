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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyRegistry;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * 
 */
public class PolicyEngineTest extends TestCase {

    private IMocksControl control;
    private PolicyEngine engine;
    
    public void setUp() {
        control = EasyMock.createNiceControl(); 
    } 
    
    public void testAccessors() {
        engine = new PolicyEngine();
        assertNotNull(engine.getRegistry());
        assertNull(engine.getBus());
        assertNull(engine.getPolicyProviders()); 
        assertTrue(!engine.getRegisterInterceptors());
        Bus bus = control.createMock(Bus.class);
        engine.setBus(bus);
        List<PolicyProvider> providers = CastUtils.cast(Collections.EMPTY_LIST, PolicyProvider.class);
        engine.setPolicyProviders(providers);
        PolicyRegistry reg = control.createMock(PolicyRegistry.class);
        engine.setRegistry(reg);
        engine.setRegisterInterceptors(true);
        assertSame(bus, engine.getBus());
        assertSame(providers, engine.getPolicyProviders());
        assertSame(reg, engine.getRegistry());
        assertTrue(engine.getRegisterInterceptors());
    }
    
    public void testDontAddBusInterceptors() {        
        doTestAddBusInterceptors(false);
    }
    
    public void testAddBusInterceptors() {        
        doTestAddBusInterceptors(true);
    }
    
    private void doTestAddBusInterceptors(boolean add) {        
        engine = new PolicyEngine();
        engine.setRegisterInterceptors(add);
    
        Bus bus = control.createMock(Bus.class);
        engine.setBus(bus);
        List<Interceptor> out = new ArrayList<Interceptor>();
        List<Interceptor> in = new ArrayList<Interceptor>();
        List<Interceptor> inFault = new ArrayList<Interceptor>();
        List<Interceptor> outFault = new ArrayList<Interceptor>();
        if (add) {
            EasyMock.expect(bus.getOutInterceptors()).andReturn(out).times(2);
            EasyMock.expect(bus.getInInterceptors()).andReturn(in).times(2);
            EasyMock.expect(bus.getInFaultInterceptors()).andReturn(inFault);
            EasyMock.expect(bus.getOutFaultInterceptors()).andReturn(outFault);
            control.replay();
        }
        
        engine.addBusInterceptors();
        
        if (add) {
            Set<String> idsOut = getInterceptorIds(out);
            Set<String> idsIn = getInterceptorIds(in);
            Set<String> idsInFault = getInterceptorIds(inFault);
            Set<String> idsOutFault = getInterceptorIds(outFault);
            assertEquals(2, out.size());
            assertTrue(idsOut.contains(PolicyConstants.CLIENT_POLICY_OUT_INTERCEPTOR_ID));
            assertTrue(idsOut.contains(PolicyConstants.SERVER_POLICY_OUT_INTERCEPTOR_ID));
            assertEquals(2, in.size());
            assertTrue(idsIn.contains(PolicyConstants.CLIENT_POLICY_IN_INTERCEPTOR_ID));
            assertTrue(idsIn.contains(PolicyConstants.SERVER_POLICY_IN_INTERCEPTOR_ID));
            assertEquals(1, inFault.size());
            assertTrue(idsInFault.contains(PolicyConstants.CLIENT_POLICY_IN_FAULT_INTERCEPTOR_ID));
            assertEquals(1, outFault.size());
            assertTrue(idsOutFault.contains(PolicyConstants.SERVER_POLICY_OUT_FAULT_INTERCEPTOR_ID));
        } else {
            assertEquals(0, out.size());
            assertEquals(0, in.size());
            assertEquals(0, inFault.size());
            assertEquals(0, outFault.size());
        }
        if (add) {
            control.verify();
        }
    }
    
    public void testGetAggregatedServicePolicy() {
        engine = new PolicyEngine();
        List<PolicyProvider> providers = new ArrayList<PolicyProvider>();
        engine.setPolicyProviders(providers);
        ServiceInfo si = control.createMock(ServiceInfo.class);
        
        control.replay();
        Policy p = engine.getAggregatedServicePolicy(si);
        assertTrue(p.isEmpty());
        control.verify();
        control.reset();
        
        PolicyProvider provider1 = control.createMock(PolicyProvider.class);
        providers.add(provider1);
        Policy p1 = control.createMock(Policy.class);
        EasyMock.expect(provider1.getEffectivePolicy(si)).andReturn(p1);
        
        control.replay();
        assertSame(p1, engine.getAggregatedServicePolicy(si));
        control.verify();
        control.reset();
        
        PolicyProvider provider2 = control.createMock(PolicyProvider.class);
        providers.add(provider2);
        Policy p2 = control.createMock(Policy.class);
        Policy p3 = control.createMock(Policy.class);
        EasyMock.expect(provider1.getEffectivePolicy(si)).andReturn(p1);
        EasyMock.expect(provider2.getEffectivePolicy(si)).andReturn(p2);
        EasyMock.expect(p1.merge(p2)).andReturn(p3);
        
        control.replay();
        assertSame(p3, engine.getAggregatedServicePolicy(si));
        control.verify();      
    }
    
    public void testGetAggregatedEndpointPolicy() {
        engine = new PolicyEngine();
        List<PolicyProvider> providers = new ArrayList<PolicyProvider>();
        engine.setPolicyProviders(providers);
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        
        control.replay();
        Policy p = engine.getAggregatedEndpointPolicy(ei);
        assertTrue(p.isEmpty());
        control.verify();
        control.reset();
        
        PolicyProvider provider1 = control.createMock(PolicyProvider.class);
        providers.add(provider1);
        Policy p1 = control.createMock(Policy.class);
        EasyMock.expect(provider1.getEffectivePolicy(ei)).andReturn(p1);
        
        control.replay();
        assertSame(p1, engine.getAggregatedEndpointPolicy(ei));
        control.verify();
        control.reset();
        
        PolicyProvider provider2 = control.createMock(PolicyProvider.class);
        providers.add(provider2);
        Policy p2 = control.createMock(Policy.class);
        Policy p3 = control.createMock(Policy.class);
        EasyMock.expect(provider1.getEffectivePolicy(ei)).andReturn(p1);
        EasyMock.expect(provider2.getEffectivePolicy(ei)).andReturn(p2);
        EasyMock.expect(p1.merge(p2)).andReturn(p3);
        
        control.replay();
        assertSame(p3, engine.getAggregatedEndpointPolicy(ei));
        control.verify();      
    }
    
    public void testGetAggregatedOperationPolicy() {
        engine = new PolicyEngine();
        List<PolicyProvider> providers = new ArrayList<PolicyProvider>();
        engine.setPolicyProviders(providers);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        
        control.replay();
        Policy p = engine.getAggregatedOperationPolicy(boi);
        assertTrue(p.isEmpty());
        control.verify();
        control.reset();
        
        PolicyProvider provider1 = control.createMock(PolicyProvider.class);
        providers.add(provider1);
        Policy p1 = control.createMock(Policy.class);
        EasyMock.expect(provider1.getEffectivePolicy(boi)).andReturn(p1);
        
        control.replay();
        assertSame(p1, engine.getAggregatedOperationPolicy(boi));
        control.verify();
        control.reset();
        
        PolicyProvider provider2 = control.createMock(PolicyProvider.class);
        providers.add(provider2);
        Policy p2 = control.createMock(Policy.class);
        Policy p3 = control.createMock(Policy.class);
        EasyMock.expect(provider1.getEffectivePolicy(boi)).andReturn(p1);
        EasyMock.expect(provider2.getEffectivePolicy(boi)).andReturn(p2);
        EasyMock.expect(p1.merge(p2)).andReturn(p3);
        
        control.replay();
        assertSame(p3, engine.getAggregatedOperationPolicy(boi));
        control.verify();      
    }
    
    public void testGetAggregatedMessagePolicy() {
        engine = new PolicyEngine();
        List<PolicyProvider> providers = new ArrayList<PolicyProvider>();
        engine.setPolicyProviders(providers);
        BindingMessageInfo bmi = control.createMock(BindingMessageInfo.class);
        
        control.replay();
        Policy p = engine.getAggregatedMessagePolicy(bmi);
        assertTrue(p.isEmpty());
        control.verify();
        control.reset();
        
        PolicyProvider provider1 = control.createMock(PolicyProvider.class);
        providers.add(provider1);
        Policy p1 = control.createMock(Policy.class);
        EasyMock.expect(provider1.getEffectivePolicy(bmi)).andReturn(p1);
        
        control.replay();
        assertSame(p1, engine.getAggregatedMessagePolicy(bmi));
        control.verify();
        control.reset();
        
        PolicyProvider provider2 = control.createMock(PolicyProvider.class);
        providers.add(provider2);
        Policy p2 = control.createMock(Policy.class);
        Policy p3 = control.createMock(Policy.class);
        EasyMock.expect(provider1.getEffectivePolicy(bmi)).andReturn(p1);
        EasyMock.expect(provider2.getEffectivePolicy(bmi)).andReturn(p2);
        EasyMock.expect(p1.merge(p2)).andReturn(p3);
        
        control.replay();
        assertSame(p3, engine.getAggregatedMessagePolicy(bmi));
        control.verify();      
    }
    
    public void testGetAggregatedFaultPolicy() {
        engine = new PolicyEngine();
        List<PolicyProvider> providers = new ArrayList<PolicyProvider>();
        engine.setPolicyProviders(providers);
        BindingFaultInfo bfi = control.createMock(BindingFaultInfo.class);
        
        control.replay();
        Policy p = engine.getAggregatedFaultPolicy(bfi);
        assertTrue(p.isEmpty());
        control.verify();
        control.reset();
        
        PolicyProvider provider1 = control.createMock(PolicyProvider.class);
        providers.add(provider1);
        Policy p1 = control.createMock(Policy.class);
        EasyMock.expect(provider1.getEffectivePolicy(bfi)).andReturn(p1);
        
        control.replay();
        assertSame(p1, engine.getAggregatedFaultPolicy(bfi));
        control.verify();
        control.reset();
        
        PolicyProvider provider2 = control.createMock(PolicyProvider.class);
        providers.add(provider2);
        Policy p2 = control.createMock(Policy.class);
        Policy p3 = control.createMock(Policy.class);
        EasyMock.expect(provider1.getEffectivePolicy(bfi)).andReturn(p1);
        EasyMock.expect(provider2.getEffectivePolicy(bfi)).andReturn(p2);
        EasyMock.expect(p1.merge(p2)).andReturn(p3);
        
        control.replay();
        assertSame(p3, engine.getAggregatedFaultPolicy(bfi));
        control.verify();      
    }
    
    public void testGetClientOutInterceptors() throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("getClientRequestPolicyInfo",
                                                        new Class[] {Endpoint.class,
                                                                     BindingOperationInfo.class,
                                                                     Conduit.class});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        Endpoint e = control.createMock(Endpoint.class);
        Conduit conduit = control.createMock(Conduit.class);
        ClientRequestPolicyInfo cpi = control.createMock(ClientRequestPolicyInfo.class);
        EasyMock.expect(engine.getClientRequestPolicyInfo(e, boi, conduit)).andReturn(cpi);
        Interceptor i = control.createMock(Interceptor.class);
        List<Interceptor> li = Collections.singletonList(i);
        EasyMock.expect(cpi.getOutInterceptors()).andReturn(li);        

        control.replay();
        List<Interceptor> clientInterceptors = engine.getClientOutInterceptors(e, boi, conduit); 
        assertSame(li, clientInterceptors);
        control.verify();
    }
    
    public void testGetClientInInterceptors() throws NoSuchMethodException { 
        doTestGetInterceptors(false, false);
    }
    
    public void testGetClientInFaultInterceptors() throws NoSuchMethodException { 
        doTestGetInterceptors(false, true);
    }
    
    public void testGetServerInFaultInterceptors() throws NoSuchMethodException { 
        doTestGetInterceptors(true, false);
    }
    
    public void testServerOutInterceptors() throws NoSuchMethodException {
        doTestGetServerOutInterceptors(false);
    }
    
    public void testServerOutFaultInterceptors() throws NoSuchMethodException {
        doTestGetServerOutInterceptors(true);
    }
     
    private void doTestGetInterceptors(boolean isServer, boolean fault) throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("getEndpointPolicyInfo",
            new Class[] {Endpoint.class, isServer ? Destination.class : Conduit.class});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        Endpoint e = control.createMock(Endpoint.class);
        Conduit conduit = null;
        Destination destination = null;
        EndpointPolicyInfo epi = control.createMock(EndpointPolicyInfo.class);
        
        if (isServer) {
            destination = control.createMock(Destination.class);
            EasyMock.expect(engine.getEndpointPolicyInfo(e, destination)).andReturn(epi);
        } else {
            conduit = control.createMock(Conduit.class);
            EasyMock.expect(engine.getEndpointPolicyInfo(e, conduit)).andReturn(epi);
        }
        
        Interceptor i = control.createMock(Interceptor.class);
        List<Interceptor> li = Collections.singletonList(i);
        if (fault) {
            EasyMock.expect(epi.getInFaultInterceptors()).andReturn(li); 
        } else {
            EasyMock.expect(epi.getInInterceptors()).andReturn(li);  
        }

        control.replay();
        List<Interceptor> interceptors = fault 
            ? engine.getClientInFaultInterceptors(e, conduit) 
            : (isServer 
                ? engine.getServerInInterceptors(e, destination)
                : engine.getClientInInterceptors(e, conduit));
        assertSame(li, interceptors);
        control.verify(); 
    }
    
    private void doTestGetServerOutInterceptors(boolean fault) throws NoSuchMethodException {

        Method m = PolicyEngine.class.getDeclaredMethod("getServerResponsePolicyInfo",
                                                        new Class[] {Endpoint.class,
                                                                     BindingOperationInfo.class,
                                                                     Destination.class});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        Endpoint e = control.createMock(Endpoint.class);
        Destination destination = control.createMock(Destination.class);
        ServerResponsePolicyInfo srpi = control.createMock(ServerResponsePolicyInfo.class);
        EasyMock.expect(engine.getServerResponsePolicyInfo(e, boi, destination)).andReturn(srpi);
        Interceptor i = control.createMock(Interceptor.class);
        List<Interceptor> li = Collections.singletonList(i);
        if (fault) {
            EasyMock.expect(srpi.getOutFaultInterceptors()).andReturn(li);
        } else {
            EasyMock.expect(srpi.getOutInterceptors()).andReturn(li);
        }

        control.replay();
        List<Interceptor> interceptors = fault ? engine.getServerOutFaultInterceptors(e, boi, destination)
            : engine.getServerOutInterceptors(e, boi, destination);
        assertSame(li, interceptors);
        control.verify();
    }
    
    private Set<String> getInterceptorIds(List<Interceptor> interceptors) {
        Set<String> ids = new HashSet<String>();
        for (Interceptor i : interceptors) {
            ids.add(((PhaseInterceptor)i).getId());
        }
        return ids;
    }
    
    
}
