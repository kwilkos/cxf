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

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.interceptor.AbstractAttributedInterceptorProvider;
import org.apache.cxf.ws.policy.attachment.wsdl11.Wsdl11AttachmentPolicyProvider;
import org.apache.neethi.Assertion;

/**
 * 
 */
public class PolicyExtensionsTest extends TestCase {

    private static final QName KNOWN = new QName("http://cxf.apache.org/test/policy", "known");
    private static final QName UNKNOWN = new QName("http://cxf.apache.org/test/policy", "unknown");
    
    public void testExtensions() {
        Bus bus = null;
        try {
            bus = new SpringBusFactory().createBus("/org/apache/cxf/ws/policy/policy-bus.xml", false);

            AssertionBuilderRegistry abr = bus.getExtension(AssertionBuilderRegistry.class);
            assertNotNull(abr);
            AssertionBuilder ab = abr.get(KNOWN);
            assertNotNull(ab);
            ab = abr.get(UNKNOWN);
            assertNull(ab);

            PolicyInterceptorProviderRegistry pipr = bus
                .getExtension(PolicyInterceptorProviderRegistry.class);
            assertNotNull(pipr);
            PolicyInterceptorProvider pip = pipr.get(KNOWN);
            assertNotNull(pip);
            pip = pipr.get(UNKNOWN);
            assertNull(pip);
            
            PolicyEngine engine = bus.getExtension(PolicyEngine.class);
            assertNotNull(engine);            
            assertNotNull(engine.getPolicyProviders());
            assertNotNull(engine.getRegistry());
            
            Collection<PolicyProvider> pps = engine.getPolicyProviders();
            assertEquals(1, pps.size());
            for (PolicyProvider pp : pps) {
                assertTrue(pp instanceof Wsdl11AttachmentPolicyProvider);
            }
            
            PolicyBuilder builder = bus.getExtension(PolicyBuilder.class);
            assertNotNull(builder);
            
        } finally {
            if (null != bus) {
                bus.shutdown(true);
                BusFactory.setDefaultBus(null);
            }
        }
    }
    
    public static class TestAssertionBuilder implements AssertionBuilder {
        
        Collection<QName> knownElements = new ArrayList<QName>();

        public TestAssertionBuilder() {
            knownElements.add(KNOWN);
        }
        public Assertion build(Element arg0) {
            return null;
        }

        public Collection<QName> getKnownElements() {
            return knownElements;
        }        
    }
    
    public static class TestPolicyInterceptorProvider extends AbstractAttributedInterceptorProvider 
        implements PolicyInterceptorProvider {
        Collection<QName> assertionTypes = new ArrayList<QName>();

        public TestPolicyInterceptorProvider() {
            assertionTypes.add(KNOWN);
        }

        public Collection<QName> getAssertionTypes() {
            return assertionTypes;
        } 
    }
}
