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

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.PolicyException;
import org.apache.neethi.All;
import org.apache.neethi.Assertion;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;

/**
 * Implementation of an assertion that required exactly one (possibly empty) child element
 * of type Policy (as does for examples the wsam:Addressing assertion).
 * 
 */
public class NestedPrimitiveAssertion extends PrimitiveAssertion {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(NestedPrimitiveAssertion.class);    
    private Policy nested;
    
    protected NestedPrimitiveAssertion(QName name, boolean optional) {
        super(name, optional);
    }
    
    public NestedPrimitiveAssertion(Element elem, PolicyBuilder builder, PolicyConstants constants) {
        super(elem, constants);
        
        // expect exactly one child element of type Policy
       
        Element policyElem = null;
        for (Node nd = elem.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE == nd.getNodeType()) {
                QName qn = new QName(nd.getNamespaceURI(), nd.getLocalName());
                if (constants.getPolicyElemQName().equals(qn)
                    && null == policyElem) {
                    policyElem = (Element)nd;
                } else {
                    throw new PolicyException(new Message("UNEXPECTED_CHILD_ELEMENT_EXC", BUNDLE, 
                                                          constants.getPolicyElemQName()));
                }                
            }
        }
        if (null == policyElem) {
            throw new PolicyException(new Message("UNEXPECTED_CHILD_ELEMENT_EXC", BUNDLE, 
                                                  constants.getPolicyElemQName()));
        }
        
        nested = builder.getPolicy(policyElem);  
    }
    
    public PolicyComponent normalize() {
        Policy normalisedNested = (Policy)nested.normalize(true);
        
        Policy p = new Policy();
        ExactlyOne ea = new ExactlyOne();
        p.addPolicyComponent(ea);
        if (isOptional()) {
            ea.addPolicyComponent(new All());
        }
        // for all alternatives in normalised nested policy
        Iterator alternatives = normalisedNested.getAlternatives();
        while (alternatives.hasNext()) {
            All all = new All();
            List<Assertion> alternative = CastUtils.cast((List)alternatives.next(), Assertion.class);
            NestedPrimitiveAssertion a = new NestedPrimitiveAssertion(getName(), false);
            a.nested = new Policy();
            ExactlyOne nea = new ExactlyOne();
            a.nested.addPolicyComponent(nea);
            All na = new All();
            nea.addPolicyComponent(na);
            na.addPolicyComponents(alternative);
            all.addPolicyComponent(a);
            ea.addPolicyComponent(all);            
        } 
        return p;      
    } 
    
    public Policy getNested() {
        return nested;
    }

    @Override
    public boolean equal(PolicyComponent policyComponent) {
        if (!super.equal(policyComponent)) {
            return false;
        }
        NestedPrimitiveAssertion other = (NestedPrimitiveAssertion)policyComponent;
        return getNested().equal(other.getNested());
    }
    
    protected void setNested(Policy n) {
        nested = n;
    }
    
    
}
