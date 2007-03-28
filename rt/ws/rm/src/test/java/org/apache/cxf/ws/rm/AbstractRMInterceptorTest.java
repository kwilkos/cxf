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

package org.apache.cxf.ws.rm;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.cxf.Bus;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.neethi.Assertion;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class AbstractRMInterceptorTest extends Assert {

    private IMocksControl control;

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
    }

    @After
    public void tearDown() {
        control.verify();
    }

    @Test
    public void testAccessors() {
        RMInterceptor interceptor = new RMInterceptor();
        assertEquals(Phase.PRE_LOGICAL, interceptor.getPhase());
        Bus bus = control.createMock(Bus.class);
        RMManager busMgr = control.createMock(RMManager.class);
        EasyMock.expect(bus.getExtension(RMManager.class)).andReturn(busMgr);
        RMManager mgr = control.createMock(RMManager.class);
        
        control.replay();
        assertNull(interceptor.getBus());
        interceptor.setBus(bus);
        assertSame(bus, interceptor.getBus());
        assertSame(busMgr, interceptor.getManager());
        interceptor.setManager(mgr);
        assertSame(mgr, interceptor.getManager());
    }
    
    @Test
    public void testHandleMessage() {
        RMInterceptor interceptor = new RMInterceptor();
        Message message = control.createMock(Message.class);
        control.replay();
        interceptor.handleMessage(message);
        interceptor.setThrow(true);
        try {
            interceptor.handleMessage(message);
            fail("Expected Fault not thrown.");
        } catch (Fault f) {
            assertTrue(f.getCause() instanceof SequenceFault);
        }
    }
    
    @Test
    public void testAssertReliability() {
        RMInterceptor interceptor = new RMInterceptor();
        Message message = control.createMock(Message.class);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(null);
        AssertionInfoMap aim = control.createMock(AssertionInfoMap.class);
        Collection<AssertionInfo> ais = new ArrayList<AssertionInfo>();
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(aim).times(2);
        Assertion a = control.createMock(Assertion.class);        
        AssertionInfo ai = new AssertionInfo(a);
        EasyMock.expectLastCall();
        control.replay();
        interceptor.assertReliability(message);
        assertTrue(!ai.isAsserted());
        aim.put(RMConstants.getRMAssertionQName(), ais);
        interceptor.assertReliability(message);
        assertTrue(!ai.isAsserted());
        ais.add(ai);
        interceptor.assertReliability(message);     
    }

    class RMInterceptor extends AbstractRMInterceptor {

        private boolean doThrow;
        
        void setThrow(boolean t) {
            doThrow = t;
        }
        
        @Override
        protected void handle(Message msg) throws SequenceFault {
            if (doThrow) {
                throw new SequenceFault("");
            }
        }     
    }
}
