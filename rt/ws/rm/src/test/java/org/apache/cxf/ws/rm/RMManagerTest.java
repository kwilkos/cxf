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

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.TimerTask;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapBinding;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerLifeCycleListener;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AddressingPropertiesImpl;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.rm.manager.SequenceTerminationPolicyType;
import org.apache.cxf.ws.rm.manager.SourcePolicyType;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.apache.cxf.ws.rm.policy.RMAssertion;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RMManagerTest extends Assert {
    
    private IMocksControl control;
    
    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
    }
   
    @Test
    public void testAccessors() {
        RMManager manager = new RMManager();
        assertNull(manager.getBus());
        assertNull(manager.getStore());
        assertNull(manager.getRetransmissionQueue());
        assertNotNull(manager.getTimer());
        assertNotNull(manager.getServerLifeCycleListener());
        
        Bus bus = control.createMock(Bus.class);
        RMStore store = control.createMock(RMStore.class);
        RetransmissionQueue queue = control.createMock(RetransmissionQueue.class);
        
        manager.setBus(bus);
        manager.setStore(store);
        manager.setRetransmissionQueue(queue);
        assertSame(bus, manager.getBus());
        assertSame(store, manager.getStore());
        assertSame(queue, manager.getRetransmissionQueue());
        control.replay();
        control.verify();
        
    }
    
    @Test
    public void testInitialisation() {
        RMManager manager = new RMManager();
        assertNull("RMAssertion is set.", manager.getRMAssertion());
        assertNull("sourcePolicy is set.", manager.getSourcePolicy());
        assertNull("destinationPolicy is set.", manager.getDestinationPolicy());
        assertNull("deliveryAssirance is set.", manager.getDeliveryAssurance());
        
        manager.initialise();
        
        assertNotNull("RMAssertion is not set.", manager.getRMAssertion());
        assertNotNull("sourcePolicy is not set.", manager.getSourcePolicy());
        assertNotNull("destinationPolicy is not set.", manager.getDestinationPolicy());
        assertNotNull("deliveryAssirance is not set.", manager.getDeliveryAssurance());
        
        RMAssertion rma = manager.getRMAssertion();
        assertTrue(rma.isSetExponentialBackoff());
        assertEquals(3000L, rma.getBaseRetransmissionInterval().getMilliseconds().longValue());
        assertTrue(!rma.isSetAcknowledgementInterval());
        assertTrue(!rma.isSetInactivityTimeout());   
        
        SourcePolicyType sp = manager.getSourcePolicy();
        assertEquals(0L, sp.getSequenceExpiration().getTimeInMillis(new Date()));
        assertEquals(0L, sp.getOfferedSequenceExpiration().getTimeInMillis(new Date()));
        assertNull(sp.getAcksTo());
        assertTrue(sp.isIncludeOffer());
        SequenceTerminationPolicyType stp = sp.getSequenceTerminationPolicy();
        assertEquals(0, stp.getMaxRanges());
        assertEquals(0, stp.getMaxUnacknowledged());
        assertTrue(!stp.isTerminateOnShutdown());
        assertEquals(BigInteger.ZERO, stp.getMaxLength());   
    } 
    
    @Test
    public void testServerLifecycleLister() {
        RMManager manager = new RMManager();
        Server s = control.createMock(Server.class);
        Endpoint e = control.createMock(Endpoint.class);
        EasyMock.expect(s.getEndpoint()).andReturn(e);
        control.replay();
        ServerLifeCycleListener sl = manager.getServerLifeCycleListener();
        sl.startServer(s);
        sl.stopServer(s);
        control.verify();
        
        control.reset();
        EasyMock.expect(s.getEndpoint()).andReturn(e);
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        manager.getReliableEndpointsMap().put(e, rme);
        rme.shutdown();
        EasyMock.expectLastCall();
        control.replay();
        sl.stopServer(s);
        control.verify();
    }
    
    @Test
    public void testGetBindingFaultFactory() {
        SoapBinding binding = control.createMock(SoapBinding.class);
        assertNotNull(new RMManager().getBindingFaultFactory(binding));
    }
    
    @Test
    public void testGetReliableEndpointServerSideCreate() throws NoSuchMethodException {
        Method m = RMManager.class.getDeclaredMethod("createReliableEndpoint", 
            new Class[] {RMManager.class, Endpoint.class});
        RMManager manager = control.createMock(RMManager.class, new Method[] {m});
        manager.setReliableEndpointsMap(new HashMap<Endpoint, RMEndpoint>());
        Message message = control.createMock(Message.class);
        Exchange exchange = control.createMock(Exchange.class);
        EasyMock.expect(message.getExchange()).andReturn(exchange).times(3);
        WrappedEndpoint wre = control.createMock(WrappedEndpoint.class);
        EasyMock.expect(exchange.get(Endpoint.class)).andReturn(wre);
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        EasyMock.expect(wre.getEndpointInfo()).andReturn(ei);
        QName name = RMConstants.getPortName();
        EasyMock.expect(ei.getName()).andReturn(name);
        Endpoint e = control.createMock(Endpoint.class);
        EasyMock.expect(wre.getWrappedEndpoint()).andReturn(e);        
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        EasyMock.expect(manager.createReliableEndpoint(manager, e)).andReturn(rme);
        org.apache.cxf.transport.Destination destination = control
            .createMock(org.apache.cxf.transport.Destination.class);
        EasyMock.expect(exchange.getDestination()).andReturn(destination);
        AddressingPropertiesImpl maps = control.createMock(AddressingPropertiesImpl.class);
        EasyMock.expect(message.get(Message.REQUESTOR_ROLE)).andReturn(null);
        EasyMock.expect(message.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND)).andReturn(maps);
        EndpointReferenceType replyTo = RMUtils.createAnonymousReference();
        EasyMock.expect(maps.getReplyTo()).andReturn(replyTo);
        EasyMock.expect(exchange.getConduit(message)).andReturn(null);
        rme.initialise(null, replyTo);
        EasyMock.expectLastCall();

        control.replay();
        assertSame(rme, manager.getReliableEndpoint(message));
        control.verify();

        control.reset();
        EasyMock.expect(message.getExchange()).andReturn(exchange);
        EasyMock.expect(exchange.get(Endpoint.class)).andReturn(wre);
        EasyMock.expect(wre.getEndpointInfo()).andReturn(ei);
        EasyMock.expect(ei.getName()).andReturn(name);
        EasyMock.expect(wre.getWrappedEndpoint()).andReturn(e); 

        control.replay();
        assertSame(rme, manager.getReliableEndpoint(message));
        control.verify();
    }
    
    @Test
    public void testGetReliableEndpointClientSideCreate() throws NoSuchMethodException {
        Method m = RMManager.class.getDeclaredMethod("createReliableEndpoint", 
            new Class[] {RMManager.class, Endpoint.class});
        RMManager manager = control.createMock(RMManager.class, new Method[] {m});
        manager.setReliableEndpointsMap(new HashMap<Endpoint, RMEndpoint>());
        Message message = control.createMock(Message.class);
        Exchange exchange = control.createMock(Exchange.class);
        EasyMock.expect(message.getExchange()).andReturn(exchange).times(3);
        Endpoint endpoint = control.createMock(Endpoint.class);
        EasyMock.expect(exchange.get(Endpoint.class)).andReturn(endpoint);
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        EasyMock.expect(endpoint.getEndpointInfo()).andReturn(ei);
        QName name = new QName("http://x.y.z/a", "GreeterPort");
        EasyMock.expect(ei.getName()).andReturn(name);
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        EasyMock.expect(manager.createReliableEndpoint(manager, endpoint)).andReturn(rme);
        EasyMock.expect(exchange.getDestination()).andReturn(null);
        Conduit conduit = control.createMock(Conduit.class);
        EasyMock.expect(exchange.getConduit(message)).andReturn(conduit);
        rme.initialise(conduit, null);
        EasyMock.expectLastCall();

        control.replay();
        assertSame(rme, manager.getReliableEndpoint(message));
        control.verify();

        control.reset();
        EasyMock.expect(message.getExchange()).andReturn(exchange);
        EasyMock.expect(exchange.get(Endpoint.class)).andReturn(endpoint);
        EasyMock.expect(endpoint.getEndpointInfo()).andReturn(ei);
        EasyMock.expect(ei.getName()).andReturn(name);
    
        control.replay();
        assertSame(rme, manager.getReliableEndpoint(message));
        control.verify();
    }
    
    @Test
    public void testGetReliableEndpointExisting() throws NoSuchMethodException {
        Method m = RMManager.class.getDeclaredMethod("createReliableEndpoint", 
            new Class[] {RMManager.class, Endpoint.class});
        RMManager manager = control.createMock(RMManager.class, new Method[] {m});
        manager.setReliableEndpointsMap(new HashMap<Endpoint, RMEndpoint>());
        Message message = control.createMock(Message.class);
        Exchange exchange = control.createMock(Exchange.class);
        EasyMock.expect(message.getExchange()).andReturn(exchange);
        Endpoint endpoint = control.createMock(Endpoint.class);
        EasyMock.expect(exchange.get(Endpoint.class)).andReturn(endpoint);
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        EasyMock.expect(endpoint.getEndpointInfo()).andReturn(ei);
        QName name = new QName("http://x.y.z/a", "GreeterPort");
        EasyMock.expect(ei.getName()).andReturn(name);
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        manager.getReliableEndpointsMap().put(endpoint, rme);

        control.replay();
        assertSame(rme, manager.getReliableEndpoint(message));
        control.verify();
    }
    
    @Test
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
    
    @Test
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
     
    @Test
    public void testGetExistingSequence() throws NoSuchMethodException, SequenceFault, RMException {
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
    
    @Test
    public void testGetNewSequence() throws NoSuchMethodException, SequenceFault, RMException {
        Method m = RMManager.class.getDeclaredMethod("getSource", new Class[] {Message.class});
        RMManager manager = control.createMock(RMManager.class, new Method[] {m});
        Message message = control.createMock(Message.class);
        Exchange exchange = control.createMock(Exchange.class);
        EasyMock.expect(message.getExchange()).andReturn(exchange).anyTimes();
        EasyMock.expect(exchange.getOutMessage()).andReturn(message).anyTimes();
        EasyMock.expect(exchange.getInMessage()).andReturn(null).anyTimes();
        EasyMock.expect(exchange.getOutFaultMessage()).andReturn(null).anyTimes();
        Conduit conduit = control.createMock(Conduit.class);
        EasyMock.expect(exchange.getConduit(message)).andReturn(conduit).anyTimes();
        EasyMock.expect(conduit.getBackChannel()).andReturn(null).anyTimes();
        Identifier inSid = control.createMock(Identifier.class);        
        AddressingProperties maps = control.createMock(AddressingProperties.class);
        Source source = control.createMock(Source.class);
        EasyMock.expect(manager.getSource(message)).andReturn(source);
        EasyMock.expect(source.getCurrent(inSid)).andReturn(null);
        AttributedURIType uri = control.createMock(AttributedURIType.class);
        EasyMock.expect(maps.getTo()).andReturn(uri);
        EasyMock.expect(uri.getValue()).andReturn("http://localhost:9001/TestPort");
        EndpointReferenceType epr = RMUtils.createNoneReference();
        EasyMock.expect(maps.getReplyTo()).andReturn(epr);
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        EasyMock.expect(source.getReliableEndpoint()).andReturn(rme).times(2);
        Proxy proxy = control.createMock(Proxy.class);
        EasyMock.expect(rme.getProxy()).andReturn(proxy);
        CreateSequenceResponseType createResponse = control.createMock(CreateSequenceResponseType.class);
        proxy.createSequence(EasyMock.isA(org.apache.cxf.ws.addressing.v200408.EndpointReferenceType.class),
                             (RelatesToType)EasyMock.isNull(),
                             EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(createResponse);
        Servant servant = control.createMock(Servant.class);
        EasyMock.expect(rme.getServant()).andReturn(servant);
        servant.createSequenceResponse(createResponse);
        EasyMock.expectLastCall();
        SourceSequence sseq = control.createMock(SourceSequence.class);
        EasyMock.expect(source.awaitCurrent(inSid)).andReturn(sseq);
        sseq.setTarget(EasyMock.isA(EndpointReferenceType.class));
        EasyMock.expectLastCall();
        
        control.replay();
        assertSame(sseq, manager.getSequence(inSid, message, maps));
        control.verify();
    }
    
    @Test
    public void testShutdown() {
        Bus bus = new SpringBusFactory().createBus("org/apache/cxf/ws/rm/rmmanager.xml", false);
        RMManager manager = bus.getExtension(RMManager.class);        
        Endpoint e = control.createMock(Endpoint.class);
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        manager.getReliableEndpointsMap().put(e, rme);
        rme.shutdown();
        EasyMock.expectLastCall();
        assertNotNull(manager);
        class TestTask extends TimerTask {
            public void run() {
            }
        }
        control.replay();
        bus.shutdown(true);
        try {
            manager.getTimer().schedule(new TestTask(), 5000); 
            fail("Timer has not been cancelled.");
        } catch (IllegalStateException ex) {
            // expected
        }
    }
    
    @Test
    public void testShutdownReliableEndpoint() {
        RMManager manager = new RMManager();
        Endpoint e = control.createMock(Endpoint.class);
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        control.replay();
        manager.shutdownReliableEndpoint(e);
        control.verify();
        
        control.reset();
        manager.getReliableEndpointsMap().put(e, rme);
        rme.shutdown();
        EasyMock.expectLastCall();
        control.replay();
        manager.shutdownReliableEndpoint(e);
        assertNull(manager.getReliableEndpointsMap().get(e));        
    }
    
    @Test
    public void testDefaultSequenceIdentifierGenerator() {
        RMManager manager = new RMManager();
        assertNull(manager.getIdGenerator());
        SequenceIdentifierGenerator generator = manager.new DefaultSequenceIdentifierGenerator();
        manager.setIdGenerator(generator);
        assertSame(generator, manager.getIdGenerator());
        Identifier id1 = generator.generateSequenceIdentifier();
        assertNotNull(id1);
        assertNotNull(id1.getValue());
        Identifier id2 = generator.generateSequenceIdentifier();
        assertTrue(id1 != id2);
        assertTrue(!id1.getValue().equals(id2.getValue()));     
        control.replay();
    }
    
     
} 
