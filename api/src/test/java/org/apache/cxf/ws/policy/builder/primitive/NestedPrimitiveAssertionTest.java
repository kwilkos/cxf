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

import javax.xml.namespace.QName;

import org.apache.neethi.Assertion;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
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
    
    private Policy[] policies;
    
    
    @Before
    public void setUp() {
        policies = buildTestPolicies();
    }
    
    @Test
    public void testEqual() {
        Assertion other = new PrimitiveAssertion(new QName("abc"));
        for (int i = 0; i < policies.length; i++) {
            Assertion a = (Assertion)policies[i].getFirstPolicyComponent();
            assertTrue("Assertion " + i + " should equal itself.", a.equal(a)); 
            assertTrue("Assertion " + i + " should not equal other.", !a.equal(other)); 
            for (int j = i + 1; j < policies.length; j++) {
                Assertion b = (Assertion)policies[j].getFirstPolicyComponent();
                if (j == 1) {
                    assertTrue("Assertion " + i + " should equal " + j + ".", a.equal(b));
                } else {
                    assertTrue("Assertion " + i + " unexpectedly equals assertion " + j + ".", !a.equal(b));
                }
            }
        }
    }
    
    protected static Policy[] buildTestPolicies() {
        Policy[] p = new Policy[5];
        int i = 0;
        
        p[i] = new Policy();
        NestedPrimitiveAssertion a = new NestedPrimitiveAssertion(TEST_NAME1, true);
        Policy nested = new Policy();
        a.setNested(nested);
        p[i++].addPolicyComponent(a);
        
        p[i] = new Policy();
        a = new NestedPrimitiveAssertion(TEST_NAME1, false);
        nested = new Policy();
        a.setNested(nested);
        p[i++].addPolicyComponent(a);
        
        p[i] = new Policy();
        a = new NestedPrimitiveAssertion(TEST_NAME1, false);
        nested = new Policy();
        a.setNested(nested);
        nested.addPolicyComponent(new PrimitiveAssertion(TEST_NAME2, true));
        nested.addPolicyComponent(new PrimitiveAssertion(TEST_NAME3, true));
        p[i++].addPolicyComponent(a);
        
        p[i] = new Policy();
        a = new NestedPrimitiveAssertion(TEST_NAME1, false);
        nested = new Policy();
        a.setNested(nested);
        ExactlyOne eo = new ExactlyOne();
        nested.addPolicyComponent(eo);
        eo.addPolicyComponent(new PrimitiveAssertion(TEST_NAME2));
        eo.addPolicyComponent(new PrimitiveAssertion(TEST_NAME3));  
        p[i++].addPolicyComponent(a);
        
        p[i] = new Policy();
        a = new NestedPrimitiveAssertion(TEST_NAME1, false);
        nested = new Policy();
        a.setNested(nested);
        nested.addPolicyComponent(new PrimitiveAssertion(TEST_NAME3));  
        p[i++].addPolicyComponent(a); 
        
        return p;
    }
}
