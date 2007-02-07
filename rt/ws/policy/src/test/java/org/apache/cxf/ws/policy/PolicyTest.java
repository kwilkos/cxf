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

import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.helpers.CastUtils;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyOperator;

/**
 * 
 */
public class PolicyTest extends TestCase {

    private static final String INDENT = "  ";
    
    public void testNothing() {
    }
    
    public void xtestMergeIdentical() {
        Policy p1 = new Policy();
        Assertion a1 = new TestAssertion(new QName("http://x.y.z", "a"));
        p1.addPolicyComponent(a1);
        System.out.println("Policy p1:");
        printPolicyComponent(p1);
        
        Policy p2 = new Policy();
        Assertion a2 = new TestAssertion(new QName("http://x.y.z", "b"));
        p2.addPolicyComponent(a2);
        System.out.println("Policy p2:");
        printPolicyComponent(p2);
        
        Policy p3 = new Policy();
        p3.addPolicyComponent(a1);
        System.out.println("Policy p3:");
        printPolicyComponent(p3);
        
        Policy p = p1.merge(p2);
        System.out.println("p1 merged with p2:");
        printPolicyComponent(p);
        
        System.out.println("normalised merge result:");
        printPolicyComponent(p.normalize(true));
        
        p = p1.merge(p3);
        System.out.println("p1 merged with p3:");
        printPolicyComponent(p);
        
        System.out.println("normalised merge result:");
        printPolicyComponent(p.normalize(true));
        
        
        
        
    }
    
    private void printPolicyComponent(PolicyComponent pc) {
        StringBuffer buf = new StringBuffer();
        printPolicyComponent(pc, buf, 0);
        System.out.println(buf.toString());
    }
    
    private void printPolicyComponent(PolicyComponent pc, StringBuffer buf, int level) {
        indent(buf, level);
        buf.append("type: ");
        buf.append(typeToString(pc.getType()));
        if (Constants.TYPE_ASSERTION == pc.getType()) {
            buf.append(" (");
            buf.append(((Assertion)pc).getName());
            buf.append(")");
            nl(buf);
        } else {
            level++;
            List<PolicyComponent> children = CastUtils.cast(((PolicyOperator)pc).getPolicyComponents(),
                PolicyComponent.class);
            nl(buf);
            for (PolicyComponent child : children) {
                printPolicyComponent(child, buf, level);
            }
            level--;
        }
    }
    
    private void indent(StringBuffer buf, int level) {
        for (int i = 0; i < level; i++) {
            buf.append(INDENT);
        }
    }
    
    private void nl(StringBuffer buf) {
        buf.append(System.getProperty("line.separator"));
    }
    
    private String typeToString(short type) {
        switch(type) {
        case Constants.TYPE_ASSERTION:
            return "Assertion";
        case Constants.TYPE_ALL:
            return "All";
        case Constants.TYPE_EXACTLYONE:
            return "ExactlyOne";
        case Constants.TYPE_POLICY:
            return "Policy";
        case Constants.TYPE_POLICY_REF:
            return "PolicyReference";
        default:
            break;
        }
        return "";
    }
    
    
}
