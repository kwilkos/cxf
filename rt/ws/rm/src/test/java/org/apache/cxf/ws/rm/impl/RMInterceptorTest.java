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

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AddressingPropertiesImpl;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.ws.addressing.MAPAggregator;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.addressing.v200408.AttributedURI;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.RMMessageConstants;
import org.apache.cxf.ws.rm.RetransmissionQueue;
import org.apache.cxf.ws.rm.SequenceFault;
import org.apache.cxf.ws.rm.interceptor.SequenceTerminationPolicyType;
import org.apache.cxf.ws.rm.interceptor.SourcePolicyType;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.apache.cxf.ws.rm.policy.RMAssertion;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class RMInterceptorTest extends TestCase {
    
    private IMocksControl control;
    
    public void setUp() {
        control = EasyMock.createNiceControl();
    }
   
    public void testAccessors() {
        RMInterceptor rmi = new RMInterceptor();
        assertNull(rmi.getStore());
        assertNull(rmi.getRetransmissionQueue());
        assertNotNull(rmi.getTimer());
        
        RMStore store = control.createMock(RMStore.class);
        RetransmissionQueue queue = control.createMock(RetransmissionQueue.class);
        
        rmi.setStore(store);
        rmi.setRetransmissionQueue(queue);
        assertSame(store, rmi.getStore());
        assertSame(queue, rmi.getRetransmissionQueue());
        control.replay();
        control.verify();
        
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
    
    public void testGetReliableEndpoint() {
        
        RMInterceptor rmi = new RMInterceptor();
        Bus bus = control.createMock(Bus.class);
        rmi.setBus(bus);
        Message message = control.createMock(Message.class);
        EasyMock.expect(message.get(Message.REQUESTOR_ROLE)).andReturn(Boolean.TRUE).times(2);
        Exchange exchange = control.createMock(Exchange.class);
        EasyMock.expect(message.getExchange()).andReturn(exchange).times(2);
        Endpoint endpoint = control.createMock(Endpoint.class);
        EasyMock.expect(exchange.get(Endpoint.class)).andReturn(endpoint).times(2);
        control.replay();
        RMEndpoint reliableEndpoint = rmi.getReliableEndpoint(message);
        assertSame(endpoint, reliableEndpoint.getEndpoint());
        RMEndpoint rme = rmi.getReliableEndpoint(message);
        assertSame(reliableEndpoint, rme); 
        control.verify();
    }
    
    public void testGetDestination() throws NoSuchMethodException {
        Method  m = RMInterceptor.class
            .getDeclaredMethod("getReliableEndpoint", new Class[] {Message.class});        
        RMInterceptor rmi = control.createMock(RMInterceptor.class, new Method[] {m});
        Message message = control.createMock(Message.class);
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        EasyMock.expect(rmi.getReliableEndpoint(message)).andReturn(rme);    
        Destination destination = control.createMock(Destination.class);
        EasyMock.expect(rme.getDestination()).andReturn(destination);
       
        control.replay();
        assertSame(destination, rmi.getDestination(message));
        control.verify();
        
        control.reset();
        EasyMock.expect(rmi.getReliableEndpoint(message)).andReturn(null);
        control.replay();
        assertNull(rmi.getDestination(message));
        control.verify();        
    }
        
    public void testGetSource() throws NoSuchMethodException {
        Method m = RMInterceptor.class
            .getDeclaredMethod("getReliableEndpoint", new Class[] {Message.class});
        RMInterceptor rmi = control.createMock(RMInterceptor.class, new Method[] {m});
        Message message = control.createMock(Message.class);
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        EasyMock.expect(rmi.getReliableEndpoint(message)).andReturn(rme);
        Source source = control.createMock(Source.class);
        EasyMock.expect(rme.getSource()).andReturn(source);

        control.replay();
        assertSame(source, rmi.getSource(message));
        control.verify();

        control.reset();
        EasyMock.expect(rmi.getReliableEndpoint(message)).andReturn(null);
        control.replay();
        assertNull(rmi.getSource(message));
        control.verify();
    }
        
    public void testGetExistingSequence() throws NoSuchMethodException, SequenceFault {
        Method m = RMInterceptor.class
           .getDeclaredMethod("getSource", new Class[] {Message.class});
        RMInterceptor rmi = control.createMock(RMInterceptor.class, new Method[] {m});
        Message message = control.createMock(Message.class);
        Identifier inSid = control.createMock(Identifier.class);
        
        Source source = control.createMock(Source.class);
        EasyMock.expect(rmi.getSource(message)).andReturn(source);
        SourceSequenceImpl sseq = control.createMock(SourceSequenceImpl.class);
        EasyMock.expect(source.getCurrent(inSid)).andReturn(sseq);
        control.replay();
        assertSame(sseq, rmi.getSequence(inSid, message, null));
        control.verify();
    }
    
    public void testGetNewSequence() throws NoSuchMethodException, SequenceFault, IOException {
        Method m = RMInterceptor.class.getDeclaredMethod("getSource", new Class[] {Message.class});
        RMInterceptor rmi = control.createMock(RMInterceptor.class, new Method[] {m});
        Message message = control.createMock(Message.class);
        Identifier inSid = control.createMock(Identifier.class);
        AddressingProperties maps = control.createMock(AddressingProperties.class);
        Source source = control.createMock(Source.class);
        EasyMock.expect(rmi.getSource(message)).andReturn(source);
        EasyMock.expect(source.getCurrent(inSid)).andReturn(null);
        EndpointReferenceType epr = TestUtils.getNoneReference();
        EasyMock.expect(maps.getReplyTo()).andReturn(epr);
        Proxy proxy = control.createMock(Proxy.class);
        EasyMock.expect(source.getProxy()).andReturn(proxy);
        proxy.createSequence((EndpointReferenceType)EasyMock.isNull(),
                             EasyMock.isA(org.apache.cxf.ws.addressing.v200408.EndpointReferenceType.class),
                             (RelatesToType)EasyMock.isNull());
        EasyMock.expectLastCall();
        SourceSequenceImpl sseq = control.createMock(SourceSequenceImpl.class);
        EasyMock.expect(source.awaitCurrent(inSid)).andReturn(sseq);
        sseq.setTarget((EndpointReferenceType)EasyMock.isNull());
        EasyMock.expectLastCall();
        
        control.replay();
        assertSame(sseq, rmi.getSequence(inSid, message, maps));
        control.verify();
    }
    
    public void testHandleOutboundApplicationMessage() throws NoSuchMethodException, SequenceFault {
        AddressingPropertiesImpl maps = createMAPs("greetMe", "localhost:9000/GreeterPort", 
            org.apache.cxf.ws.addressing.Names.WSA_NONE_ADDRESS);
        Method[] mocked = new Method[] {
            RMInterceptor.class.getDeclaredMethod("getSource", new Class[] {Message.class}),
            RMInterceptor.class.getDeclaredMethod("getDestination", new Class[] {Message.class}),
            RMInterceptor.class.getDeclaredMethod("getSequence", 
                new Class[] {Identifier.class, Message.class, AddressingProperties.class}),
            RMInterceptor.class.getDeclaredMethod("addAcknowledgements",
                new Class[] {Destination.class, RMPropertiesImpl.class, Identifier.class, 
                             AttributedURI.class})            
        };
        RMInterceptor rmi = control.createMock(RMInterceptor.class, mocked);       
        Message message = control.createMock(Message.class);
        EasyMock.expect(message.get(Message.REQUESTOR_ROLE)).andReturn(Boolean.TRUE).anyTimes();        
        EasyMock.expect(message.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND))
            .andReturn(maps).anyTimes();
        RMPropertiesImpl rmpsOut = new RMPropertiesImpl();
        EasyMock.expect(message.get(RMMessageConstants.RM_PROPERTIES_OUTBOUND)).andReturn(rmpsOut);
        EasyMock.expect(message.get(RMMessageConstants.RM_PROPERTIES_INBOUND)).andReturn(null);        
        Source source = control.createMock(Source.class);
        EasyMock.expect(rmi.getSource(message)).andReturn(source);
        Destination destination = control.createMock(Destination.class);
        EasyMock.expect(rmi.getDestination(message)).andReturn(destination);
        SourceSequenceImpl sseq = control.createMock(SourceSequenceImpl.class);
        EasyMock.expect(rmi.getSequence((Identifier)EasyMock.isNull(), EasyMock.same(message), 
                                        EasyMock.same(maps))).andReturn(sseq);
        EasyMock.expect(sseq.nextMessageNumber((Identifier)EasyMock.isNull(), 
            (BigInteger)EasyMock.isNull())).andReturn(BigInteger.TEN);
        EasyMock.expect(sseq.isLastMessage()).andReturn(false).times(2);
        rmi.addAcknowledgements(EasyMock.same(destination), EasyMock.same(rmpsOut), 
            (Identifier)EasyMock.isNull(), EasyMock.isA(AttributedURI.class));
        EasyMock.expectLastCall();
        Identifier sid = control.createMock(Identifier.class);
        EasyMock.expect(sseq.getIdentifier()).andReturn(sid);
        EasyMock.expect(sseq.getCurrentMessageNr()).andReturn(BigInteger.TEN);

        
        control.replay();
        rmi.handleOutbound(message, false);
        assertSame(sid, rmpsOut.getSequence().getIdentifier());        
        assertEquals(BigInteger.TEN, rmpsOut.getSequence().getMessageNumber());
        assertNull(rmpsOut.getSequence().getLastMessage());
        control.verify();
    }
    
    
    private AddressingPropertiesImpl createMAPs(String action, String to, String replyTo) {
        AddressingPropertiesImpl maps = new AddressingPropertiesImpl();
        org.apache.cxf.ws.addressing.ObjectFactory factory = 
            new org.apache.cxf.ws.addressing.ObjectFactory();
        AttributedURIType uri = factory.createAttributedURIType();
        uri.setValue(action);
        maps.setAction(uri);
        uri = factory.createAttributedURIType();
        uri.setValue(to);
        maps.setTo(uri);
        EndpointReferenceType epr = TestUtils.getReference(replyTo);
        maps.setReplyTo(epr);
        return maps;
           
    }
}
