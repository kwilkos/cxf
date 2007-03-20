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

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.policy.builder.primitive.NestedPrimitiveAssertion;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyOperator;

/**
 * 
 */
public final class PolicyUtils {

    private static final String INDENT = "  ";
    
    private PolicyUtils() {
    }

    /**
     * Determine if current messaging role is that of requestor.
     * 
     * @param message the current Message
     * @return true iff the current messaging role is that of requestor
     */
    public static boolean isRequestor(Message message) {
        Boolean requestor = (Boolean)message.get(Message.REQUESTOR_ROLE);
        return requestor != null && requestor.booleanValue();
    }
    
    /**
     * Determine if the current message is a partial response.
     * 
     * @param message the current message
     * @return true iff the current messags is a partial response
     */
    public static boolean isPartialResponse(Message message) {
        return Boolean.TRUE.equals(message.get(Message.PARTIAL_RESPONSE_MESSAGE));
    }
    
    /**
     * Determine if a collection of assertions contains a given assertion, using
     * the equal method from the Assertion interface.
     * 
     * @param assertions a collection of assertions
     * @param candidate the assertion to test
     * @return true iff candidate is equal to one of the assertions in the collection
     */
    public static boolean contains(Collection<Assertion> assertions, Assertion candidate) {
        for (Assertion a : assertions) {
            if (a.equal(candidate)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determine if one collection of assertions contains another collection of assertion, using
     * the equal method from the Assertion interface.
     * 
     * @param assertions a collection of assertions
     * @param candidates the collections of assertion to test
     * @return true iff each candidate is equal to one of the assertions in the collection
     */
    public static boolean contains(Collection<Assertion> assertions, 
                                   Collection<Assertion> candidates) {
        if (null == candidates || candidates.isEmpty()) {
            return true;
        }
        for (Assertion c : candidates) {
            if (!contains(assertions, c)) {
                return false;
            }
        }
        return true;
    }
    
    public static void logPolicy(Logger log, Level level, String msg, PolicyComponent pc) {
        if (null == pc) {
            log.log(level, msg);
            return;
        }
        StringBuffer buf = new StringBuffer();
        buf.append(msg);
        nl(buf);
        printPolicyComponent(pc, buf, 0);
        log.log(level, buf.toString());
    }
    
    public static void printPolicyComponent(PolicyComponent pc) {
        StringBuffer buf = new StringBuffer();
        printPolicyComponent(pc, buf, 0);
        System.out.println(buf.toString());
    }
    
    public static void printPolicyComponent(PolicyComponent pc, StringBuffer buf, int level) {
        indent(buf, level);
        buf.append("type: ");
        buf.append(typeToString(pc.getType()));
        if (Constants.TYPE_ASSERTION == pc.getType()) {
            buf.append(" ");
            buf.append(((Assertion)pc).getName());
            if (((Assertion)pc).isOptional()) {
                buf.append(" (optional)");
            }
            nl(buf);
            if (pc instanceof NestedPrimitiveAssertion) {
                PolicyComponent nested = ((NestedPrimitiveAssertion)pc).getNested();
                level++;
                printPolicyComponent(nested, buf, level);
                level--;                
            }
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
    
    private static void indent(StringBuffer buf, int level) {
        for (int i = 0; i < level; i++) {
            buf.append(INDENT);
        }
    }
    
    private static void nl(StringBuffer buf) {
        buf.append(System.getProperty("line.separator"));
    }
    
    private static String typeToString(short type) {
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
