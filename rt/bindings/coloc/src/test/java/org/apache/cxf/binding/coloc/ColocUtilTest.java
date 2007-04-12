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
package org.apache.cxf.binding.coloc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.phase.PhaseManagerImpl;
import org.apache.cxf.service.Service;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColocUtilTest extends Assert {
    private IMocksControl control = EasyMock.createNiceControl();
    private Bus bus;

    @Before
    public void setUp() throws Exception {
        bus = control.createMock(Bus.class);
        BusFactory.setDefaultBus(bus);
    }

    @After
    public void tearDown() throws Exception {
        BusFactory.setDefaultBus(null);
    }

    @Test
    public void testSetColocInPhases() throws Exception {
        PhaseManagerImpl phaseMgr = new PhaseManagerImpl();
        List<Phase> list = phaseMgr.getInPhases();
        int size1 = list.size();
        ColocUtil.setPhases(list, Phase.USER_LOGICAL, Phase.INVOKE);

        assertNotSame("The list size should not be same",
                      size1, list.size());
        assertEquals("Expecting Phase.USER_LOGICAL",
                     list.get(0).getName(),
                     Phase.USER_LOGICAL);
        assertEquals("Expecting Phase.POST_INVOKE",
                     list.get(list.size() - 1).getName(),
                     Phase.INVOKE);
    }

    @Test
    public void testSetColocOutPhases() throws Exception {
        PhaseManagerImpl phaseMgr = new PhaseManagerImpl();

        List<Phase> list = phaseMgr.getOutPhases();
        int size1 = list.size();
        ColocUtil.setPhases(list, Phase.SETUP, Phase.POST_LOGICAL);

        assertNotSame("The list size should not be same",
                      size1, list.size());
        assertEquals("Expecting Phase.SETUP",
                     list.get(0).getName(),
                     Phase.SETUP);
        assertEquals("Expecting Phase.POST_LOGICAL",
                     list.get(list.size() - 1).getName(),
                     Phase.POST_LOGICAL);

    }
    
    @Test
    public void testGetOutInterceptorChain() throws Exception {
        PhaseManagerImpl phaseMgr = new PhaseManagerImpl();
        List<Phase> list = phaseMgr.getInPhases();
        ColocUtil.setPhases(list, Phase.SETUP, Phase.POST_LOGICAL);
        
        Endpoint ep = control.createMock(Endpoint.class);
        Service srv = control.createMock(Service.class);
        Exchange ex = new ExchangeImpl();
        
        ex.put(Bus.class, bus);
        ex.put(Endpoint.class, ep);
        ex.put(Service.class, srv);
        
        EasyMock.expect(ep.getOutInterceptors()).andReturn(new ArrayList<Interceptor>()).atLeastOnce();
        EasyMock.expect(ep.getService()).andReturn(srv).atLeastOnce();
        EasyMock.expect(srv.getOutInterceptors()).andReturn(new ArrayList<Interceptor>()).atLeastOnce();
        EasyMock.expect(bus.getOutInterceptors()).andReturn(new ArrayList<Interceptor>()).atLeastOnce();
        
        control.replay();
        InterceptorChain chain = ColocUtil.getOutInterceptorChain(ex, list);
        control.verify();
        assertNotNull("Should have chain instance", chain);
        Iterator<Interceptor<? extends Message>> iter = chain.iterator();
        assertEquals("Should not have interceptors in chain",
                     false,
                     iter.hasNext());
    }

    @Test
    public void testGetInInterceptorChain() throws Exception {
        PhaseManagerImpl phaseMgr = new PhaseManagerImpl();
        List<Phase> list = phaseMgr.getInPhases();
        ColocUtil.setPhases(list, Phase.SETUP, Phase.POST_LOGICAL);
        
        Endpoint ep = control.createMock(Endpoint.class);
        Service srv = control.createMock(Service.class);
        Exchange ex = new ExchangeImpl();
        
        ex.put(Bus.class, bus);
        ex.put(Endpoint.class, ep);
        ex.put(Service.class, srv);
        
        EasyMock.expect(bus.getExtension(PhaseManager.class)).andReturn(phaseMgr);
        EasyMock.expect(ep.getInInterceptors()).andReturn(new ArrayList<Interceptor>()).atLeastOnce();
        EasyMock.expect(ep.getService()).andReturn(srv).atLeastOnce();
        EasyMock.expect(srv.getInInterceptors()).andReturn(new ArrayList<Interceptor>()).atLeastOnce();
        EasyMock.expect(bus.getInInterceptors()).andReturn(new ArrayList<Interceptor>()).atLeastOnce();
        
        control.replay();
        InterceptorChain chain = ColocUtil.getInInterceptorChain(ex, list);
        control.verify();
        assertNotNull("Should have chain instance", chain);
        Iterator<Interceptor<? extends Message>> iter = chain.iterator();
        assertEquals("Should not have interceptors in chain",
                     false,
                     iter.hasNext());
        assertNotNull("OutFaultObserver should be set", chain.getFaultObserver());
    }
    
}
