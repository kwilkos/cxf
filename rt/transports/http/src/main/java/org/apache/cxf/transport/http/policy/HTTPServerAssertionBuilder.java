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

package org.apache.cxf.transport.http.policy;

import javax.xml.bind.JAXBException;

import org.apache.cxf.transports.http.configuration.HTTPServerPolicy;
import org.apache.cxf.ws.policy.builder.jaxb.JaxbAssertion;
import org.apache.cxf.ws.policy.builder.jaxb.JaxbAssertionBuilder;
import org.apache.neethi.Assertion;

/**
 * 
 */
public class HTTPServerAssertionBuilder extends JaxbAssertionBuilder<HTTPServerPolicy> {
 
    public HTTPServerAssertionBuilder() throws JAXBException {
        super(HTTPServerPolicy.class, PolicyUtils.HTTPSERVERPOLICY_ASSERTION_QNAME);        
    }

    @Override
    public Assertion buildCompatible(Assertion a, Assertion b) {
        if (PolicyUtils.HTTPSERVERPOLICY_ASSERTION_QNAME.equals(a.getName())
            && PolicyUtils.HTTPSERVERPOLICY_ASSERTION_QNAME.equals(b.getName())) {
            
            HTTPServerPolicy compatible = PolicyUtils.intersect(
                JaxbAssertion.cast(a, HTTPServerPolicy.class).getData(),
                JaxbAssertion.cast(b, HTTPServerPolicy.class).getData());
            if (null == compatible) {
                return null;
            }
            JaxbAssertion<HTTPServerPolicy> ca = 
                new JaxbAssertion<HTTPServerPolicy>(PolicyUtils.HTTPSERVERPOLICY_ASSERTION_QNAME, 
                    a.isOptional() && b.isOptional());
            ca.setData(compatible);
            return ca;
        }
        return null;
    }
}
