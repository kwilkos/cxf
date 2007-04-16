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
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.Duration;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.jaxb.DatatypeFactory;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.workqueue.SynchronousExecutor;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.addressing.v200408.AttributedURI;
import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;
import org.apache.cxf.ws.rm.manager.SourcePolicyType;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class ProxyTest extends Assert {

    private IMocksControl control;
    private RMEndpoint rme;
    
    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
        rme = control.createMock(RMEndpoint.class);
    }

    @After
    public void tearDown() {
        control.verify();
    }
    
    @Test
    public void testCtor() {
        Proxy proxy = new Proxy(rme);
        assertSame(rme, proxy.getReliableEndpoint());  
        control.replay();
    }
    
    @Test
    public void testOfferedIdentifier() { 
        OfferType offer = control.createMock(OfferType.class);        
        Identifier id = control.createMock(Identifier.class);
        EasyMock.expect(offer.getIdentifier()).andReturn(id);
        control.replay();
        Proxy proxy = new Proxy(rme);
        assertNull(proxy.getOfferedIdentifier());
        proxy.setOfferedIdentifier(offer);
        assertSame(id, proxy.getOfferedIdentifier());
    }
    
    @Test
    public void testAcknowledgeNotSupported() throws IOException {
        DestinationSequence ds = control.createMock(DestinationSequence.class);
        EndpointReferenceType acksToEPR = control.createMock(EndpointReferenceType.class);
        EasyMock.expect(ds.getAcksTo()).andReturn(acksToEPR);
        AttributedURI acksToURI = control.createMock(AttributedURI.class);
        EasyMock.expect(acksToEPR.getAddress()).andReturn(acksToURI);
        String acksToAddress = RMConstants.getAnonymousAddress();
        EasyMock.expect(acksToURI.getValue()).andReturn(acksToAddress);
        control.replay();
        Proxy proxy = new Proxy(rme);
        proxy.acknowledge(ds);        
    }
    
    @Test
    public void testAcknowledge() throws NoSuchMethodException, IOException {
        Method m = Proxy.class.getDeclaredMethod("invoke", 
            new Class[] {OperationInfo.class, Object[].class, Map.class});
        Proxy proxy = control.createMock(Proxy.class, new Method[] {m});
        proxy.setReliableEndpoint(rme);
        DestinationSequence ds = control.createMock(DestinationSequence.class);
        EndpointReferenceType acksToEPR = control.createMock(EndpointReferenceType.class);
        EasyMock.expect(ds.getAcksTo()).andReturn(acksToEPR);
        AttributedURI acksToURI = control.createMock(AttributedURI.class);
        EasyMock.expect(acksToEPR.getAddress()).andReturn(acksToURI);
        String acksToAddress = "acksTo";
        EasyMock.expect(acksToURI.getValue()).andReturn(acksToAddress);
        Endpoint endpoint = control.createMock(Endpoint.class);
        EasyMock.expect(rme.getEndpoint()).andReturn(endpoint);
        EndpointInfo epi = control.createMock(EndpointInfo.class);
        EasyMock.expect(endpoint.getEndpointInfo()).andReturn(epi);
        ServiceInfo si = control.createMock(ServiceInfo.class);
        EasyMock.expect(epi.getService()).andReturn(si);
        InterfaceInfo ii = control.createMock(InterfaceInfo.class);
        EasyMock.expect(si.getInterface()).andReturn(ii);
        OperationInfo oi = control.createMock(OperationInfo.class);
        EasyMock.expect(ii.getOperation(RMConstants.getSequenceAckOperationName())).andReturn(oi);
        expectInvoke(proxy, oi, null);
        control.replay();
        
        proxy.acknowledge(ds);      
    }
    
    @Test    
    public void testTerminate() throws NoSuchMethodException, IOException {
        Method m = Proxy.class.getDeclaredMethod("invoke", 
            new Class[] {OperationInfo.class, Object[].class, Map.class});
        Proxy proxy = control.createMock(Proxy.class, new Method[] {m});
        proxy.setReliableEndpoint(rme);        
        Endpoint endpoint = control.createMock(Endpoint.class);
        EasyMock.expect(rme.getEndpoint()).andReturn(endpoint);
        EndpointInfo epi = control.createMock(EndpointInfo.class);
        EasyMock.expect(endpoint.getEndpointInfo()).andReturn(epi);
        ServiceInfo si = control.createMock(ServiceInfo.class);
        EasyMock.expect(epi.getService()).andReturn(si);
        InterfaceInfo ii = control.createMock(InterfaceInfo.class);
        EasyMock.expect(si.getInterface()).andReturn(ii);
        OperationInfo oi = control.createMock(OperationInfo.class);
        EasyMock.expect(ii.getOperation(RMConstants.getTerminateSequenceOperationName())).andReturn(oi);
        SourceSequence ss = control.createMock(SourceSequence.class);
        Identifier id = control.createMock(Identifier.class);
        EasyMock.expect(ss.getIdentifier()).andReturn(id);
        expectInvoke(proxy, oi, null);
        control.replay();
        proxy.terminate(ss);
    }
    
    @Test
    public void testCreateSequenceResponse() throws NoSuchMethodException, IOException {
        Method m = Proxy.class.getDeclaredMethod("invoke", 
            new Class[] {OperationInfo.class, Object[].class, Map.class});
        Proxy proxy = control.createMock(Proxy.class, new Method[] {m});
        proxy.setReliableEndpoint(rme);
        Endpoint endpoint = control.createMock(Endpoint.class);
        EasyMock.expect(rme.getEndpoint()).andReturn(endpoint);
        EndpointInfo epi = control.createMock(EndpointInfo.class);
        EasyMock.expect(endpoint.getEndpointInfo()).andReturn(epi);
        ServiceInfo si = control.createMock(ServiceInfo.class);
        EasyMock.expect(epi.getService()).andReturn(si);
        InterfaceInfo ii = control.createMock(InterfaceInfo.class);
        EasyMock.expect(si.getInterface()).andReturn(ii);
        OperationInfo oi = control.createMock(OperationInfo.class);
        EasyMock.expect(ii.getOperation(RMConstants.getCreateSequenceResponseOnewayOperationName()))
            .andReturn(oi);
        CreateSequenceResponseType csr = control.createMock(CreateSequenceResponseType.class);
        expectInvoke(proxy, oi, null);
        control.replay();
        proxy.createSequenceResponse(csr);
    }
    
    @Test
    public void testCreateSequenceOnClient() throws NoSuchMethodException, IOException {
        testCreateSequence(false); 
    }
    
    @Test
    public void testCreateSequenceOnServer() throws NoSuchMethodException, IOException {
        testCreateSequence(true); 
    }
    
    @Test
    public void testInvoke() throws Exception {        
        Method m = Proxy.class.getDeclaredMethod("createClient", 
            new Class[] {Bus.class, Endpoint.class, Conduit.class, 
                         org.apache.cxf.ws.addressing.EndpointReferenceType.class});
        Proxy proxy = control.createMock(Proxy.class, new Method[] {m});
        proxy.setReliableEndpoint(rme);

        RMManager manager = control.createMock(RMManager.class);
        EasyMock.expect(rme.getManager()).andReturn(manager);
        Bus bus = control.createMock(Bus.class);
        EasyMock.expect(manager.getBus()).andReturn(bus);
        Endpoint endpoint = control.createMock(Endpoint.class);
        EasyMock.expect(rme.getEndpoint()).andReturn(endpoint);
        BindingInfo bi = control.createMock(BindingInfo.class);
        EasyMock.expect(rme.getBindingInfo()).andReturn(bi);

        Conduit conduit = control.createMock(Conduit.class);
        EasyMock.expect(rme.getConduit()).andReturn(conduit);
        org.apache.cxf.ws.addressing.EndpointReferenceType replyTo 
            = control.createMock(org.apache.cxf.ws.addressing.EndpointReferenceType.class);
        EasyMock.expect(rme.getReplyTo()).andReturn(replyTo);
        
        OperationInfo oi = control.createMock(OperationInfo.class);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        EasyMock.expect(bi.getOperation(oi)).andReturn(boi);
        Client client = control.createMock(Client.class);
        EasyMock.expect(proxy.createClient(bus, endpoint, conduit, replyTo)).andReturn(client);  
        Object[] args = new Object[] {};
        Map<String, Object> context = new HashMap<String, Object>();
        Object[] results = new Object[] {"a", "b", "c"};
        EasyMock.expect(client.invoke(boi, args, context)).andReturn(results);        
        
        control.replay();
        assertEquals("a", proxy.invoke(oi, args, context));
    }
    
    @Test 
    public void testRMClientConstruction() {
        Proxy proxy = new Proxy(rme);
        Bus bus = control.createMock(Bus.class);
        Endpoint endpoint = control.createMock(Endpoint.class);
        Conduit conduit = control.createMock(Conduit.class);
        org.apache.cxf.ws.addressing.EndpointReferenceType address = 
            control.createMock(org.apache.cxf.ws.addressing.EndpointReferenceType.class);
        control.replay();
        assertNotNull(proxy.createClient(bus, endpoint, conduit, address));
    }
    
    @Test 
    public void testRMClientGetConduit() {
        Proxy proxy = new Proxy(rme);
        Bus bus = control.createMock(Bus.class);
        Endpoint endpoint = control.createMock(Endpoint.class);
        Conduit conduit = control.createMock(Conduit.class);
        org.apache.cxf.ws.addressing.EndpointReferenceType address = 
            control.createMock(org.apache.cxf.ws.addressing.EndpointReferenceType.class);
        Proxy.RMClient client = proxy.new RMClient(bus, endpoint, conduit, address);
        EndpointInfo endpointInfo = control.createMock(EndpointInfo.class);
        EasyMock.expect(endpoint.getEndpointInfo()).andReturn(endpointInfo).anyTimes();
        String oa = "oa";
        EasyMock.expect(endpointInfo.getAddress()).andReturn(oa);
        AttributedURIType uri = control.createMock(AttributedURIType.class);
        EasyMock.expect(address.getAddress()).andReturn(uri);
        String ta = "ta";
        EasyMock.expect(uri.getValue()).andReturn(ta);
        endpointInfo.setAddress(ta);
        EasyMock.expectLastCall();
        endpointInfo.setAddress(oa);
        EasyMock.expectLastCall();
        control.replay();
        assertSame(conduit, client.getConduit());    
    }
    
    
    
    private void testCreateSequence(boolean isServer) throws NoSuchMethodException, IOException {
        Method m = Proxy.class.getDeclaredMethod("invoke", 
            new Class[] {OperationInfo.class, Object[].class, Map.class});
        Proxy proxy = control.createMock(Proxy.class, new Method[] {m});
        proxy.setReliableEndpoint(rme);
        
        RMManager manager = control.createMock(RMManager.class);
        EasyMock.expect(rme.getManager()).andReturn(manager);
        SourcePolicyType sp = control.createMock(SourcePolicyType.class);
        EasyMock.expect(manager.getSourcePolicy()).andReturn(sp);
        EasyMock.expect(sp.getAcksTo()).andReturn(null);
        Duration d = DatatypeFactory.createDuration("PT12H");
        EasyMock.expect(sp.getSequenceExpiration()).andReturn(d);
        EasyMock.expect(sp.isIncludeOffer()).andReturn(true);
        Duration dOffered = DatatypeFactory.createDuration("PT24H");
        EasyMock.expect(sp.getOfferedSequenceExpiration()).andReturn(dOffered);
        Source source = control.createMock(Source.class);
        EasyMock.expect(rme.getSource()).andReturn(source);
        Identifier offeredId = control.createMock(Identifier.class);
        EasyMock.expect(source.generateSequenceIdentifier()).andReturn(offeredId);
             
        Endpoint endpoint = control.createMock(Endpoint.class);
        EasyMock.expect(rme.getEndpoint()).andReturn(endpoint);
        EndpointInfo epi = control.createMock(EndpointInfo.class);
        EasyMock.expect(endpoint.getEndpointInfo()).andReturn(epi);
        ServiceInfo si = control.createMock(ServiceInfo.class);
        EasyMock.expect(epi.getService()).andReturn(si);
        InterfaceInfo ii = control.createMock(InterfaceInfo.class);
        EasyMock.expect(si.getInterface()).andReturn(ii);
        OperationInfo oi = control.createMock(OperationInfo.class);
        CreateSequenceResponseType csr = control.createMock(CreateSequenceResponseType.class);
        if (isServer) {
            EasyMock.expect(ii.getOperation(RMConstants.getCreateSequenceOnewayOperationName()))
                .andReturn(oi);
            Endpoint ae = control.createMock(Endpoint.class);
            EasyMock.expect(rme.getApplicationEndpoint()).andReturn(ae);
            EasyMock.expect(ae.getExecutor()).andReturn(SynchronousExecutor.getInstance());
            expectInvoke(proxy, oi, null);
        } else {
            EasyMock.expect(ii.getOperation(RMConstants.getCreateSequenceOperationName()))
                .andReturn(oi);
            expectInvoke(proxy, oi, csr);
        }
        
        EndpointReferenceType defaultAcksTo = control.createMock(EndpointReferenceType.class);
        RelatesToType relatesTo = control.createMock(RelatesToType.class);
        control.replay();
        if (isServer) {
            assertNull(proxy.createSequence(defaultAcksTo, relatesTo, isServer));
        } else {
            assertSame(csr, proxy.createSequence(defaultAcksTo, relatesTo, isServer));
        }
    }
    
    @SuppressWarnings("unchecked")
    private void expectInvoke(Proxy proxy, OperationInfo oi, Object expectedReturn) {
        EasyMock.expect(proxy.invoke(EasyMock.same(oi), EasyMock.isA(Object[].class), 
            (Map)EasyMock.isNull())).andReturn(expectedReturn);
    }
}
