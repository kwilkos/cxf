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

import junit.framework.TestCase;

import org.apache.cxf.ws.policy.builders.AssertionBuilder;
import org.apache.cxf.ws.policy.builders.xml.XMLPrimitiveAssertionBuilder;
import org.apache.neethi.Policy;
import org.apache.neethi.util.PolicyComparator;

public class NormalizeTest extends TestCase {
    
    public void testNormalise() throws Exception {
        
        PolicyBuilder builder = new PolicyBuilder();
        AssertionBuilderRegistry abr = new AssertionBuilderRegistry();
        builder.setAssertionBuilderRegistry(abr);
        AssertionBuilder ab = new XMLPrimitiveAssertionBuilder(); 
        abr.registerBuilder(new QName("http://schemas.xmlsoap.org/ws/2002/12/secext", "SecurityToken"), ab);
        abr.registerBuilder(new QName("http://schemas.xmlsoap.org/ws/2002/12/secext", "SecurityHeader"), ab);
        abr.registerBuilder(new QName("http://schemas.xmlsoap.org/ws/2002/12/secext", "Integrity"), ab);
        abr.registerBuilder(new QName("http://sample.org/Assertions", "A"), ab);
        abr.registerBuilder(new QName("http://sample.org/Assertions", "B"), ab);
        abr.registerBuilder(new QName("http://sample.org/Assertions", "C"), ab);
        
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
