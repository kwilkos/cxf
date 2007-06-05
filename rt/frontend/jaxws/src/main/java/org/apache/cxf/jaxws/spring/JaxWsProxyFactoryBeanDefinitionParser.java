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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.configuration.spring.AbstractBeanDefinitionParser;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;

public class JaxWsProxyFactoryBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected String getSuffix() {
        return ".jaxws-client";
    }

    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder clientBean) {
        
        BeanDefinitionBuilder bean = BeanDefinitionBuilder.rootBeanDefinition(JaxWsProxyFactoryBean.class);

        NamedNodeMap atts = element.getAttributes();        
        boolean createdFromAPI = false;
        boolean setBus = false;
        for (int i = 0; i < atts.getLength(); i++) {
            Attr node = (Attr) atts.item(i);
            String val = node.getValue();
            String pre = node.getPrefix();
            String name = node.getLocalName();
            
            if ("createdFromAPI".equals(name)) {
                bean.setAbstract(true);
                clientBean.setAbstract(true);
                createdFromAPI = true;
            } else if (!"id".equals(name) && isAttribute(pre, name)) {
                if ("endpointName".equals(name) || "serviceName".equals(name)) {
                    QName q = parseQName(element, val);
                    bean.addPropertyValue(name, q);
                } else if (!"name".equals(name)) {
                    if ("bus".equals(name)) {
                        setBus = true;
                    }
                    mapToProperty(bean, name, val);
                }
            } else if ("abstract".equals(name)) {
                bean.setAbstract(true);
                clientBean.setAbstract(true);
            }
        }
        
        if (!setBus && ctx.getRegistry().containsBeanDefinition("cxf")) {
            bean.addPropertyReference("bus", "cxf");
        }
        
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String name = n.getLocalName();
                if ("properties".equals(n.getLocalName())) {
                    Map map = ctx.getDelegate().parseMapElement((Element) n, bean.getBeanDefinition());
                    bean.addPropertyValue("properties", map);
                } else if ("inInterceptors".equals(name) || "inFaultInterceptors".equals(name)
                    || "outInterceptors".equals(name) || "outFaultInterceptors".equals(name)
                    || "features".equals(name)) {
                    List list = ctx.getDelegate().parseListElement((Element) n, bean.getBeanDefinition());
                    bean.addPropertyValue(n.getLocalName(), list);
                } else {
                    setFirstChildAsProperty((Element) n, ctx, bean, n.getLocalName());
                }
            }
        }
        String id = getIdOrName(element);
        if (createdFromAPI) {
            id = id + getSuffix();
        }
        String factoryId = id + ".proxyFactory";
        
        ctx.getRegistry().registerBeanDefinition(factoryId, bean.getBeanDefinition());
        clientBean.getBeanDefinition().setAttribute("id", id);
        clientBean.setFactoryBean(factoryId, "create");
    }

    @Override
    protected Class getBeanClass(Element arg0) {
        return Object.class;
    }
}
