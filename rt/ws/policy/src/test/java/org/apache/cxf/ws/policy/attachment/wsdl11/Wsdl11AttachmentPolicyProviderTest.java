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

package org.apache.cxf.ws.policy.attachment.wsdl11;

import java.net.URL;
import java.util.List;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.ws.policy.AssertionBuilder;
import org.apache.cxf.ws.policy.AssertionBuilderRegistry;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyException;
import org.apache.cxf.ws.policy.builders.xml.XMLPrimitiveAssertionBuilder;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.util.PolicyComparator;

/**
 * 
 */
public class Wsdl11AttachmentPolicyProviderTest extends TestCase {

    private static final String NAMESPACE_URI = "http://apache.org/cxf/calculator";
    private static final QName OPERATION_NAME = new QName(NAMESPACE_URI, "add");
    private static Bus bus;
    private static ServiceInfo[] services;
    private static EndpointInfo[] endpoints;
    private Wsdl11AttachmentPolicyProvider app; 
    
    public static Test suite() {
        TestSuite suite = new TestSuite(Wsdl11AttachmentPolicyProviderTest.class);
        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {
                oneTimeSetUp();
            }

            protected void tearDown() {
                oneTimeTearDown();
            }
        };

        return wrapper;
    }
    
    public static void oneTimeSetUp() {
        BusFactory.setDefaultBus(null);
        bus = new SpringBusFactory().createBus();
        WSDLManager manager = bus.getExtension(WSDLManager.class);
        int n = 17;
        services = new ServiceInfo[n];
        endpoints = new EndpointInfo[n];
        for (int i = 0; i < n; i++) {
            String resourceName = "/attachment/wsdl11/test" + i + ".wsdl";
            URL url = Wsdl11AttachmentPolicyProviderTest.class.getResource(resourceName);       
            WSDLServiceBuilder builder = new WSDLServiceBuilder(bus);
            try {
                services[i] = builder.buildService(manager.getDefinition(url)).get(0);
            } catch (WSDLException ex) {
                ex.printStackTrace();
                fail("Failed to build service from resource " + resourceName);
            }
            assertNotNull(services[i]);
            endpoints[i] = services[i].getEndpoints().iterator().next();
            assertNotNull(endpoints[i]);
        }
    }
    
    public static void oneTimeTearDown() {
        bus.shutdown(true);
        BusFactory.setDefaultBus(null);
        endpoints = null;
        services = null;
        
    }
    
    public void setUp() {
        AssertionBuilderRegistry abr = bus.getExtension(AssertionBuilderRegistry.class);
        assertNotNull(abr);
        AssertionBuilder ab = new XMLPrimitiveAssertionBuilder();
        abr.registerBuilder(new QName("http://cxf.apache.org/test/assertions", "A"), ab);
        abr.registerBuilder(new QName("http://cxf.apache.org/test/assertions", "B"), ab);
        abr.registerBuilder(new QName("http://cxf.apache.org/test/assertions", "C"), ab);
        
        PolicyBuilder pb = new PolicyBuilder(); 
        pb.setAssertionBuilderRegistry(abr);
        app = new Wsdl11AttachmentPolicyProvider();
        app.setBuilder(pb);
        
    }
    
    public void testElementPolicies() throws WSDLException {
    
        Policy p;
        
        // no extensions       
        p = app.getElementPolicy(services[0]);
        assertNotNull(p);
        assertTrue(p.isEmpty());
        
        // extensions not of type Policy or PolicyReference
        p = app.getElementPolicy(services[1]);
        assertNotNull(p);
        assertTrue(p.isEmpty());
        
        // one extension of type Policy, without assertion builder
        try {
            p = app.getElementPolicy(services[2]);
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
        
        // one extension of type Policy
        p = app.getElementPolicy(services[3]);
        assertNotNull(p);
        assertTrue(!p.isEmpty());
        verifyAssertionsOnly(p, 2);
        
        // two extensions of type Policy
        p = app.getElementPolicy(services[4]);
        assertNotNull(p);
        assertTrue(!p.isEmpty());
        verifyAssertionsOnly(p, 3);
        
        EndpointInfo ei = new EndpointInfo();
        assertTrue(app.getElementPolicy(ei).isEmpty());
    }
    
    public void testEffectiveServicePolicies() throws WSDLException {
        
        Policy p;
        Policy ep;
        
        // no extensions        
        ep = app.getEffectivePolicy(services[0]);
        assertNotNull(ep);
        assertTrue(ep.isEmpty());
        p = app.getElementPolicy(services[0]);
        assertTrue(PolicyComparator.compare(p, ep));
        
        // extensions not of type Policy or PolicyReference
        ep = app.getEffectivePolicy(services[1]);
        assertNotNull(ep);
        assertTrue(ep.isEmpty());
        
        // one extension of type Policy, without assertion builder
        try {
            ep = app.getEffectivePolicy(services[2]);
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
        
        // one extension of type Policy
        ep = app.getEffectivePolicy(services[3]);
        assertNotNull(ep);
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 2);
        p = app.getElementPolicy(services[3]);
        assertTrue(PolicyComparator.compare(p, ep));
        
        // two extensions of type Policy
        ep = app.getEffectivePolicy(services[4]);
        assertNotNull(ep);
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 3);
        p = app.getElementPolicy(services[4]);
        assertTrue(PolicyComparator.compare(p, ep));
    }

    public void testEffectiveEndpointPolicies() {
        Policy ep;
        Policy p;
        
        // port has no extensions
        // porttype has no extensions
        // binding has no extensions
        ep = app.getEffectivePolicy(endpoints[0]);
        assertNotNull(ep);
        assertTrue(ep.isEmpty());
        
        // port has one extension of type Policy        
        // binding has no extensions
        // porttype has no extensions
        ep = app.getEffectivePolicy(endpoints[5]);
        assertNotNull(ep);
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 1);
        p = app.getElementPolicy(endpoints[5]);
        assertTrue(PolicyComparator.compare(p, ep));
        
        // port has no extensions
        // binding has one extension of type Policy
        // porttype has no extensions
        ep = app.getEffectivePolicy(endpoints[6]);
        assertNotNull(ep);
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 1);
        p = app.getElementPolicy(endpoints[6].getBinding());
        assertTrue(PolicyComparator.compare(p, ep));
        
        // port has no extensions
        // binding has no extensions
        // porttype has one extension of type Policy
        ep = app.getEffectivePolicy(endpoints[7]);
        assertNotNull(ep);
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 1);
        p = app.getElementPolicy(endpoints[7].getInterface());
        assertTrue(PolicyComparator.compare(p, ep));
        
        // port has one extension of type Policy
        // porttype has one extension of type Policy
        // binding has one extension of type Policy
        ep = app.getEffectivePolicy(endpoints[8]);
        assertNotNull(ep);
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 3);
        
    }
    
    public void testEffectiveBindingOperationPolicies() {
        Policy ep;
        
        // operation has no extensions
        // binding operation has no extensions
        ep = app.getEffectivePolicy(getBindingOperationInfo(endpoints[0]));
        assertNotNull(ep);
        assertTrue(ep.isEmpty());
        
        // operation has no extensions
        // binding operation has one extension of type Policy
        ep = app.getEffectivePolicy(getBindingOperationInfo(endpoints[9]));
        assertNotNull(ep);
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 1);
        
        // operation has one extension of type Policy
        // binding operation has no extensions
        ep = app.getEffectivePolicy(getBindingOperationInfo(endpoints[10]));
        assertNotNull(ep);
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 2);
        
        // operation has one extension of type Policy
        // binding operation one extension of type Policy
        ep = app.getEffectivePolicy(getBindingOperationInfo(endpoints[11]));
        assertNotNull(ep);
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 3);
    }
    
    public void testEffectiveMessagePolicies() {
        Policy ep;
        
        // binding operation message has no extensions
        // operation message has no extensions
        // message has no extensions
        ep = app.getEffectivePolicy(getBindingMessageInfo(endpoints[0], true));
        assertNotNull(ep);
        assertTrue(ep.isEmpty());
        
        // binding operation message has one extension of type Policy
        // operation message has no extensions
        // message has no extensions
        ep = app.getEffectivePolicy(getBindingMessageInfo(endpoints[12], true));
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 1);
        
        // binding operation message has no extensions
        // operation message has one extension of type Policy
        // message has no extensions  
        ep = app.getEffectivePolicy(getBindingMessageInfo(endpoints[13], true));
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 1);
        
        // binding operation message has no extensions
        // operation message has no extensions
        // message has one extension of type Policy
        ep = app.getEffectivePolicy(getBindingMessageInfo(endpoints[14], true));
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 1);
        
        // binding operation message has one extension of type Policy
        // operation message has one extension of type Policy
        // message has one extension of type Policy
        ep = app.getEffectivePolicy(getBindingMessageInfo(endpoints[15], true));
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 3);      
    }
    
    public void testResolveLocal() {
        
        Policy ep;
        
        // service has one extension of type PolicyReference, reference can be resolved locally
        ep = app.getElementPolicy(services[16]);
        assertNotNull(ep);
        verifyAssertionsOnly(ep, 2);
        
        // port has one extension of type PolicyReference, reference cannot be resolved locally
        try {
            app.getElementPolicy(endpoints[16]);
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
        
        // binding has one extension of type PolicyReference, reference is external
        try {
            app.getElementPolicy(endpoints[16].getBinding());
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
        
    }
    
    public void testResolveExternal() {
        
    }
    
    private void verifyAssertionsOnly(Policy p, int expectedAssertions) {
        List<PolicyComponent> pcs;
        pcs = CastUtils.cast(p.getAssertions(), PolicyComponent.class);
        assertEquals(expectedAssertions, pcs.size());
        for (int i = 0; i < expectedAssertions; i++) {
            assertEquals(Constants.TYPE_ASSERTION, pcs.get(i).getType());
        }
    }
    
    private BindingOperationInfo getBindingOperationInfo(EndpointInfo ei) {
        return ei.getBinding().getOperation(OPERATION_NAME);        
    }
    
    private BindingMessageInfo getBindingMessageInfo(EndpointInfo ei, boolean in) {
        return in ? ei.getBinding().getOperation(OPERATION_NAME).getInput()
            : ei.getBinding().getOperation(OPERATION_NAME).getOutput();
    }
    
    
}
