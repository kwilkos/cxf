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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.neethi.All;
import org.apache.neethi.Assertion;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * 
 */
public class ClientPolicyInfoTest extends TestCase {

    private IMocksControl control;
    
    public void setUp() {
        control = EasyMock.createNiceControl();        
    } 
    
    public void testAccessors() {
        ClientPolicyInfo cpi = new ClientPolicyInfo();
        Policy p = control.createMock(Policy.class);
        Assertion a = control.createMock(Assertion.class);
        List<Assertion> la = Collections.singletonList(a);
        control.replay();
        cpi.setRequestPolicy(p);
        assertSame(p, cpi.getRequestPolicy());
        cpi.setChosenAlternative(la);
        assertSame(la, cpi.getChosenAlternative());
        cpi.setResponsePolicy(p);
        assertSame(p, cpi.getResponsePolicy());
        control.verify();
    }
    
    public void testInitialiseRequestPolicy() {
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        ServiceInfo si = control.createMock(ServiceInfo.class);
        BindingMessageInfo bmi = control.createMock(BindingMessageInfo.class);
        PolicyEngine engine = control.createMock(PolicyEngine.class);
           
        EasyMock.expect(boi.getInput()).andReturn(bmi);
        Policy mp = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedMessagePolicy(bmi)).andReturn(mp);
        Policy op = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedOperationPolicy(boi)).andReturn(op);
        Policy merged = control.createMock(Policy.class);
        EasyMock.expect(mp.merge(op)).andReturn(merged);
        Policy ep = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedEndpointPolicy(ei)).andReturn(ep);
        EasyMock.expect(merged.merge(ep)).andReturn(merged);
        EasyMock.expect(ei.getService()).andReturn(si);
        Policy sp = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedServicePolicy(si)).andReturn(sp);
        EasyMock.expect(merged.merge(sp)).andReturn(merged);
        EasyMock.expect(merged.normalize(true)).andReturn(merged);
        
        control.replay();
        ClientPolicyInfo cpi = new ClientPolicyInfo();
        cpi.initialiseRequestPolicy(boi, ei, engine);
        assertSame(merged, cpi.getRequestPolicy());
        control.verify();
    }
    
    public void testChooseAlternative() {
        ClientPolicyInfo cpi = new ClientPolicyInfo();
        cpi.setRequestPolicy(new Policy());
        
        PolicyEngine engine = control.createMock(PolicyEngine.class);
        Conduit conduit = control.createMock(Conduit.class);
               
        Policy policy = new Policy();
        ExactlyOne ea = new ExactlyOne();
        All all = new All();
        Assertion a1 = new TestAssertion(); 
        all.addAssertion(a1);
        ea.addPolicyComponent(all);
        List<Assertion> firstAlternative = CastUtils.cast(all.getPolicyComponents(), Assertion.class);
        policy.addPolicyComponent(ea);
        cpi.setRequestPolicy(policy);
        
        EasyMock.expect(engine.supportsAlternative(firstAlternative, conduit)).andReturn(false);
        control.replay();
        try {
            cpi.chooseAlternative(conduit, engine);  
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
        control.verify();
        
        control.reset();        
        EasyMock.expect(engine.supportsAlternative(firstAlternative, conduit)).andReturn(true);
        control.replay();        
        cpi.chooseAlternative(conduit, engine); 
        
        List<Assertion> chosen = cpi.getChosenAlternative();
        assertSame(1, chosen.size());
        assertSame(chosen.size(), firstAlternative.size());
        assertSame(chosen.get(0), firstAlternative.get(0));
        
        // assertSame(cpi.getChosenAlternative(), firstAlternative);
        control.verify();
        
        control.reset();
        All other = new All();
        other.addAssertion(a1);
        ea.addPolicyComponent(other);
        List<Assertion> secondAlternative = CastUtils.cast(other.getPolicyComponents(), Assertion.class);
        EasyMock.expect(engine.supportsAlternative(firstAlternative, conduit)).andReturn(false);
        EasyMock.expect(engine.supportsAlternative(secondAlternative, conduit)).andReturn(true);
        control.replay();        
        cpi.chooseAlternative(conduit, engine); 
        chosen = cpi.getChosenAlternative();
        assertSame(1, chosen.size());
        assertSame(chosen.size(), secondAlternative.size());
        assertSame(chosen.get(0), secondAlternative.get(0));
        control.verify();
    }
    
    public void testInitialiseOutInterceptors() {
        ClientPolicyInfo cpi = new ClientPolicyInfo();        
        List<Assertion> alternative = new ArrayList<Assertion>();
        cpi.setChosenAlternative(alternative);
        
        PolicyEngine engine = control.createMock(PolicyEngine.class);
        PolicyInterceptorProviderRegistry reg = control.createMock(PolicyInterceptorProviderRegistry.class);
        setupPolicyInterceptorProviderRegistry(engine, reg);
        
        control.replay();
        cpi.initialiseOutInterceptors(engine);
        assertEquals(0, cpi.getOutInterceptors().size());
        control.verify();
        
        control.reset();
        setupPolicyInterceptorProviderRegistry(engine, reg);
        Assertion a = control.createMock(Assertion.class);        
        alternative.add(a);
        EasyMock.expect(a.isOptional()).andReturn(true);
        control.replay();
        cpi.initialiseOutInterceptors(engine);
        assertEquals(0, cpi.getOutInterceptors().size());
        control.verify();
        
        control.reset();
        setupPolicyInterceptorProviderRegistry(engine, reg);
        EasyMock.expect(a.isOptional()).andReturn(false);
        QName qn = new QName("http://x.y.z", "a");
        EasyMock.expect(a.getName()).andReturn(qn);
        EasyMock.expect(reg.get(qn)).andReturn(null);
        control.replay();
        cpi.initialiseOutInterceptors(engine);
        assertEquals(0, cpi.getOutInterceptors().size());
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
        cpi.initialiseOutInterceptors(engine);
        assertEquals(1, cpi.getOutInterceptors().size());
        assertSame(pi, cpi.getOutInterceptors().get(0));
        control.verify();     
    }

 
    public void testInitialiseResponsePolicyOn() {
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        PolicyEngine engine = control.createMock(PolicyEngine.class);
        
        EasyMock.expect(boi.getOutput()).andReturn(null);
        control.replay();
        ClientPolicyInfo cpi = new ClientPolicyInfo();
        cpi.initialiseResponsePolicy(boi, ei, engine);
        assertNull(cpi.getResponsePolicy());
        control.verify();
        control.reset();
        
        
        ServiceInfo si = control.createMock(ServiceInfo.class);
        BindingMessageInfo bmi = control.createMock(BindingMessageInfo.class);                     
        EasyMock.expect(boi.getOutput()).andReturn(bmi);        
        Policy mp = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedMessagePolicy(bmi)).andReturn(mp);
        BindingFaultInfo bfi = control.createMock(BindingFaultInfo.class);
        EasyMock.expect(boi.getFaults()).andReturn(Collections.singletonList(bfi));
        Policy fp = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedFaultPolicy(bfi)).andReturn(fp);
        Policy merged = control.createMock(Policy.class);
        EasyMock.expect(mp.merge(fp)).andReturn(merged);        
        Policy op = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedOperationPolicy(boi)).andReturn(op);    
        EasyMock.expect(merged.merge(op)).andReturn(merged);
        Policy ep = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedEndpointPolicy(ei)).andReturn(ep);
        EasyMock.expect(merged.merge(ep)).andReturn(merged);
        EasyMock.expect(ei.getService()).andReturn(si);
        Policy sp = control.createMock(Policy.class);
        EasyMock.expect(engine.getAggregatedServicePolicy(si)).andReturn(sp);
        EasyMock.expect(merged.merge(sp)).andReturn(merged);
        EasyMock.expect(merged.normalize(true)).andReturn(merged);
        
        control.replay();
        cpi = new ClientPolicyInfo();
        cpi.initialiseResponsePolicy(boi, ei, engine);
        assertSame(merged, cpi.getResponsePolicy());
        control.verify();
    }
    
    public void testInitialiseInInterceptors() {
        ClientPolicyInfo cpi = new ClientPolicyInfo();    
        Policy responsePolicy = new Policy();
        ExactlyOne ea = new ExactlyOne();
        All alt1 = new All();  
        QName qn1 = new QName("http://x.y.z", "a1");
        TestAssertion a1 = new TestAssertion(qn1);
        alt1.addAssertion(a1);
        All alt2 = new All();
        QName qn2 = new QName("http://x.y.z", "a2");
        TestAssertion a2 = new TestAssertion(qn2);
        alt2.addAssertion(a2);
        ea.addPolicyComponent(alt1);
        ea.addPolicyComponent(alt2);
        responsePolicy.addPolicyComponent(ea);        
 
        PolicyEngine engine = control.createMock(PolicyEngine.class);
        PolicyInterceptorProviderRegistry reg = control.createMock(PolicyInterceptorProviderRegistry.class);
        
        control.replay();
        cpi.initialiseInInterceptors(engine);
        assertTrue(cpi.getInInterceptors().isEmpty());
        assertTrue(cpi.getInFaultInterceptors().isEmpty());
        control.verify();
        
        control.reset();
        cpi.setResponsePolicy(responsePolicy);
        setupPolicyInterceptorProviderRegistry(engine, reg);
        PolicyInterceptorProvider p1 = control.createMock(PolicyInterceptorProvider.class);
        EasyMock.expect(reg.get(qn1)).andReturn(p1);
        PolicyInterceptorProvider p2 = control.createMock(PolicyInterceptorProvider.class);
        EasyMock.expect(reg.get(qn2)).andReturn(p2);
        Interceptor i1 = control.createMock(Interceptor.class);
        EasyMock.expect(p1.getInInterceptors()).andReturn(Collections.singletonList(i1));
        EasyMock.expect(p1.getInFaultInterceptors())
            .andReturn(CastUtils.cast(Collections.EMPTY_LIST, Interceptor.class));
        Interceptor i2 = control.createMock(Interceptor.class);
        EasyMock.expect(p2.getInInterceptors()).andReturn(Collections.singletonList(i2));
        Interceptor fi2 = control.createMock(Interceptor.class);
        EasyMock.expect(p2.getInFaultInterceptors())
            .andReturn(Collections.singletonList(fi2));
           
        control.replay();
        cpi.initialiseInInterceptors(engine);
        assertEquals(2, cpi.getInInterceptors().size());
        assertEquals(1, cpi.getInFaultInterceptors().size());
        control.verify();
    }
    
    private void setupPolicyInterceptorProviderRegistry(PolicyEngine engine, 
                                                        PolicyInterceptorProviderRegistry reg) {
        Bus bus = control.createMock(Bus.class);        
        EasyMock.expect(engine.getBus()).andReturn(bus);
        EasyMock.expect(bus.getExtension(PolicyInterceptorProviderRegistry.class)).andReturn(reg);
    }
   
    
    
    
   
}
