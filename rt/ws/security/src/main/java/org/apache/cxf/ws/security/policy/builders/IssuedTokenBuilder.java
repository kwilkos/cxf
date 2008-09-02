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
package org.apache.cxf.ws.security.policy.builders;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.ws.policy.AssertionBuilder;
import org.apache.cxf.ws.policy.PolicyAssertion;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.security.policy.SP11Constants;
import org.apache.cxf.ws.security.policy.SP12Constants;
import org.apache.cxf.ws.security.policy.SPConstants;
import org.apache.cxf.ws.security.policy.model.IssuedToken;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;


public class IssuedTokenBuilder implements AssertionBuilder {
    private static final String WSA_NAMESPACE_SUB = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    private static final String WSA_NAMESPACE = "http://www.w3.org/2005/08/addressing";
    
    private static final List<QName> KNOWN_ELEMENTS 
        = Arrays.asList(SP11Constants.ISSUED_TOKEN, SP12Constants.ISSUED_TOKEN);
    
    PolicyBuilder builder;
    public IssuedTokenBuilder(PolicyBuilder b) {
        builder = b;
    }
    public List<QName> getKnownElements() {
        return KNOWN_ELEMENTS;
    }
    

    public PolicyAssertion build(Element element)
        throws IllegalArgumentException {
        
        SPConstants consts = SP11Constants.SP_NS.equals(element.getNamespaceURI())
            ? SP11Constants.INSTANCE : SP12Constants.INSTANCE;
    

        IssuedToken issuedToken = new IssuedToken(consts);

        String includeAttr = DOMUtils.getAttribute(element, consts.getIncludeToken());
        if (includeAttr != null) {
            issuedToken.setInclusion(consts.getInclusionFromAttributeValue(includeAttr));
        }
        // Extract Issuer
        Element issuerElem = DOMUtils.getFirstChildWithName(element, SP11Constants.ISSUER);
        if (issuerElem != null) {
            Element issuerEpr = DOMUtils
                .getFirstChildWithName(issuerElem, 
                                       new QName(WSA_NAMESPACE, "Address"));

            // try the other addressing namespace
            if (issuerEpr == null) {
                issuerEpr = DOMUtils
                    .getFirstChildWithName(issuerElem,
                                           new QName(WSA_NAMESPACE_SUB,
                                                     "Address"));
            }

            issuedToken.setIssuerEpr(issuerEpr);
        }

        // TODO check why this returns an Address element
        // iter = issuerElem.getChildrenWithLocalName("Metadata");

        if (issuerElem != null) {
            Element issuerMex = DOMUtils
                .getFirstChildWithName(issuerElem,
                                       new QName(WSA_NAMESPACE, "Metadata"));

            // try the other addressing namespace
            if (issuerMex == null) {
                issuerMex = DOMUtils
                    .getFirstChildWithName(issuerElem,
                                           new QName(WSA_NAMESPACE_SUB, 
                                                     "Metadata"));
            }

            issuedToken.setIssuerMex(issuerMex);
        }

        // Extract RSTTemplate
        Element rstTmplElem = DOMUtils.getFirstChildWithName(element, 
                                                             SP11Constants.REQUEST_SECURITY_TOKEN_TEMPLATE);
        if (rstTmplElem != null) {
            issuedToken.setRstTemplate(rstTmplElem);
        }

        Element policyElement = DOMUtils.getFirstChildWithName(element,
                                                               org.apache.neethi.Constants.Q_ELEM_POLICY);

        if (policyElement != null) {

            Policy policy = builder.getPolicy(policyElement);
            policy = (Policy)policy.normalize(false);

            for (Iterator iterator = policy.getAlternatives(); iterator.hasNext();) {
                processAlternative((List)iterator.next(), issuedToken);
                break; // since there should be only one alternative ..
            }
        }

        return issuedToken;
    }


    private void processAlternative(List assertions, IssuedToken parent) {
        Assertion assertion;
        QName name;

        for (Iterator iterator = assertions.iterator(); iterator.hasNext();) {
            assertion = (Assertion)iterator.next();
            name = assertion.getName();

            if (SP11Constants.REQUIRE_DERIVED_KEYS.equals(name)) {
                parent.setDerivedKeys(true);
            } else if (SP11Constants.REQUIRE_EXTERNAL_REFERNCE.equals(name)) {
                parent.setRequireExternalReference(true);
            } else if (SP11Constants.REQUIRE_INTERNAL_REFERNCE.equals(name)) {
                parent.setRequireInternalReference(true);
            }
        }

    }

    public PolicyAssertion buildCompatible(PolicyAssertion a, PolicyAssertion b) {
        // TODO Auto-generated method stub
        return null;
    }
}