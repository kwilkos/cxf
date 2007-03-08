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
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.ws.policy.builder.primitive.PrimitiveAssertion;
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
public class OutPolicyInfoTest extends Assert {

    private IMocksControl control;
    
    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();        
    } 
    
    @Test
    public void testAccessors() {
        OutPolicyInfo opi = new OutPolicyInfo();
        assertNull(opi.getPolicy());
        assertNull(opi.getChosenAlternative());
        assertNull(opi.getInterceptors());
        
        Policy p = control.createMock(Policy.class);
        Assertion a = control.createMock(Assertion.class);
        List<Assertion> la = Collections.singletonList(a);
        Interceptor i = control.createMock(Interceptor.class);
        List<Interceptor> li = Collections.singletonList(i);
        control.replay();
        opi.setPolicy(p);
        assertSame(p, opi.getPolicy());
        opi.setChosenAlternative(la);
        assertSame(la, opi.getChosenAlternative());
        opi.setInterceptors(li);
        assertSame(li, opi.getInterceptors());
        control.verify();
    }
    
    @Test
    public void testInitialise() throws NoSuchMethodException {
        Method m1 = OutPolicyInfo.class.getDeclaredMethod("initialisePolicy",
            new Class[] {Endpoint.class, BindingOperationInfo.class, PolicyEngine.class, boolean.class});
        Method m2 = OutPolicyInfo.class.getDeclaredMethod("chooseAlternative",
            new Class[] {PolicyEngine.class, Assertor.class});
        Method m3 = OutPolicyInfo.class.getDeclaredMethod("initialiseInterceptors",
                                                          new Class[] {PolicyEngine.class});
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class, new Method[] {m1, m2, m3});
        
        Endpoint e = control.createMock(Endpoint.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        PolicyEngine pe = control.createMock(PolicyEngine.class);
        Assertor a = control.createMock(Assertor.class);
        boolean requestor = true;
       
        opi.initialisePolicy(e, boi, pe, requestor);
        EasyMock.expectLastCall();
        opi.chooseAlternative(pe, a);
        EasyMock.expectLastCall();
        opi.initialiseInterceptors(pe);
        EasyMock.expectLastCall();
        
        control.replay();
        opi.initialise(e, boi, pe, a, requestor);
        control.verify();        
    }
    
    @Test
    public void testInitialiseFault() throws NoSuchMethodException {
        Method m1 = OutPolicyInfo.class.getDeclaredMethod("initialisePolicy",
            new Class[] {Endpoint.class, BindingFaultInfo.class, PolicyEngine.class});
        Method m2 = OutPolicyInfo.class.getDeclaredMethod("chooseAlternative",
            new Class[] {PolicyEngine.class, Assertor.class});
        Method m3 = OutPolicyInfo.class.getDeclaredMethod("initialiseInterceptors",
                                                          new Class[] {PolicyEngine.class});
        OutPolicyInfo opi = control.createMock(OutPolicyInfo.class, new Method[] {m1, m2, m3});
        
        Endpoint e = control.createMock(Endpoint.class);
        BindingFaultInfo bfi = control.createMock(BindingFaultInfo.class);
        PolicyEngine pe = control.createMock(PolicyEngine.class);
        Assertor a = control.createMock(Assertor.class);
       
        opi.initialisePolicy(e, bfi, pe);
        EasyMock.expectLastCall();
        opi.chooseAlternative(pe, a);
        EasyMock.expectLastCall();
        opi.initialiseInterceptors(pe);
        EasyMock.expectLastCall();
        
        control.replay();
        opi.initialise(e, bfi, pe, a);
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
        Endpoint e = control.createMock(Endpoint.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        PolicyEngine engine = control.createMock(PolicyEngine.class);
        BindingMessageInfo bmi = control.createMock(BindingMessageInfo.class);
        if (requestor) {
            EasyMock.expect(boi.getInput()).andReturn(bmi);
        } else {
            EasyMock.expect(boi.getOutput()).andReturn(bmi);
        }
                
        EndpointPolicyInfo epi = control.createMock(EndpointPolicyInfo.class);
        if (requestor) {
            EasyMock.expect(engine.getEndpointPolicyInfo(e, (Conduit)null)).andReturn(epi);
        } else {
            EasyMock.expect(engine.getEndpointPolicyInfo(e, (Destination)null)).andReturn(epi);
        }
        Policy ep = control.createMock(Policy.class);
        EasyMock.expect(epi.getPolicy()).andReturn(ep);        
        Policy op = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedOperationPolicy(boi)).andReturn(op);
        Policy merged = control.createMock(Policy.class);
        EasyMock.expect(ep.merge(op)).andReturn(merged);
        Policy mp = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedMessagePolicy(bmi)).andReturn(mp);
        EasyMock.expect(merged.merge(mp)).andReturn(merged);
        EasyMock.expect(merged.normalize(true)).andReturn(merged);
        
        control.replay();
        OutPolicyInfo opi = new OutPolicyInfo();
        opi.initialisePolicy(e, boi, engine, requestor);
        assertSame(merged, opi.getPolicy());
        control.verify();
    }
    
    @Test
    public void testInitialiseServerFaultPolicy() {        
        Endpoint e = control.createMock(Endpoint.class);
        BindingFaultInfo bfi = control.createMock(BindingFaultInfo.class);
        PolicyEngine engine = control.createMock(PolicyEngine.class);
        
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        EasyMock.expect(bfi.getBindingOperation()).andReturn(boi);               
        EndpointPolicyInfo epi = control.createMock(EndpointPolicyInfo.class);        
        EasyMock.expect(engine.getEndpointPolicyInfo(e, (Destination)null)).andReturn(epi);
        Policy ep = control.createMock(Policy.class);
        EasyMock.expect(epi.getPolicy()).andReturn(ep);        
        Policy op = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedOperationPolicy(boi)).andReturn(op);
        Policy merged = control.createMock(Policy.class);
        EasyMock.expect(ep.merge(op)).andReturn(merged);
        Policy fp = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedFaultPolicy(bfi)).andReturn(fp);
        EasyMock.expect(merged.merge(fp)).andReturn(merged);
        EasyMock.expect(merged.normalize(true)).andReturn(merged);
        
        control.replay();
        OutPolicyInfo opi = new OutPolicyInfo();
        opi.initialisePolicy(e, bfi, engine);
        assertSame(merged, opi.getPolicy());
        control.verify();
    }
    
    @Test
    public void testChooseAlternative() {
        OutPolicyInfo cpi = new OutPolicyInfo();
        cpi.setPolicy(new Policy());
        
        PolicyEngine engine = control.createMock(PolicyEngine.class);
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
        OutPolicyInfo cpi = new OutPolicyInfo();        
        List<Assertion> alternative = new ArrayList<Assertion>();
        cpi.setChosenAlternative(alternative);
        
        PolicyEngine engine = control.createMock(PolicyEngine.class);
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
    
    private void setupPolicyInterceptorProviderRegistry(PolicyEngine engine, 
                                                        PolicyInterceptorProviderRegistry reg) {
        Bus bus = control.createMock(Bus.class);        
        EasyMock.expect(engine.getBus()).andReturn(bus);
        EasyMock.expect(bus.getExtension(PolicyInterceptorProviderRegistry.class)).andReturn(reg);
    }
    
    @Test
    public void testCheckEffectivePolicy() {
        OutPolicyInfo opi = new OutPolicyInfo();  
        Policy p = new Policy();
        QName aqn = new QName("http://x.y.z", "a");
        Assertion a = new PrimitiveAssertion(aqn);
        QName bqn = new QName("http://x.y.z", "b");
        Assertion b = new PrimitiveAssertion(bqn);
        QName cqn = new QName("http://x.y.z", "c");
        Assertion c = new PrimitiveAssertion(cqn);
        All alt1 = new All();
        alt1.addAssertion(a);
        alt1.addAssertion(b);
        All alt2 = new All();
        alt2.addAssertion(c);
        ExactlyOne ea = new ExactlyOne();
        ea.addPolicyComponent(alt1);
        ea.addPolicyComponent(alt2);
        p.addPolicyComponent(ea);   
        AssertionInfoMap aim = new AssertionInfoMap(CastUtils.cast(Collections.EMPTY_LIST, Assertion.class));
        AssertionInfo ai = new AssertionInfo(a);
        AssertionInfo bi = new AssertionInfo(b);
        AssertionInfo ci = new AssertionInfo(c);
        aim.put(aqn, Collections.singleton(ai));
        aim.put(bqn, Collections.singleton(bi));
        aim.put(cqn, Collections.singleton(ci));
        opi.setPolicy(p);
        
        try {
            opi.checkEffectivePolicy(aim);
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
        
        ai.setAsserted(true);
        ci.setAsserted(true);
        
        opi.checkEffectivePolicy(aim);
    }
    
    @Test
    public void testAlternativeSupported() {
        Assertion a1 = control.createMock(Assertion.class);
        QName aqn = new QName("http://x.y.z", "a");
        EasyMock.expect(a1.getName()).andReturn(aqn).anyTimes();
        Assertion a2 = control.createMock(Assertion.class);
        EasyMock.expect(a2.getName()).andReturn(aqn).anyTimes();
        Assertion b = control.createMock(Assertion.class);
        QName bqn = new QName("http://x.y.z", "b");
        EasyMock.expect(b.getName()).andReturn(bqn).anyTimes();
        Assertion c = control.createMock(Assertion.class);
        QName cqn = new QName("http://x.y.z", "c");
        EasyMock.expect(c.getName()).andReturn(cqn).anyTimes();
        AssertionInfoMap aim = new AssertionInfoMap(CastUtils.cast(Collections.EMPTY_LIST, Assertion.class));
        AssertionInfo ai1 = new AssertionInfo(a1);
        AssertionInfo ai2 = new AssertionInfo(a2);
        Collection<AssertionInfo> ais = new ArrayList<AssertionInfo>();
        AssertionInfo bi = new AssertionInfo(b);
        AssertionInfo ci = new AssertionInfo(c);
        ais.add(ai1);
        ais.add(ai2);
        aim.put(aqn, ais);
        aim.put(bqn, Collections.singleton(bi));
        aim.put(cqn, Collections.singleton(ci));
        ai2.setAsserted(true);
        bi.setAsserted(true);
        ci.setAsserted(true);
        EasyMock.expect(a1.equal(a1)).andReturn(true).anyTimes();
        EasyMock.expect(a2.equal(a2)).andReturn(true).anyTimes();
        EasyMock.expect(b.equal(b)).andReturn(true).anyTimes();
        EasyMock.expect(c.equal(c)).andReturn(true).anyTimes();
        
        
        List<Assertion> alt1 = new ArrayList<Assertion>();
        alt1.add(a1);
        alt1.add(b);
        
        List<Assertion> alt2 = new ArrayList<Assertion>();
        alt2.add(a2);
        alt2.add(c);
                
        OutPolicyInfo opi = new OutPolicyInfo();
        control.replay();
        assertTrue(!opi.alternativeSupported(alt1, aim));
        assertTrue(opi.alternativeSupported(alt2, aim));
        control.verify();     
    }  
}
