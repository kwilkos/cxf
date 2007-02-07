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
import java.util.List;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.extension.BusExtensionRegistrar;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.neethi.Policy;
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
        assertNull(engine.getBuilder());
        assertNull(engine.getPolicyProviders());
        PolicyBuilder builder = control.createMock(PolicyBuilder.class);
        engine.setBuilder(builder);
        Bus bus = control.createMock(Bus.class);
        engine.setBus(bus);
        List<PolicyProvider> providers = CastUtils.cast(Collections.EMPTY_LIST, PolicyProvider.class);
        engine.setPolicyProviders(providers);
        assertSame(builder, engine.getBuilder());
        assertSame(bus, engine.getBus());
        assertSame(providers, engine.getPolicyProviders());
    }
    
    public void testInit() {  
        Bus bus = control.createMock(Bus.class);
        BusExtensionRegistrar br = control.createMock(BusExtensionRegistrar.class);
        br.registerExtension((PolicyEngine)EasyMock.isA(PolicyEngine.class), 
                             EasyMock.eq(PolicyEngine.class));
        EasyMock.expectLastCall();
        EasyMock.expect(br.getBus()).andReturn(bus);
        AssertionBuilderRegistry abr = control.createMock(AssertionBuilderRegistry.class);
        EasyMock.expect(bus.getExtension(AssertionBuilderRegistry.class)).andReturn(abr);
        
        control.replay();
        engine = new PolicyEngine(br);
        engine.init();
        assertSame(bus, engine.getBus());
        assertEquals(1, engine.getPolicyProviders().size());
        assertNotNull(engine.getBuilder());
        control.verify();    
    }
    
    public void testAddBusInterceptors() {        
        engine = new PolicyEngine();
        engine.addBusInterceptors();
        
        Bus bus = control.createMock(Bus.class);
        engine.setBus(bus);
        List<Interceptor> out = new ArrayList<Interceptor>();
        List<Interceptor> in = new ArrayList<Interceptor>();
        List<Interceptor> inFault = new ArrayList<Interceptor>();
        EasyMock.expect(bus.getOutInterceptors()).andReturn(out);
        EasyMock.expect(bus.getInInterceptors()).andReturn(in);
        EasyMock.expect(bus.getInFaultInterceptors()).andReturn(inFault);
        
        control.replay();
        engine.addBusInterceptors();
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof ClientPolicyOutInterceptor);
        assertEquals(1, in.size());
        assertTrue(in.get(0) instanceof ClientPolicyInInterceptor);
        assertEquals(1, inFault.size());
        assertTrue(inFault.get(0) instanceof ClientPolicyInFaultInterceptor);
        control.verify();
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
        doTestGetClientInterceptors(true, false);                
    }
    
    public void testGetClientInInterceptors() throws NoSuchMethodException { 
        doTestGetClientInterceptors(false, false);       
    }
    
    public void testGetClientInFaultInterceptors() throws NoSuchMethodException { 
        doTestGetClientInterceptors(false, true);       
    }
    
    public void doTestGetClientInterceptors(boolean out, boolean fault) throws NoSuchMethodException { 
        Method m = PolicyEngine.class.getDeclaredMethod("getClientPolicyInfo", 
            new Class[] {BindingOperationInfo.class, EndpointInfo.class, Conduit.class});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        Conduit conduit = control.createMock(Conduit.class);
        
        ClientPolicyInfo cpi = control.createMock(ClientPolicyInfo.class);
        EasyMock.expect(engine.getClientPolicyInfo(boi, ei, conduit)).andReturn(cpi);
        Interceptor i = control.createMock(Interceptor.class);
        List<Interceptor> li = Collections.singletonList(i);
        if (out) {
            EasyMock.expect(cpi.getOutInterceptors()).andReturn(li);
        } else {
            if (fault) {
                EasyMock.expect(cpi.getInFaultInterceptors()).andReturn(li);
            } else {
                EasyMock.expect(cpi.getInInterceptors()).andReturn(li);
            }
        }
        
        control.replay();
        List<Interceptor> clientInterceptors = out ? engine.getClientOutInterceptors(boi, ei, conduit)
            : (fault ? engine.getClientInFaultInterceptors(boi, ei, conduit) 
                : engine.getClientInInterceptors(boi, ei, conduit));        
        assertSame(li, clientInterceptors);        
        control.verify();                
    }
    
    public void xtestSupportsAlternative() {
        doTestSupportsAlternative(true);
    }    
    
    void doTestSupportsAlternative(boolean supported) {
        /*
        PolicyInterceptorProviderRegistry pipr = control.createMock(PolicyInterceptorProviderRegistry.class);
        EasyMock.expect(bus.getExtension(PolicyInterceptorProviderRegistry.class)).andReturn(pipr);
        Assertion a = control.createMock(Assertion.class);
        List<Assertion> alternative = Collections.singletonList(a);
        EasyMock.expect(a.isOptional()).andReturn(false);
        QName qn = new QName("http://x.y.z", "a");
        EasyMock.expect(a.getName()).andReturn(qn);
        EasyMock.expect(pipr.get(qn)).andReturn(null);
        AssertingConduit ac = control.createMock(AssertingConduit.class);
        EasyMock.expect(ac.asserts(a)).andReturn(supported);
                
        control.replay();
        assertEquals(supported, engine.supportsAlternative(alternative, ac));
        control.verify();
        */
    }
    
    
}
