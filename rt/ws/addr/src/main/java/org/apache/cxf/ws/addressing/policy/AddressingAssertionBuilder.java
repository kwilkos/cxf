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

package org.apache.cxf.ws.addressing.policy;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import org.apache.cxf.Bus;
import org.apache.cxf.ws.policy.AssertionBuilder;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.builders.primitive.NestedPrimitiveAssertion;
import org.apache.cxf.ws.policy.builders.primitive.PrimitiveAssertion;
import org.apache.neethi.Assertion;

/**
 * 
 */
public class AddressingAssertionBuilder implements AssertionBuilder {

    private static final Collection<QName> KNOWN = new ArrayList<QName>();
    private Bus bus;
    
    public AddressingAssertionBuilder(Bus b) {
        bus = b;
    }
    
    static {
        KNOWN.add(MetadataConstants.getAddressingAssertionQName());
        KNOWN.add(MetadataConstants.getAnonymousResponsesAssertionQName());
        KNOWN.add(MetadataConstants.getNonAnonymousResponsesAssertionQName());
    }
    
    public Assertion build(Element elem) {
        
        String localName = elem.getLocalName();
        QName n = new QName(elem.getNamespaceURI(), localName);
        System.out.println("Using AddressingAssertionBuilder to build assertion for " + n);
        
        boolean optional = false;
        Attr attribute = elem.getAttributeNodeNS(PolicyConstants.NAMESPACE_URI, 
                                              PolicyConstants.OPTIONAL_ATTR_NAME);
        if (attribute != null) {
            optional = Boolean.valueOf(attribute.getValue());
        }
        if (MetadataConstants.getAddressingAssertionQName().getLocalPart().equals(localName)) {
            PolicyBuilder builder = bus.getExtension(PolicyBuilder.class);
            return new NestedPrimitiveAssertion(elem, builder);
        } else if (MetadataConstants.getAnonymousResponsesAssertionQName().getLocalPart()
            .equals(localName)) {
            return new PrimitiveAssertion(MetadataConstants.getAnonymousResponsesAssertionQName(), 
                                          optional);
        } else if (MetadataConstants.getNonAnonymousResponsesAssertionQName().getLocalPart()
            .equals(localName)) {
            return new PrimitiveAssertion(MetadataConstants.getNonAnonymousResponsesAssertionQName(), 
                                          optional);
        }
        return null;
    }

    public Collection<QName> getKnownElements() {
        return KNOWN;
    }

    
    
}
