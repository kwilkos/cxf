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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

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
import org.apache.cxf.ws.policy.builder.primitive.PrimitiveAssertion;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyReference;
import org.apache.neethi.PolicyRegistry;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class PolicyEngineTest extends Assert {

    private IMocksControl control;
    private PolicyEngine engine;
    
    @Before
    public void setUp() {
        control = EasyMock.createNiceControl(); 
    } 
    
    @Test
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
        
        assertNotNull(engine.createOutPolicyInfo());
        assertNotNull(engine.createEndpointPolicyInfo());
        
    }
    
    @Test
    public void testDontAddBusInterceptors() {        
        doTestAddBusInterceptors(false);
    }
    
    @Test
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
            EasyMock.expect(bus.getOutInterceptors()).andReturn(out).times(3);
            EasyMock.expect(bus.getInInterceptors()).andReturn(in).times(3);
            EasyMock.expect(bus.getInFaultInterceptors()).andReturn(inFault).times(2);
            EasyMock.expect(bus.getOutFaultInterceptors()).andReturn(outFault);
            control.replay();
        }
        
        engine.addBusInterceptors();
        
        if (add) {
            Set<String> idsOut = getInterceptorIds(out);
            Set<String> idsIn = getInterceptorIds(in);
            Set<String> idsInFault = getInterceptorIds(inFault);
            Set<String> idsOutFault = getInterceptorIds(outFault);
            assertEquals(3, out.size());
            assertTrue(idsOut.contains(PolicyConstants.CLIENT_POLICY_OUT_INTERCEPTOR_ID));
            assertTrue(idsOut.contains(PolicyConstants.SERVER_POLICY_OUT_INTERCEPTOR_ID));
            assertTrue(idsOut.contains(PolicyVerificationOutInterceptor.class.getName()));
            assertEquals(3, in.size());
            assertTrue(idsIn.contains(PolicyConstants.CLIENT_POLICY_IN_INTERCEPTOR_ID));
            assertTrue(idsIn.contains(PolicyConstants.SERVER_POLICY_IN_INTERCEPTOR_ID));
            assertTrue(idsIn.contains(PolicyVerificationInInterceptor.class.getName()));
            assertEquals(2, inFault.size());
            assertTrue(idsInFault.contains(PolicyConstants.CLIENT_POLICY_IN_FAULT_INTERCEPTOR_ID));
            assertTrue(idsInFault.contains(PolicyVerificationInFaultInterceptor.class.getName()));
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
    
    @Test
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
    
    @Test
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
    
    @Test
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
    
    @Test
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
    
    @Test
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
    
    @Test
    public void testGetAssertions() throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("addAssertions",
            new Class[] {PolicyComponent.class, boolean.class, Collection.class});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        Assertion a = control.createMock(Assertion.class);
        EasyMock.expect(a.getType()).andReturn(Constants.TYPE_ASSERTION);
        EasyMock.expect(a.isOptional()).andReturn(true);
        
        control.replay();
        assertTrue(engine.getAssertions(a, false).isEmpty());
        control.verify();
        
        control.reset();
        EasyMock.expect(a.getType()).andReturn(Constants.TYPE_ASSERTION);
        // EasyMock.expect(a.isOptional()).andReturn(false);
        
        control.replay();
        Collection<Assertion> ca = engine.getAssertions(a, true);
        assertEquals(1, ca.size());
        assertSame(a, ca.iterator().next());
        control.verify();
        
        control.reset();
        Policy p = control.createMock(Policy.class);
        EasyMock.expect(p.getType()).andReturn(Constants.TYPE_POLICY);
        engine.addAssertions(EasyMock.eq(p), EasyMock.eq(false), 
                             CastUtils.cast(EasyMock.isA(Collection.class), Assertion.class));
        EasyMock.expectLastCall();
        
        control.replay();
        assertTrue(engine.getAssertions(p, false).isEmpty());
        control.verify();
    }
    
    @Test
    public void testAddAssertions() {
        engine = new PolicyEngine();
        Collection<Assertion> assertions = new ArrayList<Assertion>();
        
        Assertion a = control.createMock(Assertion.class);
        EasyMock.expect(a.getType()).andReturn(Constants.TYPE_ASSERTION);
        EasyMock.expect(a.isOptional()).andReturn(true);
        
        control.replay();
        engine.addAssertions(a, false, assertions);
        assertTrue(assertions.isEmpty());
        control.verify();
        
        control.reset();
        EasyMock.expect(a.getType()).andReturn(Constants.TYPE_ASSERTION);
        control.replay();
        engine.addAssertions(a, true, assertions);
        assertEquals(1, assertions.size());
        assertSame(a, assertions.iterator().next());        
        control.verify();
        
        assertions.clear();
        Policy p = new Policy();
        a = new PrimitiveAssertion(new QName("http://x.y.z", "a"));
        p.addAssertion(a);
        PolicyReference pr = new PolicyReference();
        pr.setURI("a#b");
        engine.getRegistry().register("a#b", p);
        
        engine.addAssertions(pr, false, assertions);
        assertEquals(1, assertions.size());
        assertSame(a, assertions.iterator().next());       
    }
    
    @Test
    public void testKeys() {
        engine = new PolicyEngine();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);      
        BindingFaultInfo bfi = control.createMock(BindingFaultInfo.class);  
        control.replay();
        
        PolicyEngine.BindingOperation bo = engine.new BindingOperation(endpoint, boi); 
        assertNotNull(bo);
        PolicyEngine.BindingOperation bo2 = engine.new BindingOperation(endpoint, boi);
        assertEquals(bo, bo2);
        assertEquals(bo.hashCode(), bo2.hashCode());
                  
        PolicyEngine.BindingFault bf = engine.new BindingFault(endpoint, bfi);
        assertNotNull(bf);
        PolicyEngine.BindingFault bf2 = engine.new BindingFault(endpoint, bfi);
        assertEquals(bf, bf2);
        assertEquals(bf.hashCode(), bf2.hashCode());
              
        control.verify();
    }
    
    @Test
    public void testGetClientRequestPolicyInfo() throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("createOutPolicyInfo", new Class[] {});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        engine.init();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class); 
        AssertingConduit conduit = control.createMock(AssertingConduit.class);
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        EasyMock.expect(engine.createOutPolicyInfo()).andReturn(opi);
        opi.initialise(endpoint, boi, engine, conduit, true);
        EasyMock.expectLastCall();
        control.replay();
        assertSame(opi, engine.getClientRequestPolicyInfo(endpoint, boi, conduit));
        assertSame(opi, engine.getClientRequestPolicyInfo(endpoint, boi, conduit));
        control.verify();
    }
    
    @Test 
    public void testSetClientRequestPolicyInfo() {
        engine = new PolicyEngine();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        engine.setClientRequestPolicyInfo(endpoint, boi, opi);
        assertSame(opi, engine.getClientRequestPolicyInfo(endpoint, boi, (Conduit)null));        
    }
    
    @Test
    public void testGetServerRequestPolicyInfo() throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("createOutPolicyInfo", new Class[] {});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        engine.init();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class); 
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        EasyMock.expect(engine.createOutPolicyInfo()).andReturn(opi);
        opi.initialisePolicy(endpoint, boi, engine, false);
        EasyMock.expectLastCall();
        control.replay();
        assertSame(opi, engine.getServerRequestPolicyInfo(endpoint, boi));
        assertSame(opi, engine.getServerRequestPolicyInfo(endpoint, boi));
        control.verify();
    }
    
    @Test 
    public void testSetServerRequestPolicyInfo() {
        engine = new PolicyEngine();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        engine.setServerRequestPolicyInfo(endpoint, boi, opi);
        assertSame(opi, engine.getServerRequestPolicyInfo(endpoint, boi));        
    }
    
    @Test
    public void testGetClientResponsePolicyInfo() throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("createOutPolicyInfo", new Class[] {});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        engine.init();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class); 
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        EasyMock.expect(engine.createOutPolicyInfo()).andReturn(opi);
        opi.initialisePolicy(endpoint, boi, engine, true);
        EasyMock.expectLastCall();
        control.replay();
        assertSame(opi, engine.getClientResponsePolicyInfo(endpoint, boi));
        assertSame(opi, engine.getClientResponsePolicyInfo(endpoint, boi));
        control.verify();
    }
    
    @Test 
    public void testSetClientResponsePolicyInfo() {
        engine = new PolicyEngine();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        engine.setClientResponsePolicyInfo(endpoint, boi, opi);
        assertSame(opi, engine.getClientResponsePolicyInfo(endpoint, boi));        
    }
    
    @Test
    public void testGetClientFaultPolicyInfo() throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("createOutPolicyInfo", new Class[] {});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        engine.init();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingFaultInfo bfi = control.createMock(BindingFaultInfo.class); 
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        EasyMock.expect(engine.createOutPolicyInfo()).andReturn(opi);
        opi.initialisePolicy(endpoint, bfi, engine);
        EasyMock.expectLastCall();
        control.replay();
        assertSame(opi, engine.getClientFaultPolicyInfo(endpoint, bfi));
        assertSame(opi, engine.getClientFaultPolicyInfo(endpoint, bfi));
        control.verify();
    }
    
    @Test 
    public void testSetClientFaultPolicyInfo() {
        engine = new PolicyEngine();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingFaultInfo bfi = control.createMock(BindingFaultInfo.class);
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        engine.setClientFaultPolicyInfo(endpoint, bfi, opi);
        assertSame(opi, engine.getClientFaultPolicyInfo(endpoint, bfi));        
    }
    
    @Test
    public void testGetEndpointPolicyInfoClientSide() throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("createEndpointPolicyInfo", 
            new Class[] {Endpoint.class, boolean.class, Assertor.class});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        engine.init();
        Endpoint endpoint = control.createMock(Endpoint.class);
        AssertingConduit conduit = control.createMock(AssertingConduit.class);
        EndpointPolicyInfo epi = control.createMock(EndpointPolicyInfo.class);
        EasyMock.expect(engine.createEndpointPolicyInfo(endpoint, false, conduit)).andReturn(epi);
        control.replay();
        assertSame(epi, engine.getEndpointPolicyInfo(endpoint, conduit));
        control.verify();        
    }
    
    @Test
    public void testGetEndpointPolicyInfoServerSide() throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("createEndpointPolicyInfo", 
            new Class[] {Endpoint.class, boolean.class, Assertor.class});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        engine.init();
        Endpoint endpoint = control.createMock(Endpoint.class);
        AssertingDestination destination = control.createMock(AssertingDestination.class);
        EndpointPolicyInfo epi = control.createMock(EndpointPolicyInfo.class);
        EasyMock.expect(engine.createEndpointPolicyInfo(endpoint, true, destination)).andReturn(epi);
        control.replay();
        assertSame(epi, engine.getEndpointPolicyInfo(endpoint, destination));
        control.verify();        
    }
    
    @Test
    public void testCreateEndpointPolicyInfo() throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("createEndpointPolicyInfo", new Class[] {});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        engine.init();
        Endpoint endpoint = control.createMock(Endpoint.class);
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        EasyMock.expect(endpoint.getEndpointInfo()).andReturn(ei);
        Assertor assertor = control.createMock(Assertor.class);
        EndpointPolicyInfo epi = control.createMock(EndpointPolicyInfo.class);
        EasyMock.expect(engine.createEndpointPolicyInfo()).andReturn(epi);
        epi.initialise(ei, false, engine, assertor);
        EasyMock.expectLastCall();
        control.replay();
        assertSame(epi, engine.createEndpointPolicyInfo(endpoint, false, assertor));
        control.verify();
    }
    
    @Test
    public void testSetEndpointPolicyInfo() {
        engine = new PolicyEngine();
        Endpoint endpoint = control.createMock(Endpoint.class);
        EndpointPolicyInfo epi = control.createMock(EndpointPolicyInfo.class);
        engine.setEndpointPolicyInfo(endpoint, epi);
        assertSame(epi, engine.getEndpointPolicyInfo(endpoint, (Conduit)null));
        assertSame(epi, engine.getEndpointPolicyInfo(endpoint, (Destination)null)); 
    }
    
    @Test
    public void testGetServerResponsePolicyInfo() throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("createOutPolicyInfo", new Class[] {});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        engine.init();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class); 
        AssertingDestination destination = control.createMock(AssertingDestination.class);
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        EasyMock.expect(engine.createOutPolicyInfo()).andReturn(opi);
        opi.initialise(endpoint, boi, engine, destination, false);
        EasyMock.expectLastCall();
        control.replay();
        assertSame(opi, engine.getServerResponsePolicyInfo(endpoint, boi, destination));
        assertSame(opi, engine.getServerResponsePolicyInfo(endpoint, boi, destination));
        control.verify();
    }
    
    @Test
    public void testSetServerResponsePolicyInfo() {
        engine = new PolicyEngine();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        engine.setServerResponsePolicyInfo(endpoint, boi, opi);
        assertSame(opi, engine.getServerResponsePolicyInfo(endpoint, boi, (Destination)null));   
    }
   
    @Test
    public void testGetServerFaultPolicyInfo() throws NoSuchMethodException {
        Method m = PolicyEngine.class.getDeclaredMethod("createOutPolicyInfo", new Class[] {});
        engine = control.createMock(PolicyEngine.class, new Method[] {m});
        engine.init();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingFaultInfo bfi = control.createMock(BindingFaultInfo.class); 
        AssertingDestination destination = control.createMock(AssertingDestination.class);
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        EasyMock.expect(engine.createOutPolicyInfo()).andReturn(opi);
        opi.initialise(endpoint, bfi, engine, destination);
        EasyMock.expectLastCall();
        control.replay();
        assertSame(opi, engine.getServerFaultPolicyInfo(endpoint, bfi, destination));
        assertSame(opi, engine.getServerFaultPolicyInfo(endpoint, bfi, destination));
        control.verify();
    }
    
    @Test
    public void testSetServerFaultPolicyInfo() {
        engine = new PolicyEngine();
        Endpoint endpoint = control.createMock(Endpoint.class);
        BindingFaultInfo bfi = control.createMock(BindingFaultInfo.class);
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class);
        engine.setServerFaultPolicyInfo(endpoint, bfi, opi);
        assertSame(opi, engine.getServerFaultPolicyInfo(endpoint, bfi, (Destination)null));   
    }
    
    private Set<String> getInterceptorIds(List<Interceptor> interceptors) {
        Set<String> ids = new HashSet<String>();
        for (Interceptor i : interceptors) {
            ids.add(((PhaseInterceptor)i).getId());
        }
        return ids;
    }
    
    interface AssertingConduit extends Assertor, Conduit {
    }
    
    interface AssertingDestination extends Assertor, Destination {
    }
    
    
}
