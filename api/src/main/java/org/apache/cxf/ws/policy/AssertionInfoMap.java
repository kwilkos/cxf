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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.helpers.CastUtils;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * 
 */
public class AssertionInfoMap extends HashMap<QName, Collection<AssertionInfo>> {
    
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(AssertionInfoMap.class, "APIMessages");
    
    public AssertionInfoMap(Collection<Assertion> assertions) {
        super(assertions.size());
        for (Assertion a : assertions) {
            AssertionInfo ai = new AssertionInfo(a);
            Collection<AssertionInfo> ais = get(a.getName());
            if (null == ais) {
                ais = new ArrayList<AssertionInfo>();
                put(a.getName(), ais);
            }
            ais.add(ai);
        }
    }
    
    public boolean supportsAlternative(Collection<Assertion> alternative) {
        for (Assertion a : alternative) {          
            boolean asserted = false;
            Collection<AssertionInfo> ais = get(a.getName());
            if (null != ais) {
                for (AssertionInfo ai : ais) {
                    // if (ai.getAssertion() == a && ai.isAsserted()) {
                    if (ai.getAssertion().equal(a) && ai.isAsserted()) {
                        asserted = true;
                        break;
                    }
                }
            }
            if (!asserted) {
                return false;
            }
        }
        
        return true;
    }
    
    public void checkEffectivePolicy(Policy policy) {
        Iterator alternatives = policy.getAlternatives();
        while (alternatives.hasNext()) {      
            List<Assertion> alternative = CastUtils.cast((List)alternatives.next(), Assertion.class);
            if (supportsAlternative(alternative)) {
                return;
            }
        }
        throw new PolicyException(new Message("NO_ALTERNATIVE_EXC", BUNDLE));
    }
    
    public void check() {
        for (Collection<AssertionInfo> ais : values()) {
            for (AssertionInfo ai : ais) {
                if (!ai.isAsserted()) {
                    throw new PolicyException(new org.apache.cxf.common.i18n.Message(
                        "NOT_ASSERTED_EXC", BUNDLE, ai.getAssertion().getName()));
                }
            }
        }
    }
}
