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

package org.apache.cxf.ws.policy.builder.primitive;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.namespace.QName;

import org.apache.cxf.ws.policy.AssertionBuilderRegistry;
import org.apache.cxf.ws.policy.AssertionBuilderRegistryImpl;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.util.PolicyComparator;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * 
 */
public class NestedPrimitiveAssertionTest extends Assert {

    private static final String TEST_NAMESPACE = "http://www.w3.org/2007/01/addressing/metadata";
    private static final QName TEST_NAME1 = new QName(TEST_NAMESPACE, "Addressing");
    private static final QName TEST_NAME2 = new QName(TEST_NAMESPACE, "AnonymousResponses");
    private static final QName TEST_NAME3 = new QName(TEST_NAMESPACE, "NonAnonymousResponses");
    
    private String originalNamespace;

    private PolicyBuilderImpl builder;
    
    @Before
    public void setUp() {
        AssertionBuilderRegistry abr = new AssertionBuilderRegistryImpl();
        builder = new PolicyBuilderImpl();
        builder.setAssertionBuilderRegistry(abr);
        
        NestedPrimitiveAssertionBuilder npab = new NestedPrimitiveAssertionBuilder();
        npab.setPolicyBuilder(builder);
        npab.setKnownElements(Collections.singletonList(TEST_NAME1));
        abr.register(TEST_NAME1, npab);
        
        PrimitiveAssertionBuilder pab = new PrimitiveAssertionBuilder();
        Collection<QName> known = new ArrayList<QName>();
        known.add(TEST_NAME2);
        known.add(TEST_NAME3);
        pab.setKnownElements(known);
        abr.register(TEST_NAME2, pab);
        abr.register(TEST_NAME3, pab); 
        
        originalNamespace = PolicyConstants.getNamespace();
        PolicyConstants.setNamespace(PolicyConstants.NAMESPACE_XMLSOAP_200409);
    }
    
    @After
    public void tearDown() {
        PolicyConstants.setNamespace(originalNamespace);
    }
    
    @Test
    public void testBuildNonNested() throws Exception {
        String resource = "resources/compact1.xml";
        InputStream is = NestedPrimitiveAssertionTest.class.getResourceAsStream(resource);
        Policy policy = builder.getPolicy(is);
        assertEquals(1, policy.getPolicyComponents().size());
        PolicyComponent pc = policy.getFirstPolicyComponent();
        assertTrue(pc instanceof NestedPrimitiveAssertion);
        NestedPrimitiveAssertion npc = (NestedPrimitiveAssertion)pc;
        assertEquals(TEST_NAME1, npc.getName());
        Policy nested = npc.getNested();
        assertTrue(nested.isEmpty());
    }
    
    @Test
    public void testBuildNested() throws Exception {
        String resource = "resources/compact3.xml";
        InputStream is = NestedPrimitiveAssertionTest.class.getResourceAsStream(resource);
        Policy policy = builder.getPolicy(is);
        assertEquals(1, policy.getPolicyComponents().size());
        PolicyComponent pc = policy.getFirstPolicyComponent();
        assertTrue(pc instanceof NestedPrimitiveAssertion);
        NestedPrimitiveAssertion npc = (NestedPrimitiveAssertion)pc;
        assertEquals(TEST_NAME1, npc.getName());
        Policy nested = npc.getNested();
        assertEquals(2, nested.getPolicyComponents().size());
        Assertion a1 = (Assertion)(nested.getPolicyComponents().get(0));
        assertTrue(a1 instanceof PrimitiveAssertion);
        assertTrue(TEST_NAME2.equals(a1.getName()) || TEST_NAME3.equals(a1.getName()));
        Assertion a2 = (Assertion)(nested.getPolicyComponents().get(0));
        assertTrue(a2 instanceof PrimitiveAssertion);
        assertTrue(TEST_NAME2.equals(a2.getName()) || TEST_NAME3.equals(a2.getName()));       
    }
    
    
    /**
     * Resources for this tests are taken from WS-Addressing 1.0 Metadata
     * specification
     * http://dev.w3.org/cvsweb/~checkout~/2004/ws/addressing/ws-addr-wsdl.html
     * 
     * @throws Exception
     */
    @Test
    public void testNormalise() throws Exception {    
        
        int n = 6;
        for (int i = 1; i < n; i++) {
            String compact = "resources/compact" + i + ".xml";
            String normalised = "resources/normalised" + i + ".xml";
            
            InputStream compactIn = NestedPrimitiveAssertionTest.class.getResourceAsStream(compact);
            assertNotNull("Could not get input stream for resource " + compact, compactIn);
            InputStream normalisedIn = NestedPrimitiveAssertionTest.class.getResourceAsStream(normalised);
            assertNotNull("Could not get input stream for resource " + normalised, normalisedIn);
                        
            Policy compactPolicy = builder.getPolicy(compactIn);
            Policy expectedNormalisedPolicy = builder.getPolicy(normalisedIn);
            
            assertNotNull(compactPolicy);
            assertNotNull(expectedNormalisedPolicy);
                        
            Policy normalisedPolicy = (Policy)compactPolicy.normalize(true);
            assertNotNull(normalisedPolicy);

            assertTrue("Normalised version of policy defined in compact" + i
                       + ".xml does not match expected version defined in normalised" + i + ".xml",
                       PolicyComparator.compare(expectedNormalisedPolicy, normalisedPolicy));
        }
    }
}
