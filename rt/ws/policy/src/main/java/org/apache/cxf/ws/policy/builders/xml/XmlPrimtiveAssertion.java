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

package org.apache.cxf.ws.policy.builders.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import org.apache.neethi.All;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;


/**
 * XmlPrimitiveAssertion is a primitive implementation of an AssertionBuilder
 * that simply wraps the underlying xml element.
 * 
 */
public class XmlPrimtiveAssertion implements Assertion {

    Element element;

    boolean isOptional;

    /**
     * Constructs a XmlPrimitiveAssertion from an xml element.
     * 
     * @param element the xml element
     */
    public XmlPrimtiveAssertion(Element e) {
        setValue(e);
        setOptionality(e);
    }

    /**
     * Returns the QName of the wrapped Element.
     */
    public QName getName() {
        return (element != null) ? new QName(element.getNamespaceURI(), element.getLocalName()) : null;
    }

    /**
     * Sets the wrapped xml element.
     * 
     * @param element the element to be wrapped
     */
    public final void setValue(Element e) {
        element = e;
    }

    /**
     * Returns the wrapped element.
     * 
     * @return the wrapped element
     */
    public Element getValue() {
        return element;
    }

    /**
     * Returns <tt>true</tt> if the wrapped element that is assumed to be an
     * assertion, is optional.
     */
    public boolean isOptional() {
        return isOptional;
    }

    /**
     * Returns the partial normalized version of the wrapped element, that is
     * assumed to be an assertion.
     */
    public PolicyComponent normalize() {
        if (isOptional) {
            Policy policy = new Policy();
            ExactlyOne exactlyOne = new ExactlyOne();

            All all = new All();
            Element e = (Element)element.cloneNode(true);

            e.removeAttributeNode(e.getAttributeNodeNS(Constants.Q_ELEM_OPTIONAL_ATTR.getNamespaceURI(),
                                                       Constants.Q_ELEM_OPTIONAL_ATTR.getLocalPart()));
            all.addPolicyComponent(new XmlPrimtiveAssertion(e));
            exactlyOne.addPolicyComponent(all);

            exactlyOne.addPolicyComponent(new All());
            policy.addPolicyComponent(exactlyOne);

            return policy;
        }

        return this;
    }

    /**
     * Throws an UnsupportedOperationException since an assertion of an unknown
     * element can't be fully normalized due to it's unknown composite.
     */
    public PolicyComponent normalize(boolean isDeep) {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        // TODO
        /*
        if (element != null) {
            element.serialize(writer);

        } else {
            throw new RuntimeException("Wrapped Element is not set");
        }
        */
    }

    /**
     * Returns Constants.TYPE_ASSERTION
     */
    public short getType() {
        return Constants.TYPE_ASSERTION;
    }

    private void setOptionality(Element e) {
        Attr attribute = e.getAttributeNodeNS(Constants.Q_ELEM_OPTIONAL_ATTR.getNamespaceURI(), 
                                                    Constants.Q_ELEM_OPTIONAL_ATTR.getLocalPart());
        if (attribute != null) {
            this.isOptional = Boolean.valueOf(attribute.getValue());
            
        } else {
            this.isOptional = false;
        }
    }

    public boolean equal(PolicyComponent policyComponent) {
        if (policyComponent.getType() != Constants.TYPE_ASSERTION) {
            return false;
        }

        return getName().equals(((Assertion)policyComponent).getName());
    }
}
