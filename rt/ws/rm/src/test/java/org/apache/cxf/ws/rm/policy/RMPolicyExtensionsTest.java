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

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.ws.policy.AssertionBuilder;
import org.apache.cxf.ws.policy.AssertionBuilderRegistry;
import org.apache.cxf.ws.policy.PolicyInterceptorProvider;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistry;
import org.apache.cxf.ws.rm.RMConstants;

/**
 * 
 */
public class RMPolicyExtensionsTest extends TestCase {

    public void testExtensions() {
        Bus bus = null;
        try {
            bus = new SpringBusFactory().createBus();

            AssertionBuilderRegistry abr = bus.getExtension(AssertionBuilderRegistry.class);
            assertNotNull(abr);
            AssertionBuilder ab = abr.get(RMConstants.getRMAssertionQName());
            assertNotNull(ab);

            PolicyInterceptorProviderRegistry pipr = bus
                .getExtension(PolicyInterceptorProviderRegistry.class);
            assertNotNull(pipr);
            PolicyInterceptorProvider pip = pipr.get(RMConstants.getRMAssertionQName());
            assertNotNull(pip);
            
            assertEquals(4, pip.getOutInterceptors().size());
            assertEquals(4, pip.getOutFaultInterceptors().size());
            assertEquals(4, pip.getInInterceptors().size());
            assertEquals(3, pip.getInFaultInterceptors().size());
            
            PhaseManager pm = bus.getExtension(PhaseManager.class);
            PhaseInterceptorChain chain = new PhaseInterceptorChain(pm.getOutPhases());
            chain.add(pip.getOutInterceptors());
            
        } finally {
            if (null != bus) {
                bus.shutdown(true);
                BusFactory.setDefaultBus(null);
            }
        }
    }
}
