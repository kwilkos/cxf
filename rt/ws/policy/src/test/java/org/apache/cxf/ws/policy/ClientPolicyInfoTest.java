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
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
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
        ClientRequestPolicyInfo crpi = new ClientRequestPolicyInfo();
        Policy p = control.createMock(Policy.class);
        Assertion a = control.createMock(Assertion.class);
        List<Assertion> la = Collections.singletonList(a);
        Interceptor i = control.createMock(Interceptor.class);
        List<Interceptor> li = Collections.singletonList(i);
        control.replay();
        crpi.setRequestPolicy(p);
        assertSame(p, crpi.getRequestPolicy());
        crpi.setChosenAlternative(la);
        assertSame(la, crpi.getChosenAlternative());
        crpi.setOutInterceptors(li);
        assertSame(li, crpi.getOutInterceptors());
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
        ClientRequestPolicyInfo cpi = new ClientRequestPolicyInfo();
        cpi.initialiseRequestPolicy(boi, ei, engine);
        assertSame(merged, cpi.getRequestPolicy());
        control.verify();
    }
    
    public void testChooseAlternative() {
        ClientRequestPolicyInfo cpi = new ClientRequestPolicyInfo();
        cpi.setRequestPolicy(new Policy());
        
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
        cpi.setRequestPolicy(policy);
        
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
        EasyMock.expect(engine.supportsAlternative(firstAlternative, assertor)).andReturn(false);
        EasyMock.expect(engine.supportsAlternative(secondAlternative, assertor)).andReturn(true);
        control.replay();        
        cpi.chooseAlternative(engine, assertor); 
        chosen = cpi.getChosenAlternative();
        assertSame(1, chosen.size());
        assertSame(chosen.size(), secondAlternative.size());
        assertSame(chosen.get(0), secondAlternative.get(0));
        control.verify();
    }
    
    public void testInitialiseOutInterceptors() {
        ClientRequestPolicyInfo cpi = new ClientRequestPolicyInfo();        
        List<Assertion> alternative = new ArrayList<Assertion>();
        cpi.setChosenAlternative(alternative);
        
        PolicyEngine engine = control.createMock(PolicyEngine.class);
        PolicyInterceptorProviderRegistry reg = control.createMock(PolicyInterceptorProviderRegistry.class);
        setupPolicyInterceptorProviderRegistry(engine, reg);
        
        control.replay();
        cpi.initialiseInterceptors(engine);
        assertEquals(0, cpi.getOutInterceptors().size());
        control.verify();
        
        control.reset();
        setupPolicyInterceptorProviderRegistry(engine, reg);
        Assertion a = control.createMock(Assertion.class);        
        alternative.add(a);
        EasyMock.expect(a.isOptional()).andReturn(true);
        control.replay();
        cpi.initialiseInterceptors(engine);
        assertEquals(0, cpi.getOutInterceptors().size());
        control.verify();
        
        control.reset();
        setupPolicyInterceptorProviderRegistry(engine, reg);
        EasyMock.expect(a.isOptional()).andReturn(false);
        QName qn = new QName("http://x.y.z", "a");
        EasyMock.expect(a.getName()).andReturn(qn);
        EasyMock.expect(reg.get(qn)).andReturn(null);
        control.replay();
        cpi.initialiseInterceptors(engine);
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
        cpi.initialiseInterceptors(engine);
        assertEquals(1, cpi.getOutInterceptors().size());
        assertSame(pi, cpi.getOutInterceptors().get(0));
        control.verify();     
    }
    
    private void setupPolicyInterceptorProviderRegistry(PolicyEngine engine, 
                                                        PolicyInterceptorProviderRegistry reg) {
        Bus bus = control.createMock(Bus.class);        
        EasyMock.expect(engine.getBus()).andReturn(bus);
        EasyMock.expect(bus.getExtension(PolicyInterceptorProviderRegistry.class)).andReturn(reg);
    }
   
    
    
    
   
}
