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

import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.cxf.ws.policy.builder.xml.XMLPrimitiveAssertionBuilder;
import org.apache.neethi.Policy;
import org.apache.neethi.util.PolicyComparator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NormalizeTest extends Assert {
    
    private String originalNamespace;
    
    
    @Before
    public void setUp() {
        originalNamespace = PolicyConstants.getNamespace();
        PolicyConstants.setNamespace("http://schemas.xmlsoap.org/ws/2004/09/policy");
    }
    
    @After
    public void tearDown() {
        PolicyConstants.setNamespace(originalNamespace);
    }
    
    @Test
    public void testNormalise() throws Exception {
        
        PolicyBuilderImpl builder = new PolicyBuilderImpl();
        AssertionBuilderRegistry abr = new AssertionBuilderRegistryImpl();
        builder.setAssertionBuilderRegistry(abr);
        AssertionBuilder ab = new XMLPrimitiveAssertionBuilder(); 
        abr.register(new QName("http://schemas.xmlsoap.org/ws/2002/12/secext", "SecurityToken"), ab);
        abr.register(new QName("http://schemas.xmlsoap.org/ws/2002/12/secext", "SecurityHeader"), ab);
        abr.register(new QName("http://schemas.xmlsoap.org/ws/2002/12/secext", "Integrity"), ab);
        abr.register(new QName("http://sample.org/Assertions", "A"), ab);
        abr.register(new QName("http://sample.org/Assertions", "B"), ab);
        abr.register(new QName("http://sample.org/Assertions", "C"), ab);
        
        int n = 26;
        for (int i = 1; i < n; i++) {
            String sample = "/samples/test" + i + ".xml";
            String normalised = "/normalized/test" + i + ".xml";
            
            InputStream sampleIn = NormalizeTest.class.getResourceAsStream(sample);
            assertNotNull("Could not get input stream for resource " + sample, sampleIn);
            InputStream normalisedIn = NormalizeTest.class.getResourceAsStream(normalised);
            assertNotNull("Could not get input stream for resource " + normalised, normalisedIn);
                        
            Policy samplePolicy = builder.getPolicy(sampleIn);
            Policy normalisedPolicy = builder.getPolicy(normalisedIn);
            assertNotNull(samplePolicy);
            assertNotNull(normalisedPolicy);
            
            Policy normalisedSamplePolicy = (Policy)samplePolicy.normalize(true);
            assertTrue(PolicyComparator.compare(normalisedPolicy, normalisedSamplePolicy));
            
        }       
    }
}
