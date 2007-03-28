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
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.neethi.All;
import org.apache.neethi.Assertion;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class EffectivePolicyImplTest extends Assert {

    private IMocksControl control;
    
    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
        new Integer(4);
    } 
    
    @Test
    public void testAccessors() {
        EffectivePolicyImpl effectivePolicy = new EffectivePolicyImpl();
        assertNull(effectivePolicy.getPolicy());
        assertNull(effectivePolicy.getChosenAlternative());
        assertNull(effectivePolicy.getInterceptors());
        
        Policy p = control.createMock(Policy.class);
        Assertion a = control.createMock(Assertion.class);
        List<Assertion> la = Collections.singletonList(a);
        Interceptor i = control.createMock(Interceptor.class);
        List<Interceptor> li = Collections.singletonList(i);
        control.replay();
        effectivePolicy.setPolicy(p);
        assertSame(p, effectivePolicy.getPolicy());
        effectivePolicy.setChosenAlternative(la);
        assertSame(la, effectivePolicy.getChosenAlternative());
        effectivePolicy.setInterceptors(li);
        assertSame(li, effectivePolicy.getInterceptors());
        control.verify();
    }
    
    @Test
    public void testInitialiseFromEndpointPolicy() throws NoSuchMethodException {
        Method m = EffectivePolicyImpl.class.getDeclaredMethod("initialiseInterceptors",
                                                          new Class[] {PolicyEngineImpl.class});
        EffectivePolicyImpl effectivePolicy = control.createMock(EffectivePolicyImpl.class, new Method[] {m});
        EndpointPolicyImpl endpointPolicy = control.createMock(EndpointPolicyImpl.class);
        Policy p = control.createMock(Policy.class);
        EasyMock.expect(endpointPolicy.getPolicy()).andReturn(p);
        Collection<Assertion> chosenAlternative = new ArrayList<Assertion>();
        EasyMock.expect(endpointPolicy.getChosenAlternative()).andReturn(chosenAlternative);
        PolicyEngineImpl pe = control.createMock(PolicyEngineImpl.class);
        effectivePolicy.initialiseInterceptors(pe);
        EasyMock.expectLastCall();
        control.replay();
        effectivePolicy.initialise(endpointPolicy, pe);
        control.verify();    
    }
    
    @Test
    public void testInitialise() throws NoSuchMethodException {
        Method m1 = EffectivePolicyImpl.class.getDeclaredMethod("initialisePolicy",
            new Class[] {EndpointInfo.class, BindingOperationInfo.class, PolicyEngineImpl.class, 
                         boolean.class});
        Method m2 = EffectivePolicyImpl.class.getDeclaredMethod("chooseAlternative",
            new Class[] {PolicyEngineImpl.class, Assertor.class});
        Method m3 = EffectivePolicyImpl.class.getDeclaredMethod("initialiseInterceptors",
                                                          new Class[] {PolicyEngineImpl.class});
        EffectivePolicyImpl effectivePolicy = 
            control.createMock(EffectivePolicyImpl.class, new Method[] {m1, m2, m3});        
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        PolicyEngineImpl pe = control.createMock(PolicyEngineImpl.class);
        Assertor a = control.createMock(Assertor.class);
        boolean requestor = true;
       
        effectivePolicy.initialisePolicy(ei, boi, pe, requestor);
        EasyMock.expectLastCall();
        effectivePolicy.chooseAlternative(pe, a);
        EasyMock.expectLastCall();
        effectivePolicy.initialiseInterceptors(pe);
        EasyMock.expectLastCall();
        
        control.replay();
        effectivePolicy.initialise(ei, boi, pe, a, requestor);
        control.verify();        
    }
    
    @Test
    public void testInitialiseFault() throws NoSuchMethodException {
        Method m1 = EffectivePolicyImpl.class.getDeclaredMethod("initialisePolicy",
            new Class[] {EndpointInfo.class, BindingFaultInfo.class, PolicyEngineImpl.class});
        Method m2 = EffectivePolicyImpl.class.getDeclaredMethod("chooseAlternative",
            new Class[] {PolicyEngineImpl.class, Assertor.class});
        Method m3 = EffectivePolicyImpl.class.getDeclaredMethod("initialiseInterceptors",
                                                          new Class[] {PolicyEngineImpl.class});
        EffectivePolicyImpl effectivePolicy = 
            control.createMock(EffectivePolicyImpl.class, new Method[] {m1, m2, m3});        
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        BindingFaultInfo bfi = control.createMock(BindingFaultInfo.class);
        PolicyEngineImpl pe = control.createMock(PolicyEngineImpl.class);
        Assertor a = control.createMock(Assertor.class);
       
        effectivePolicy.initialisePolicy(ei, bfi, pe);
        EasyMock.expectLastCall();
        effectivePolicy.chooseAlternative(pe, a);
        EasyMock.expectLastCall();
        effectivePolicy.initialiseInterceptors(pe);
        EasyMock.expectLastCall();
        
        control.replay();
        effectivePolicy.initialise(ei, bfi, pe, a);
        control.verify();        
    }
    
    @Test
    public void testInitialiseClientPolicy() {  
        doTestInitialisePolicy(true);
    }
    
    @Test
    public void testInitialiseServerPolicy() {  
        doTestInitialisePolicy(false);
    }
    
    private void doTestInitialisePolicy(boolean requestor) {        
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        PolicyEngineImpl engine = control.createMock(PolicyEngineImpl.class);
        BindingMessageInfo bmi = control.createMock(BindingMessageInfo.class);
        if (requestor) {
            EasyMock.expect(boi.getInput()).andReturn(bmi);
        } else {
            EasyMock.expect(boi.getOutput()).andReturn(bmi);
        }
                
        EndpointPolicy effectivePolicy = control.createMock(EndpointPolicy.class);
        if (requestor) {
            EasyMock.expect(engine.getClientEndpointPolicy(ei, (Conduit)null)).andReturn(effectivePolicy);
        } else {
            EasyMock.expect(engine.getServerEndpointPolicy(ei, (Destination)null)).andReturn(effectivePolicy);
        }
        Policy ep = control.createMock(Policy.class);
        EasyMock.expect(effectivePolicy.getPolicy()).andReturn(ep);        
        Policy op = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedOperationPolicy(boi)).andReturn(op);
        Policy merged = control.createMock(Policy.class);
        EasyMock.expect(ep.merge(op)).andReturn(merged);
        Policy mp = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedMessagePolicy(bmi)).andReturn(mp);
        EasyMock.expect(merged.merge(mp)).andReturn(merged);
        EasyMock.expect(merged.normalize(true)).andReturn(merged);
        
        control.replay();
        EffectivePolicyImpl epi = new EffectivePolicyImpl();
        epi.initialisePolicy(ei, boi, engine, requestor);
        assertSame(merged, epi.getPolicy());
        control.verify();
    }
    
    @Test
    public void testInitialiseServerFaultPolicy() {        
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        BindingFaultInfo bfi = control.createMock(BindingFaultInfo.class);
        PolicyEngineImpl engine = control.createMock(PolicyEngineImpl.class);
        
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        EasyMock.expect(bfi.getBindingOperation()).andReturn(boi);               
        EndpointPolicy endpointPolicy = control.createMock(EndpointPolicy.class);        
        EasyMock.expect(engine.getServerEndpointPolicy(ei, (Destination)null)).andReturn(endpointPolicy);
        Policy ep = control.createMock(Policy.class);
        EasyMock.expect(endpointPolicy.getPolicy()).andReturn(ep);        
        Policy op = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedOperationPolicy(boi)).andReturn(op);
        Policy merged = control.createMock(Policy.class);
        EasyMock.expect(ep.merge(op)).andReturn(merged);
        Policy fp = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedFaultPolicy(bfi)).andReturn(fp);
        EasyMock.expect(merged.merge(fp)).andReturn(merged);
        EasyMock.expect(merged.normalize(true)).andReturn(merged);
        
        control.replay();
        EffectivePolicyImpl epi = new EffectivePolicyImpl();
        epi.initialisePolicy(ei, bfi, engine);
        assertSame(merged, epi.getPolicy());
        control.verify();
    }
    
    @Test
    public void testChooseAlternative() {
        EffectivePolicyImpl cpi = new EffectivePolicyImpl();
        cpi.setPolicy(new Policy());
        
        PolicyEngineImpl engine = control.createMock(PolicyEngineImpl.class);
        Assertor assertor = control.createMock(Assertor.class);
               
        Policy policy = new Policy();
        ExactlyOne ea = new ExactlyOne();
        All all = new All();
        Assertion a1 = new TestAssertion(); 
        all.addAssertion(a1);
        ea.addPolicyComponent(all);
        List<Assertion> firstAlternative = CastUtils.cast(all.getPolicyComponents(), Assertion.class);
        policy.addPolicyComponent(ea);
        cpi.setPolicy(policy);
        
        EasyMock.expect(engine.supportsAlternative(firstAlternative, assertor)).andReturn(false);
        control.replay();
        try {
            cpi.chooseAlternative(engine, assertor);  
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
        control.verify();
        
        control.reset();        
        EasyMock.expect(engine.supportsAlternative(firstAlternative, assertor)).andReturn(true);
        control.replay();        
        cpi.chooseAlternative(engine, assertor); 
        
        Collection<Assertion> chosen = cpi.getChosenAlternative();
        assertSame(1, chosen.size());
        assertSame(chosen.size(), firstAlternative.size());
        assertSame(chosen.iterator().next(), firstAlternative.get(0));
        
        // assertSame(cpi.getChosenAlternative(), firstAlternative);
        control.verify();
        
        control.reset();
        All other = new All();
        other.addAssertion(a1);
        ea.addPolicyComponent(other);
        List<Assertion> secondAlternative = CastUtils.cast(other.getPolicyComponents(), Assertion.class);
        EasyMock.expect(engine.supportsAlternative(firstAlternative, assertor)).andReturn(false);
        EasyMock.expect(engine.supportsAlternative(secondAlternative, assertor)).andReturn(true);
        control.replay();        
        cpi.chooseAlternative(engine, assertor); 
        chosen = cpi.getChosenAlternative();
        assertSame(1, chosen.size());
        assertSame(chosen.size(), secondAlternative.size());
        assertSame(chosen.iterator().next(), secondAlternative.get(0));
        control.verify();
    }
    
    @Test
    public void testInitialiseOutInterceptors() {
        EffectivePolicyImpl cpi = new EffectivePolicyImpl();        
        List<Assertion> alternative = new ArrayList<Assertion>();
        cpi.setChosenAlternative(alternative);
        
        PolicyEngineImpl engine = control.createMock(PolicyEngineImpl.class);
        PolicyInterceptorProviderRegistry reg = control.createMock(PolicyInterceptorProviderRegistry.class);
        setupPolicyInterceptorProviderRegistry(engine, reg);
        
        control.replay();
        cpi.initialiseInterceptors(engine);
        assertEquals(0, cpi.getInterceptors().size());
        control.verify();
        
        control.reset();
        setupPolicyInterceptorProviderRegistry(engine, reg);
        Assertion a = control.createMock(Assertion.class);        
        alternative.add(a);
        EasyMock.expect(a.isOptional()).andReturn(true);
        control.replay();
        cpi.initialiseInterceptors(engine);
        assertEquals(0, cpi.getInterceptors().size());
        control.verify();
        
        control.reset();
        setupPolicyInterceptorProviderRegistry(engine, reg);
        EasyMock.expect(a.isOptional()).andReturn(false);
        QName qn = new QName("http://x.y.z", "a");
        EasyMock.expect(a.getName()).andReturn(qn);
        EasyMock.expect(reg.get(qn)).andReturn(null);
        control.replay();
        cpi.initialiseInterceptors(engine);
        assertEquals(0, cpi.getInterceptors().size());
        control.verify();
        
        control.reset();
        setupPolicyInterceptorProviderRegistry(engine, reg);
        EasyMock.expect(a.isOptional()).andReturn(false);
        EasyMock.expect(a.getName()).andReturn(qn);        
        PolicyInterceptorProvider pp = control.createMock(PolicyInterceptorProvider.class);               
        EasyMock.expect(reg.get(qn)).andReturn(pp);
        Interceptor pi = control.createMock(Interceptor.class);
        EasyMock.expect(pp.getOutInterceptors()).andReturn(Collections.singletonList(pi));
        control.replay();
        cpi.initialiseInterceptors(engine);
        assertEquals(1, cpi.getInterceptors().size());
        assertSame(pi, cpi.getInterceptors().get(0));
        control.verify();     
    }
    
    private void setupPolicyInterceptorProviderRegistry(PolicyEngineImpl engine, 
                                                        PolicyInterceptorProviderRegistry reg) {
        Bus bus = control.createMock(Bus.class);        
        EasyMock.expect(engine.getBus()).andReturn(bus);
        EasyMock.expect(bus.getExtension(PolicyInterceptorProviderRegistry.class)).andReturn(reg);
    }
    
}
