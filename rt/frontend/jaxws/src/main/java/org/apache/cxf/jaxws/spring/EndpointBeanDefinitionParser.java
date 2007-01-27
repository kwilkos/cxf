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
package org.apache.cxf.jaxws.spring;

import org.w3c.dom.Element;

import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;

public class EndpointBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        mapAttributeToProperty(element, bean, "class", "serviceClass");
    }

    private void mapAttributeToProperty(Element element, 
                                        BeanDefinitionBuilder bean,
                                        String attrName,
                                        String propertyName) {
        String cls = element.getAttribute(attrName);
        if (StringUtils.hasText(cls)) {
            bean.addPropertyValue(propertyName, cls);
        }
    }

    @Override
    protected Class getBeanClass(Element arg0) {
        return JaxWsServerFactoryBean.class;
    }

}
