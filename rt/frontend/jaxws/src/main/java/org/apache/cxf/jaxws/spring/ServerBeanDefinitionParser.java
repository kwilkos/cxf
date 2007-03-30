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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.configuration.spring.AbstractBeanDefinitionParser;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;

public class ServerBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String IMPLEMENTOR = "implementor";

    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder bean) {
        NamedNodeMap atts = element.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Attr node = (Attr) atts.item(i);
            String val = node.getValue();
            
            if (IMPLEMENTOR.equals(node.getLocalName())) {
                loadImplementor(bean, val);
            } else {
                mapToProperty(bean, node.getLocalName(), val);
            }
        }
        
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String name = n.getLocalName();
                if ("properties".equals(n.getLocalName())) {
                    Map map = ctx.getDelegate().parseMapElement((Element) n, bean.getBeanDefinition());
                    bean.addPropertyValue("properties", map);
                } else if ("executor".equals(n.getLocalName())) {
                    setFirstChildAsProperty((Element) n, ctx, bean, "serviceFactory.executor");
                } else if ("invoker".equals(n.getLocalName())) {
                    setFirstChildAsProperty((Element) n, ctx, bean, "serviceFactory.invoker");
                } else if ("binding".equals(n.getLocalName())) {
                    setFirstChildAsProperty((Element) n, ctx, bean, "bindingConfig");
                }  else if ("inInterceptors".equals(name) || "inFaultInterceptors".equals(name)
                    || "outInterceptors".equals(name) || "outFaultInterceptors".equals(name)) {
                    List list = ctx.getDelegate().parseListElement((Element) n, bean.getBeanDefinition());
                    bean.addPropertyValue(n.getLocalName(), list);
                } else {
                    setFirstChildAsProperty((Element) n, ctx, bean, n.getLocalName());
                }
            }
        }
        
        bean.setInitMethodName("create");
        
        // We don't really want to delay the registration of our Server
        bean.setLazyInit(false);
    }

    private void loadImplementor(BeanDefinitionBuilder bean, String val) {
        if (StringUtils.hasText(val)) {
            if (val.startsWith("#")) {
                bean.addPropertyReference(IMPLEMENTOR, val.substring(1));
            } else {
                try {
                    bean.addPropertyValue(IMPLEMENTOR,
                                          ClassLoaderUtils.loadClass(val, getClass()).newInstance());
                } catch (Exception e) {
                    throw new FatalBeanException("Could not load class: " + val, e);
                }
            }
        }
    }

    @Override
    protected Class getBeanClass(Element arg0) {
        return JaxWsServerFactoryBean.class;
    }

}
