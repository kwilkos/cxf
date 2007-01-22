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

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.apache.cxf.Bus;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.ws.policy.builders.AssertionBuilder;
import org.apache.neethi.Assertion;


/**
 * AssertionBuilderRegistry is used to manage AssertionBuilders and
 * create Assertion objects from given xml elements.
 */
public class AssertionBuilderRegistry {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(AssertionBuilderRegistry.class);

    private Bus bus;
    private Map<QName, AssertionBuilder> builders 
        = new HashMap<QName, AssertionBuilder>();
    
    public AssertionBuilderRegistry() {
    }

    public Bus getBus() {
        return bus;
    }

    @Resource
    public void setBus(Bus b) {
        bus = b;
    }
    
    @PostConstruct
    public void register() {
        if (null != bus) {
            bus.setExtension(this, AssertionBuilderRegistry.class);
        }
    }

    /**
     * Registers an AssertionBuilder with a specified QName. 
     *  
     * @param key the QName that the AssertionBuilder understands.
     * @param builder the AssertionBuilder that can build an Assertion from
     * an element of the type specified by the QName.
     */
    public void registerBuilder(QName key, AssertionBuilder builder) {
        builders.put(key, builder);
    }

    /**
     * Returns an assertion that is built using the specified xml element.
     * 
     * @param element the element from which to build an Assertion.
     * @return an Assertion that is built using the specified element.
     */
    public Assertion build(Element element) {

        AssertionBuilder builder;

        QName qname = new QName(element.getNamespaceURI(), element.getLocalName());
        builder = (AssertionBuilder) builders.get(qname);
        
        if (null == builder) {
            throw new PolicyException(new Message("NO_ASSERTIONBUILDER_EXC", BUNDLE, qname.toString()));
        }

        return builder.build(element);
        
    }
    
    /**
     * Returns an AssertionBuilder that can build an Assertion for an xml element with
     * the specified type.
     * 
     * @param qname the type for which an assertion builder is requested
     * @return an AssertionBuilder that understands the type
     */
    public AssertionBuilder getBuilder(QName qname) {
        return builders.get(qname);
    }
}
