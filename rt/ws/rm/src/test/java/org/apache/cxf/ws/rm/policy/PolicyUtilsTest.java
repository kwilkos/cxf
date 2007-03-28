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

package org.apache.cxf.ws.rm.policy;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.cxf.message.Message;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.policy.builder.jaxb.JaxbAssertion;
import org.apache.cxf.ws.rm.RMConstants;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class PolicyUtilsTest extends Assert {

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
    public void testGetBaseRetranmissionInterval() {
        Message message = control.createMock(Message.class);
        AssertionInfoMap aim = control.createMock(AssertionInfoMap.class);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(aim);
        AssertionInfo ai1 = control.createMock(AssertionInfo.class);
        AssertionInfo ai2 =  control.createMock(AssertionInfo.class);
        AssertionInfo ai3 =  control.createMock(AssertionInfo.class);
        AssertionInfo ai4 =  control.createMock(AssertionInfo.class);
        Collection<AssertionInfo> ais = new ArrayList<AssertionInfo>();
        ais.add(ai1);
        ais.add(ai2);
        ais.add(ai3);
        ais.add(ai4);
        EasyMock.expect(aim.get(RMConstants.getRMAssertionQName())).andReturn(ais);
        JaxbAssertion ja1 =  control.createMock(JaxbAssertion.class);
        EasyMock.expect(ai1.getAssertion()).andReturn(ja1);
        RMAssertion rma1 =  control.createMock(RMAssertion.class);
        EasyMock.expect(ja1.getData()).andReturn(rma1);
        EasyMock.expect(rma1.getBaseRetransmissionInterval()).andReturn(null);
        JaxbAssertion ja2 =  control.createMock(JaxbAssertion.class);
        EasyMock.expect(ai2.getAssertion()).andReturn(ja2);
        RMAssertion rma2 =  control.createMock(RMAssertion.class);
        EasyMock.expect(ja2.getData()).andReturn(rma2);
        RMAssertion.BaseRetransmissionInterval bri2 = 
            control.createMock(RMAssertion.BaseRetransmissionInterval.class);
        EasyMock.expect(rma2.getBaseRetransmissionInterval()).andReturn(bri2);
        EasyMock.expect(bri2.getMilliseconds()).andReturn(null);
        JaxbAssertion ja3 =  control.createMock(JaxbAssertion.class);
        EasyMock.expect(ai3.getAssertion()).andReturn(ja3);
        RMAssertion rma3 =  control.createMock(RMAssertion.class);
        EasyMock.expect(ja3.getData()).andReturn(rma3);
        RMAssertion.BaseRetransmissionInterval bri3 = 
            control.createMock(RMAssertion.BaseRetransmissionInterval.class);
        EasyMock.expect(rma3.getBaseRetransmissionInterval()).andReturn(bri3);
        EasyMock.expect(bri3.getMilliseconds()).andReturn(new BigInteger("10000"));
        JaxbAssertion ja4 =  control.createMock(JaxbAssertion.class);
        EasyMock.expect(ai4.getAssertion()).andReturn(ja4);
        RMAssertion rma4 =  control.createMock(RMAssertion.class);
        EasyMock.expect(ja4.getData()).andReturn(rma4);
        RMAssertion.BaseRetransmissionInterval bri4 = 
            control.createMock(RMAssertion.BaseRetransmissionInterval.class);
        EasyMock.expect(rma4.getBaseRetransmissionInterval()).andReturn(bri4);
        EasyMock.expect(bri4.getMilliseconds()).andReturn(new BigInteger("5000"));
        
        control.replay();
        assertEquals("Unexpected value for base retransmission interval", 
                     5000, PolicyUtils.getBaseRetransmissionInterval(message).intValue());
    }
    
    @Test
    public void testUseExponentialBackoff() {
        Message message = control.createMock(Message.class);
        AssertionInfoMap aim = control.createMock(AssertionInfoMap.class);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(aim);
        AssertionInfo ai = control.createMock(AssertionInfo.class);
        Collection<AssertionInfo> ais = new ArrayList<AssertionInfo>();
        EasyMock.expect(aim.get(RMConstants.getRMAssertionQName())).andReturn(ais);
        ais.add(ai);
        JaxbAssertion ja = control.createMock(JaxbAssertion.class);
        EasyMock.expect(ai.getAssertion()).andReturn(ja);
        RMAssertion rma =  control.createMock(RMAssertion.class);
        EasyMock.expect(ja.getData()).andReturn(rma);
        EasyMock.expect(rma.getExponentialBackoff()).andReturn(null);
        control.replay();
        assertTrue("Should not use exponential backoff", !PolicyUtils.useExponentialBackoff(message));
        control.verify();
        control.reset();
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(null);
        control.replay();
        assertTrue("Should use exponential backoff", PolicyUtils.useExponentialBackoff(message));    
    }
   
    @Test
    public void testGetAcknowledgmentInterval() {
        Message message = control.createMock(Message.class);
        AssertionInfoMap aim = control.createMock(AssertionInfoMap.class);
        EasyMock.expect(message.get(AssertionInfoMap.class)).andReturn(aim);
        AssertionInfo ai1 = control.createMock(AssertionInfo.class);
        AssertionInfo ai2 =  control.createMock(AssertionInfo.class);
        AssertionInfo ai3 =  control.createMock(AssertionInfo.class);
        AssertionInfo ai4 =  control.createMock(AssertionInfo.class);
        Collection<AssertionInfo> ais = new ArrayList<AssertionInfo>();
        ais.add(ai1);
        ais.add(ai2);
        ais.add(ai3);
        ais.add(ai4);
        EasyMock.expect(aim.get(RMConstants.getRMAssertionQName())).andReturn(ais);
        JaxbAssertion ja1 =  control.createMock(JaxbAssertion.class);
        EasyMock.expect(ai1.getAssertion()).andReturn(ja1);
        RMAssertion rma1 =  control.createMock(RMAssertion.class);
        EasyMock.expect(ja1.getData()).andReturn(rma1);
        EasyMock.expect(rma1.getAcknowledgementInterval()).andReturn(null);
        JaxbAssertion ja2 =  control.createMock(JaxbAssertion.class);
        EasyMock.expect(ai2.getAssertion()).andReturn(ja2);
        RMAssertion rma2 =  control.createMock(RMAssertion.class);
        EasyMock.expect(ja2.getData()).andReturn(rma2);
        RMAssertion.AcknowledgementInterval aint2 = 
            control.createMock(RMAssertion.AcknowledgementInterval.class);
        EasyMock.expect(rma2.getAcknowledgementInterval()).andReturn(aint2);
        EasyMock.expect(aint2.getMilliseconds()).andReturn(null);
        JaxbAssertion ja3 =  control.createMock(JaxbAssertion.class);
        EasyMock.expect(ai3.getAssertion()).andReturn(ja3);
        RMAssertion rma3 =  control.createMock(RMAssertion.class);
        EasyMock.expect(ja3.getData()).andReturn(rma3);
        RMAssertion.AcknowledgementInterval aint3 = 
            control.createMock(RMAssertion.AcknowledgementInterval.class);
        EasyMock.expect(rma3.getAcknowledgementInterval()).andReturn(aint3);
        EasyMock.expect(aint3.getMilliseconds()).andReturn(new BigInteger("10000"));
        JaxbAssertion ja4 =  control.createMock(JaxbAssertion.class);
        EasyMock.expect(ai4.getAssertion()).andReturn(ja4);
        RMAssertion rma4 =  control.createMock(RMAssertion.class);
        EasyMock.expect(ja4.getData()).andReturn(rma4);
        RMAssertion.AcknowledgementInterval aint4 = 
            control.createMock(RMAssertion.AcknowledgementInterval.class);
        EasyMock.expect(rma4.getAcknowledgementInterval()).andReturn(aint4);
        EasyMock.expect(aint4.getMilliseconds()).andReturn(new BigInteger("5000"));
        
        control.replay();
        assertEquals("Unexpected value for acknowledgment interval", 
                     5000, PolicyUtils.getAcknowledgmentInterval(message).intValue());
    }
    
    
}
