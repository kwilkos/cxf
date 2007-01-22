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

package org.apache.cxf.ws.policy;

import java.net.URL;
import java.util.List;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.ws.policy.builders.AssertionBuilder;
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
public class PolicyAttachmentManagerTest extends TestCase {

    private static Bus bus;
    private static ServiceInfo[] services;
    private static EndpointInfo[] endpoints;
    private PolicyAttachmentManager pam; 
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PolicyAttachmentManagerTest.class);
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
        bus = new SpringBusFactory().getDefaultBus();
        WSDLManager manager = bus.getExtension(WSDLManager.class);
        int n = 9;
        services = new ServiceInfo[n];
        endpoints = new EndpointInfo[n];
        for (int i = 0; i < n; i++) {
            String resourceName = "/attachment/wsdl11/test" + i + ".wsdl";
            URL url = PolicyAttachmentManagerTest.class.getResource(resourceName);       
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
        new SpringBusFactory().setDefaultBus(null);
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
        pam = new PolicyAttachmentManager();
        pam.setBuilder(pb);
        
    }
    
    public void testElementPolicies() throws WSDLException {
    
        Policy p;
        
        // no extensions       
        p = pam.getElementPolicy(services[0]);
        assertNotNull(p);
        assertTrue(p.isEmpty());
        
        // extensions not of type Policy or PolicyReference
        p = pam.getElementPolicy(services[1]);
        assertNotNull(p);
        assertTrue(p.isEmpty());
        
        // one extension of type Policy, without assertion builder
        try {
            p = pam.getElementPolicy(services[2]);
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
        
        // one extension of type Policy
        p = pam.getElementPolicy(services[3]);
        assertNotNull(p);
        assertTrue(!p.isEmpty());
        verifyAssertionsOnly(p, 2);
        
        // two extensions of type Policy
        p = pam.getElementPolicy(services[4]);
        assertNotNull(p);
        assertTrue(!p.isEmpty());
        verifyAssertionsOnly(p, 3);
    }
    
    public void testServiceSubjectPolicies() throws WSDLException {
        
        Policy p;
        Policy sp;
        
        // no extensions        
        sp = pam.getSubjectPolicy(services[0]);
        assertNotNull(sp);
        assertTrue(sp.isEmpty());
        p = pam.getElementPolicy(services[0]);
        assertTrue(PolicyComparator.compare(p, sp));
        
        // extensions not of type Policy or PolicyReference
        sp = pam.getSubjectPolicy(services[1]);
        assertNotNull(sp);
        assertTrue(sp.isEmpty());
        
        // one extension of type Policy, without assertion builder
        try {
            sp = pam.getSubjectPolicy(services[2]);
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
        
        // one extension of type Policy
        sp = pam.getSubjectPolicy(services[3]);
        assertNotNull(sp);
        assertTrue(!sp.isEmpty());
        verifyAssertionsOnly(sp, 2);
        p = pam.getElementPolicy(services[3]);
        assertTrue(PolicyComparator.compare(p, sp));
        
        // two extensions of type Policy
        sp = pam.getSubjectPolicy(services[4]);
        assertNotNull(sp);
        assertTrue(!sp.isEmpty());
        verifyAssertionsOnly(sp, 3);
        p = pam.getElementPolicy(services[4]);
        assertTrue(PolicyComparator.compare(p, sp));
    }
    
    public void testServiceEffectivePolicies() throws WSDLException {
        
        Policy p;
        Policy ep;
        
        // no extensions        
        ep = pam.getEffectivePolicy(services[0]);
        assertNotNull(ep);
        assertTrue(ep.isEmpty());
        p = pam.getElementPolicy(services[0]);
        assertTrue(PolicyComparator.compare(p, ep));
        
        // extensions not of type Policy or PolicyReference
        ep = pam.getEffectivePolicy(services[1]);
        assertNotNull(ep);
        assertTrue(ep.isEmpty());
        
        // one extension of type Policy, without assertion builder
        try {
            ep = pam.getEffectivePolicy(services[2]);
            fail("Expected PolicyException not thrown.");
        } catch (PolicyException ex) {
            // expected
        }
        
        // one extension of type Policy
        ep = pam.getEffectivePolicy(services[3]);
        assertNotNull(ep);
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 2);
        p = pam.getElementPolicy(services[3]);
        assertTrue(PolicyComparator.compare(p, ep));
        
        // two extensions of type Policy
        ep = pam.getEffectivePolicy(services[4]);
        assertNotNull(ep);
        assertTrue(!ep.isEmpty());
        verifyAssertionsOnly(ep, 3);
        p = pam.getElementPolicy(services[4]);
        assertTrue(PolicyComparator.compare(p, ep));
    }


    public void testEndpointSubjectPolicies() {
        Policy sp;
        Policy p;
        
        // port has no extensions
        // porttype has no extensions
        // binding has no extensions
        sp = pam.getSubjectPolicy(endpoints[0]);
        assertNotNull(sp);
        assertTrue(sp.isEmpty());
        
        // port has one extension of type Policy        
        // binding has no extensions
        // porttype has no extensions
        sp = pam.getSubjectPolicy(endpoints[5]);
        assertNotNull(sp);
        assertTrue(!sp.isEmpty());
        verifyAssertionsOnly(sp, 1);
        p = pam.getElementPolicy(endpoints[5]);
        assertTrue(PolicyComparator.compare(p, sp));
        
        // port has no extensions
        // binding has one extension of type Policy
        // porttype has no extensions
        sp = pam.getSubjectPolicy(endpoints[6]);
        assertNotNull(sp);
        assertTrue(!sp.isEmpty());
        verifyAssertionsOnly(sp, 1);
        p = pam.getElementPolicy(endpoints[6].getBinding());
        assertTrue(PolicyComparator.compare(p, sp));
        
        // port has no extensions
        // binding has no extensions
        // porttype has one extension of type Policy
        sp = pam.getSubjectPolicy(endpoints[7]);
        assertNotNull(sp);
        assertTrue(!sp.isEmpty());
        verifyAssertionsOnly(sp, 1);
        p = pam.getElementPolicy(endpoints[7].getInterface());
        assertTrue(PolicyComparator.compare(p, sp));
        
        // port has one extension of type Policy
        // porttype has one extension of type Policy
        // binding has one extension of type Policy
        sp = pam.getSubjectPolicy(endpoints[8]);
        assertNotNull(sp);
        assertTrue(!sp.isEmpty());
        verifyAssertionsOnly(sp, 3);
        
    }
    
    private void verifyAssertionsOnly(Policy p, int expectedAssertions) {
        List<PolicyComponent> pcs;
        pcs = CastUtils.cast(p.getAssertions(), PolicyComponent.class);
        assertEquals(expectedAssertions, pcs.size());
        for (int i = 0; i < expectedAssertions; i++) {
            assertEquals(Constants.TYPE_ASSERTION, pcs.get(i).getType());
        }
    }
    
    
}
