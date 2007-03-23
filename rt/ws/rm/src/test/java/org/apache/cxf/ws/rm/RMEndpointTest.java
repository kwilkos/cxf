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
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.policy.EndpointPolicyInfo;
import org.apache.cxf.ws.policy.OutPolicyInfo;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistry;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RMEndpointTest extends TestCase {
    
    private IMocksControl control;
    private RMManager manager;
    private Endpoint ae;
    private RMEndpoint rme;
    
    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
        manager = control.createMock(RMManager.class);
        ae = control.createMock(Endpoint.class);
        rme = new RMEndpoint(manager, ae);
    }
    
    @After
    public void tearDown() {
        control.verify();
    }
    
    @Test
    public void testConstructor() {
        control.replay();
        assertNotNull(rme);
        assertNull(rme.getEndpoint());
        assertNull(rme.getService());
        assertNull(rme.getConduit());
        assertNull(rme.getReplyTo());
    }
    
    @Test
    public void testGetName() {
        EndpointInfo aei = control.createMock(EndpointInfo.class);        
        EasyMock.expect(ae.getEndpointInfo()).andReturn(aei);
        QName qn = new QName("cxf");
        EasyMock.expect(aei.getName()).andReturn(qn);
        control.replay();
        assertSame(qn, rme.getName());
    }
    
    @Test
    public void testGetManager() {
        control.replay();
        assertSame(manager, rme.getManager());
    }
    
    @Test
    public void testGetApplicationEndpoint() {
        control.replay();
        assertSame(ae, rme.getApplicationEndpoint());
    }
    
    @Test
    public void testGetProxy() {
        control.replay();
        assertSame(rme, rme.getProxy().getReliableEndpoint());
    }
    
    @Test
    public void testGetServant() {
        control.replay();
        assertNotNull(rme.getServant());
    }
    
    @Test
    public void testGetSetDestination() {
        Destination d = control.createMock(Destination.class);
        control.replay();
        assertSame(rme, rme.getDestination().getReliableEndpoint());
        rme.setDestination(d);
        assertSame(d, rme.getDestination());        
    }
    
    @Test
    public void testGetSetSource() {
        Source s = control.createMock(Source.class);
        control.replay();
        assertSame(rme, rme.getSource().getReliableEndpoint());
        rme.setSource(s);
        assertSame(s, rme.getSource());        
    }
    
    @Test
    public void testInitialise() throws NoSuchMethodException {
        Method m1 = RMEndpoint.class.getDeclaredMethod("createService", new Class[] {});
        Method m2 = RMEndpoint.class.getDeclaredMethod("createEndpoint", new Class[] {});
        Method m3 = RMEndpoint.class.getDeclaredMethod("setPolicies", new Class[] {});
        
        rme = control.createMock(RMEndpoint.class, new Method[] {m1, m2, m3});
        rme.createService();
        EasyMock.expectLastCall();
        rme.createEndpoint();
        EasyMock.expectLastCall();
        rme.setPolicies();
        EasyMock.expectLastCall();
        Conduit c = control.createMock(Conduit.class);
        org.apache.cxf.ws.addressing.EndpointReferenceType epr = 
            control.createMock(org.apache.cxf.ws.addressing.EndpointReferenceType.class);        
        control.replay();
        rme.initialise(c, epr);
        assertSame(c, rme.getConduit());
        assertSame(epr, rme.getReplyTo());  
    }
    
    @Test
    public void testCreateService() {
        Service as = control.createMock(Service.class);
        EasyMock.expect(ae.getService()).andReturn(as);
        control.replay();
        rme.createService();
        Service s = rme.getService();
        assertNotNull(s);
        WrappedService ws = (WrappedService)s;
        assertSame(as, ws.getWrappedService());
        assertSame(rme.getServant(), s.getInvoker());
        verifyService();
    }
    
    @Test
    public void testCreateEndpoint() throws NoSuchMethodException {
        Method m = RMEndpoint.class.getDeclaredMethod("getUsingAddressing", new Class[] {EndpointInfo.class});
        rme = control.createMock(RMEndpoint.class, new Method[] {m});
        rme.setAplicationEndpoint(ae);
        rme.setManager(manager);  
        Service as = control.createMock(Service.class);
        EasyMock.expect(ae.getService()).andReturn(as);
        EndpointInfo aei = control.createMock(EndpointInfo.class);
        EasyMock.expect(ae.getEndpointInfo()).andReturn(aei).times(2);
        BindingInfo bi = control.createMock(BindingInfo.class);
        EasyMock.expect(aei.getBinding()).andReturn(bi);
        String ns = "http://schemas.xmlsoap.org/wsdl/soap/";
        EasyMock.expect(bi.getBindingId()).andReturn(ns);
        EasyMock.expect(aei.getTransportId()).andReturn(ns);
        String addr = "addr";
        EasyMock.expect(aei.getAddress()).andReturn(addr);
        Object ua = new Object();
        EasyMock.expect(rme.getUsingAddressing(aei)).andReturn(ua);
        control.replay();
        rme.createService();
        rme.createEndpoint();
        Endpoint e = rme.getEndpoint();
        WrappedEndpoint we = (WrappedEndpoint)e;
        assertSame(ae, we.getWrappedEndpoint());  
        Service s = rme.getService();
        assertEquals(1, s.getEndpoints().size());
        assertSame(e, s.getEndpoints().get(new QName(RMConstants.getWsdlNamespace(), 
                                                     "SequenceAbstractSoapPort")));   
    }
    
    @Test
    public void testSetPolicies() throws NoSuchMethodException {
        Method m = RMEndpoint.class.getDeclaredMethod("getEndpoint", new Class[] {});
        rme = control.createMock(RMEndpoint.class, new Method[] {m});
        rme.setAplicationEndpoint(ae);
        rme.setManager(manager);
        Endpoint e = control.createMock(Endpoint.class);
        EasyMock.expect(rme.getEndpoint()).andReturn(e);
        Bus bus = control.createMock(Bus.class);
        EasyMock.expect(manager.getBus()).andReturn(bus);
        PolicyEngine pe = control.createMock(PolicyEngine.class);
        EasyMock.expect(bus.getExtension(PolicyEngine.class)).andReturn(pe);
        EndpointPolicyInfo epi = control.createMock(EndpointPolicyInfo.class);
        EasyMock.expect(pe.getEndpointPolicyInfo(ae, (Conduit)null)).andReturn(epi);
        Policy policy = new Policy();
        EasyMock.expect(epi.getPolicy()).andReturn(policy);
        EasyMock.expect(epi.getChosenAlternative()).andReturn(new ArrayList<Assertion>());
        EasyMock.expect(pe.getBus()).andReturn(bus);
        PolicyInterceptorProviderRegistry reg = control.createMock(PolicyInterceptorProviderRegistry.class);
        EasyMock.expect(bus.getExtension(PolicyInterceptorProviderRegistry.class)).andReturn(reg);
        pe.setEndpointPolicyInfo(e, epi);
        EasyMock.expectLastCall();
        EndpointInfo ei = control.createMock(EndpointInfo.class);
        EasyMock.expect(e.getEndpointInfo()).andReturn(ei);
        BindingInfo bi = control.createMock(BindingInfo.class);
        EasyMock.expect(ei.getBinding()).andReturn(bi);
        BindingOperationInfo boi = control.createMock(BindingOperationInfo.class);
        EasyMock.expect(bi.getOperations()).andReturn(Collections.singletonList(boi));
        pe.setServerRequestPolicyInfo(EasyMock.eq(e), EasyMock.eq(boi), 
                                      EasyMock.isA(OutPolicyInfo.class));
        EasyMock.expectLastCall();
        pe.setServerResponsePolicyInfo(EasyMock.eq(e), EasyMock.eq(boi), 
                                      EasyMock.isA(OutPolicyInfo.class));
        EasyMock.expectLastCall();
        pe.setClientRequestPolicyInfo(EasyMock.eq(e), EasyMock.eq(boi), 
                                      EasyMock.isA(OutPolicyInfo.class));
        EasyMock.expectLastCall();
        pe.setClientResponsePolicyInfo(EasyMock.eq(e), EasyMock.eq(boi), 
                                      EasyMock.isA(OutPolicyInfo.class));
        EasyMock.expectLastCall();
        control.replay();
        rme.setPolicies();
    }
       
    private void verifyService() {        
        Service service = rme.getService();
        ServiceInfo si = service.getServiceInfo();
        assertNotNull("service info is null", si);

        InterfaceInfo intf = si.getInterface();
        
        assertEquals(5, intf.getOperations().size());
        
        String ns = si.getName().getNamespaceURI();
        OperationInfo oi = intf.getOperation(new QName(ns, "CreateSequence"));
        assertNotNull("No operation info.", oi);
        assertTrue("Operation is oneway.", !oi.isOneWay());
        assertTrue("Operation is unwrapped.", !oi.isUnwrapped());
        assertTrue("Operation is unwrappedCapable.", !oi.isUnwrappedCapable());
        assertNull("Unexpected unwrapped operation.", oi.getUnwrappedOperation());
        
        oi = intf.getOperation(new QName(ns, "TerminateSequence"));
        assertNotNull("No operation info.", oi);
        assertTrue("Operation is toway.", oi.isOneWay());
        
        oi = intf.getOperation(new QName(ns, "SequenceAcknowledgement"));
        assertNotNull("No operation info.", oi);
        assertTrue("Operation is toway.", oi.isOneWay());
        
        oi = intf.getOperation(new QName(ns, "CreateSequenceOneway"));
        assertNotNull("No operation info.", oi);
        assertTrue("Operation is toway.", oi.isOneWay());
        
        oi = intf.getOperation(new QName(ns, "CreateSequenceResponseOneway"));
        assertNotNull("No operation info.", oi);
        assertTrue("Operation is toway.", oi.isOneWay());
    }

}
