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

package org.apache.cxf.ws.policy.builders.jaxb;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.All;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;


/**
 * 
 */
public class JaxbAssertion<T> implements Assertion {
    
    private T data;
    private QName name;
    private boolean optional;
    
    void setData(T d) {
        data = d;
    }

    void setName(QName n) {
        name = n;
    }
    
    void setOptional(boolean o) {
        optional = o;
    }
    
    public T getData() {
        return data;
    }
    
    public QName getName() {
        return name;
    }

    public boolean isOptional() {
        return optional;
    }

    /**
     * Returns the partial normalized version of the wrapped element, that
     * is assumed to be an assertion.
     */
    public PolicyComponent normalize() {
        if (optional) {
            Policy policy = new Policy();
            ExactlyOne exactlyOne = new ExactlyOne();
            All all = new All();
            JaxbAssertion<T> a = new JaxbAssertion<T>();
            a.setData(getData());
            a.setName(getName());
            all.addPolicyComponent(this);
            exactlyOne.addPolicyComponent(all);
            exactlyOne.addPolicyComponent(new All());
            policy.addPolicyComponent(exactlyOne);
            return policy;
        }

        return this;
    }

    public boolean equal(PolicyComponent policyComponent) {
        if (policyComponent.getType() != Constants.TYPE_ASSERTION) {
            return false;
        }

        if (!getName().equals(((Assertion)policyComponent).getName())) {
            return false;
        }
        
        return getData().equals(((JaxbAssertion)policyComponent).getData());
    }

    public short getType() {
        return Constants.TYPE_ASSERTION;
    }
    
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        // TODO
    }
    

    
}
