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

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.rm.manager.SequenceTerminationPolicyType;
import org.apache.cxf.ws.rm.manager.SourcePolicyType;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.apache.cxf.ws.rm.policy.RMAssertion;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class RMManagerTest extends TestCase {
    
    private IMocksControl control;
    
    public void setUp() {
        control = EasyMock.createNiceControl();
    }
   
    public void testAccessors() {
        RMManager manager = new RMManager();
        assertNull(manager.getStore());
        assertNull(manager.getRetransmissionQueue());
        assertNotNull(manager.getTimer());
        
        RMStore store = control.createMock(RMStore.class);
        RetransmissionQueue queue = control.createMock(RetransmissionQueue.class);
        
        manager.setStore(store);
        manager.setRetransmissionQueue(queue);
        assertSame(store, manager.getStore());
        assertSame(queue, manager.getRetransmissionQueue());
        control.replay();
        control.verify();
        
    }
    
    public void testInitialisation() {
        RMManager manager = new RMManager();
        assertTrue("RMAssertion is set.", !manager.isSetRMAssertion());
        assertTrue("sourcePolicy is set.", !manager.isSetSourcePolicy());
        assertTrue("destinationPolicy is set.", !manager.isSetDestinationPolicy());
        assertTrue("deliveryAssirance is set.", !manager.isSetDeliveryAssurance());
        
        manager.initialise();
        
        assertTrue("RMAssertion is not set.", manager.isSetRMAssertion());
        assertTrue("sourcePolicy is not set.", manager.isSetSourcePolicy());
        assertTrue("destinationPolicy is not set.", manager.isSetDestinationPolicy());
        assertTrue("deliveryAssirance is not set.", manager.isSetDeliveryAssurance());
        
        RMAssertion rma = manager.getRMAssertion();
        assertTrue(rma.isSetExponentialBackoff());
        assertEquals(3000L, rma.getBaseRetransmissionInterval().getMilliseconds().longValue());
        assertTrue(!rma.isSetAcknowledgementInterval());
        assertTrue(!rma.isSetInactivityTimeout());   
        
        SourcePolicyType sp = manager.getSourcePolicy();
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
    
    public void xtestGetReliableEndpoint() {
        
        RMManager manager = new RMManager();
        Bus bus = control.createMock(Bus.class);
        manager.setBus(bus);
        Message message = control.createMock(Message.class);
        EasyMock.expect(message.get(Message.REQUESTOR_ROLE)).andReturn(Boolean.TRUE).times(2);
        Exchange exchange = control.createMock(Exchange.class);
        EasyMock.expect(message.getExchange()).andReturn(exchange).times(2);
        // Endpoint endpoint = control.createMock(Endpoint.class);
        EasyMock.expect(exchange.get(Endpoint.class)).andReturn(null).times(2);
        control.replay();
        RMEndpoint reliableEndpoint = manager.getReliableEndpoint(message);
        // assertSame(endpoint, reliableEndpoint.getEndpoint());
        RMEndpoint rme = manager.getReliableEndpoint(message);
        assertSame(reliableEndpoint, rme); 
        control.verify();
    }
    
    public void testGetDestination() throws NoSuchMethodException {
        Method  m = RMManager.class
            .getDeclaredMethod("getReliableEndpoint", new Class[] {Message.class});        
        RMManager manager = control.createMock(RMManager.class, new Method[] {m});
        Message message = control.createMock(Message.class);
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        EasyMock.expect(manager.getReliableEndpoint(message)).andReturn(rme);    
        Destination destination = control.createMock(Destination.class);
        EasyMock.expect(rme.getDestination()).andReturn(destination);
       
        control.replay();
        assertSame(destination, manager.getDestination(message));
        control.verify();
        
        control.reset();
        EasyMock.expect(manager.getReliableEndpoint(message)).andReturn(null);
        control.replay();
        assertNull(manager.getDestination(message));
        control.verify();        
    }
        
    public void testGetSource() throws NoSuchMethodException {
        Method m = RMManager.class
            .getDeclaredMethod("getReliableEndpoint", new Class[] {Message.class});
        RMManager manager = control.createMock(RMManager.class, new Method[] {m});
        Message message = control.createMock(Message.class);
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        EasyMock.expect(manager.getReliableEndpoint(message)).andReturn(rme);
        Source source = control.createMock(Source.class);
        EasyMock.expect(rme.getSource()).andReturn(source);

        control.replay();
        assertSame(source, manager.getSource(message));
        control.verify();

        control.reset();
        EasyMock.expect(manager.getReliableEndpoint(message)).andReturn(null);
        control.replay();
        assertNull(manager.getSource(message));
        control.verify();
    }
        
    public void testGetExistingSequence() throws NoSuchMethodException, SequenceFault {
        Method m = RMManager.class
           .getDeclaredMethod("getSource", new Class[] {Message.class});
        RMManager manager = control.createMock(RMManager.class, new Method[] {m});
        Message message = control.createMock(Message.class);
        Identifier inSid = control.createMock(Identifier.class);
        
        Source source = control.createMock(Source.class);
        EasyMock.expect(manager.getSource(message)).andReturn(source);
        SourceSequence sseq = control.createMock(SourceSequence.class);
        EasyMock.expect(source.getCurrent(inSid)).andReturn(sseq);
        control.replay();
        assertSame(sseq, manager.getSequence(inSid, message, null));
        control.verify();
    }
    
    public void testGetNewSequence() throws NoSuchMethodException, SequenceFault, IOException {
        Method m = RMManager.class.getDeclaredMethod("getSource", new Class[] {Message.class});
        RMManager manager = control.createMock(RMManager.class, new Method[] {m});
        Message message = control.createMock(Message.class);
        Identifier inSid = control.createMock(Identifier.class);
        AddressingProperties maps = control.createMock(AddressingProperties.class);
        Source source = control.createMock(Source.class);
        EasyMock.expect(manager.getSource(message)).andReturn(source);
        EasyMock.expect(source.getCurrent(inSid)).andReturn(null);
        EndpointReferenceType epr = RMUtils.createNoneReference();
        EasyMock.expect(maps.getReplyTo()).andReturn(epr);
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        EasyMock.expect(source.getReliableEndpoint()).andReturn(rme);
        Proxy proxy = control.createMock(Proxy.class);
        EasyMock.expect(rme.getProxy()).andReturn(proxy);
        proxy.createSequence((EndpointReferenceType)EasyMock.isNull(),
                             EasyMock.isA(org.apache.cxf.ws.addressing.v200408.EndpointReferenceType.class),
                             (RelatesToType)EasyMock.isNull());
        EasyMock.expectLastCall();
        SourceSequence sseq = control.createMock(SourceSequence.class);
        EasyMock.expect(source.awaitCurrent(inSid)).andReturn(sseq);
        sseq.setTarget((EndpointReferenceType)EasyMock.isNull());
        EasyMock.expectLastCall();
        
        control.replay();
        assertSame(sseq, manager.getSequence(inSid, message, maps));
        control.verify();
    }
}
