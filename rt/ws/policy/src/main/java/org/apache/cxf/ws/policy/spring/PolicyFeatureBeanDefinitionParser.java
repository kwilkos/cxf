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
package org.apache.cxf.ws.policy.spring;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.WSPolicyFeature;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;

public class PolicyFeatureBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder bean) {
        List<Element> els = new ArrayList<Element>();
        els.add(element);
        bean.addPropertyValue("policyElements", els);
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition bean, 
                               ParserContext ctx) throws BeanDefinitionStoreException {
        return element.getAttributeNS(PolicyConstants.getWSUNamespace(), PolicyConstants.getIdAttrName());
    }

    @Override
    protected Class getBeanClass(Element el) {
        return WSPolicyFeature.class;
    }

}
