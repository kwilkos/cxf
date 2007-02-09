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

import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.extension.BusExtension;
import org.apache.cxf.extension.RegistryImpl;
import org.apache.neethi.Assertion;

/**
 * 
 */
public class AssertionBuilderRegistryImpl extends RegistryImpl<QName, AssertionBuilder> implements
    AssertionBuilderRegistry, BusExtension {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(AssertionBuilderRegistry.class);

    public AssertionBuilderRegistryImpl() {
        this(null);
    }

    public AssertionBuilderRegistryImpl(Map<QName, AssertionBuilder> builders) {
        super(builders);
    }

    public Class getRegistrationType() {
        return AssertionBuilderRegistry.class;
    }
    

    public Assertion build(Element element) {

        AssertionBuilder builder;

        QName qname = new QName(element.getNamespaceURI(), element.getLocalName());
        builder = get(qname);

        if (null == builder) {
            throw new PolicyException(new Message("NO_ASSERTIONBUILDER_EXC", BUNDLE, qname.toString()));
        }

        return builder.build(element);

    }
}
