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
package org.apache.cxf.ws.policy.spring;

import junit.framework.Assert;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.selector.MaximalAlternativeSelector;
import org.junit.After;
import org.junit.Test;

public class PolicyBeansTest extends Assert {
    
    private Bus bus;
    
    @After
    public void shutdown() {
        if (null != bus) {
            bus.shutdown(true);
        }
    }

    @Test
    public void testParse() {
        bus = new SpringBusFactory().createBus("org/apache/cxf/ws/policy/spring/engine.xml");
        PolicyEngine pe = bus.getExtension(PolicyEngine.class);
        assertTrue("Policy engine is not enabled", pe.isEnabled());
        assertTrue("Unknown assertions are not ignored", pe.isIgnoreUnknownAssertions());
        
        assertEquals(MaximalAlternativeSelector.class.getName(), 
                     pe.getAlternativeSelector().getClass().getName()); 
        
        assertEquals("http://www.w3.org/ns/ws-policy",
                     bus.getExtension(PolicyConstants.class).getNamespace());
    }
    
   
}
