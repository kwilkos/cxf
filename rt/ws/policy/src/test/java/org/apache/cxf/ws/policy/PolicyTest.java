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
import java.util.List;

import javax.xml.namespace.QName;


import org.apache.cxf.ws.policy.builder.primitive.PrimitiveAssertion;
import org.apache.neethi.All;
import org.apache.neethi.Assertion;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 */
public class PolicyTest extends Assert {
 
    private static final String TEST_NS_URI = "http://cxf.apache.org/test";
    
    @Test
    public void testContains() {
        List<Assertion> alt1 = new ArrayList<Assertion>();
        Assertion a11 = new PrimitiveAssertion(new QName("http://x.y.z", "a1"));
        alt1.add(a11);
        Assertion a12 = new PrimitiveAssertion(new QName("http://x.y.z", "a2"));
        alt1.add(a12);
        
        List<Assertion> alt2 = new ArrayList<Assertion>();
        Assertion a21 = new PrimitiveAssertion(new QName("http://x.y.z", "a1"));
        alt2.add(a21);

        assertTrue("second alternative should be contained in first alternative",
                   PolicyUtils.contains(alt1, alt2));    
    }
    
    @Test
    @Ignore ("need to change the print to assert check point")
    public void testMergeIdentical() {
        Policy p1 = new Policy();
        Assertion a1 = new TestAssertion(new QName("http://x.y.z", "a"));
        p1.addPolicyComponent(a1);
        System.out.println("Policy p1:");
        PolicyUtils.printPolicyComponent(p1);
        
        Policy p2 = new Policy();
        Assertion a2 = new TestAssertion(new QName("http://x.y.z", "b"));
        p2.addPolicyComponent(a2);
        System.out.println("Policy p2:");
        PolicyUtils.printPolicyComponent(p2);
        
        Policy p3 = new Policy();
        p3.addPolicyComponent(a1);
        System.out.println("Policy p3:");
        PolicyUtils.printPolicyComponent(p3);
        
        Policy p = p1.merge(p2);
        System.out.println("p1 merged with p2:");
        PolicyUtils.printPolicyComponent(p);
        
        System.out.println("normalised merge result:");
        PolicyUtils.printPolicyComponent(p.normalize(true));
        
        p = p1.merge(p3);
        System.out.println("p1 merged with p3:");
        PolicyUtils.printPolicyComponent(p);
        
        System.out.println("normalised merge result:");
        PolicyUtils.printPolicyComponent(p.normalize(true));    
    }
    
    @Test
    @Ignore ("need to change the print to assert check point")
    public void testNormalisePrimitives() {
        Policy p;
        /*
        p = getOneOptionalAssertion();
        doNormalise(p, true);
        
        p = getOneAssertion();
        doNormalise(p, true);
        */
        
        p = getTwoOptionalAssertions();
        doNormalise(p, true);
     
    }  
    
    @Test
    @Ignore ("need to change the print to assert check point")
    public void testMergePolciesWithAlternatives() {
        String uri1 = "http://x.y.z";
        Policy p1 = new Policy();
        ExactlyOne ea = new ExactlyOne();
        p1.addPolicyComponent(ea);
        All all = new All();
        ea.addPolicyComponent(all);
        all.addPolicyComponent(new PrimitiveAssertion(new QName(uri1, "a1")));
        all = new All();
        ea.addPolicyComponent(all);
        all.addPolicyComponent(new PrimitiveAssertion(new QName(uri1, "a2")));
        
        String uri2 = "http://a.b.c";
        Policy p2 = new Policy();
        ea = new ExactlyOne();
        p2.addPolicyComponent(ea);
        all = new All();
        ea.addPolicyComponent(all);
        all.addPolicyComponent(new PrimitiveAssertion(new QName(uri2, "x1")));
        all = new All();
        ea.addPolicyComponent(all);
        all.addPolicyComponent(new PrimitiveAssertion(new QName(uri2, "x2")));
        
        System.out.println("p1:");
        PolicyUtils.printPolicyComponent(p1);
        System.out.println();
        System.out.println("p2:");
        PolicyUtils.printPolicyComponent(p1);
        System.out.println();
        Policy p = p1.merge(p2);
        System.out.println("p1 merge p2:");
        PolicyUtils.printPolicyComponent(p);
        System.out.println();
        System.out.println("normalised merge:");
        PolicyUtils.printPolicyComponent(p.normalize(true));
        System.out.println();
    }
    
    Policy getOneAssertion() {
        Policy p = new Policy();
        p.addAssertion(new PrimitiveAssertion(new QName(TEST_NS_URI, "AnonymousResponses"), false));
        return p;
    }
    
    Policy getOneOptionalAssertion() {
        Policy p = new Policy();
        p.addAssertion(new PrimitiveAssertion(new QName(TEST_NS_URI, "AnonymousResponses"), true));
        return p;
    }
    
    Policy getTwoOptionalAssertions() {
        Policy p = new Policy();
        p.addAssertion(new PrimitiveAssertion(new QName(TEST_NS_URI, "AnonymousResponses"), true));
        p.addAssertion(new PrimitiveAssertion(new QName(TEST_NS_URI, "NonAnonymousResponses"), true));
        return p;
    }
    
    private void doNormalise(Policy p, boolean deep) {
        System.out.println("compact form:");
        PolicyUtils.printPolicyComponent(p);
        System.out.println();
        
        if (deep) {
            System.out.println("normalised form (deep):");
        } else {
            System.out.println("normalised form (shallow):");
        }
        PolicyUtils.printPolicyComponent(p.normalize(true));
        System.out.println();
    }
   
}
