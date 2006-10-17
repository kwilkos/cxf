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

package org.apache.cxf.ws.rm.impl;

import java.math.BigInteger;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.ws.addressing.MAPAggregator;
import org.apache.cxf.ws.rm.RetransmissionQueue;
import org.apache.cxf.ws.rm.interceptor.SequenceTerminationPolicyType;
import org.apache.cxf.ws.rm.interceptor.SourcePolicyType;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.apache.cxf.ws.rm.policy.RMAssertion;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class RMInterceptorTest extends TestCase {
   
    public void testAccessors() {
        RMInterceptor rmi = new RMInterceptor();
        assertNull(rmi.getStore());
        assertNull(rmi.getRetransmissionQueue());
        assertNotNull(rmi.getTimer());
        
        IMocksControl control = EasyMock.createNiceControl();
        RMStore store = control.createMock(RMStore.class);
        RetransmissionQueue queue = control.createMock(RetransmissionQueue.class);
        
        rmi.setStore(store);
        rmi.setRetransmissionQueue(queue);
        assertSame(store, rmi.getStore());
        assertSame(queue, rmi.getRetransmissionQueue());
  
        
        
    }
    
    public void testInitialisation() {
        RMInterceptor rmi = new RMInterceptor();
        assertTrue("RMAssertion is set.", !rmi.isSetRMAssertion());
        assertTrue("sourcePolicy is set.", !rmi.isSetSourcePolicy());
        assertTrue("destinationPolicy is set.", !rmi.isSetDestinationPolicy());
        assertTrue("deliveryAssirance is set.", !rmi.isSetDeliveryAssurance());
        
        rmi.initialise();
        
        assertTrue("RMAssertion is not set.", rmi.isSetRMAssertion());
        assertTrue("sourcePolicy is not set.", rmi.isSetSourcePolicy());
        assertTrue("destinationPolicy is not set.", rmi.isSetDestinationPolicy());
        assertTrue("deliveryAssirance is not set.", rmi.isSetDeliveryAssurance());
        
        RMAssertion rma = rmi.getRMAssertion();
        assertTrue(rma.isSetExponentialBackoff());
        assertEquals(3000L, rma.getBaseRetransmissionInterval().getMilliseconds().longValue());
        assertTrue(!rma.isSetAcknowledgementInterval());
        assertTrue(!rma.isSetInactivityTimeout());   
        
        SourcePolicyType sp = rmi.getSourcePolicy();
        assertEquals(0, sp.getSequenceExpiration().getTimeInMillis(new Date()));
        assertEquals(0, sp.getOfferedSequenceExpiration().getTimeInMillis(new Date()));
        assertNull(sp.getAcksTo());
        assertTrue(sp.isIncludeOffer());
        SequenceTerminationPolicyType stp = sp.getSequenceTerminationPolicy();
        assertEquals(0, stp.getMaxRanges());
        assertEquals(0, stp.getMaxUnacknowledged());
        assertTrue(!stp.isTerminateOnShutdown());
        assertEquals(BigInteger.ZERO, stp.getMaxLength());
   
    }
    
    public void testOrdering() {
        Phase p = new Phase(Phase.PRE_LOGICAL, 1);
        PhaseInterceptorChain chain = 
            new PhaseInterceptorChain(Collections.singletonList(p));
        MAPAggregator map = new MAPAggregator();
        RMInterceptor rmi = new RMInterceptor();        
        chain.add(rmi);
        chain.add(map);
        Iterator it = chain.iterator();
        assertSame("Unexpected order.", map, it.next());
        assertSame("Unexpected order.", rmi, it.next());                      
    }    
    
    public void testAddAcknowledgement() {
        // MInterceptor rmi = new RMInterceptor();
        IMocksControl control = EasyMock.createNiceControl();
        control.createMock(Source.class);
         
    }
}
