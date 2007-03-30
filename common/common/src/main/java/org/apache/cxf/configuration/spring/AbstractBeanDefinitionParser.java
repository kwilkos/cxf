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
package org.apache.cxf.configuration.spring;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;

public abstract class AbstractBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected void setFirstChildAsProperty(Element element, ParserContext ctx, 
                                         BeanDefinitionBuilder bean, String string) {
        Element first = getFirstChild(element);
        
        if (first == null) {
            throw new IllegalStateException(string + " property must have child elements!");
        }
        
        // Seems odd that we have to do the registration, I wonder if there is a better way
        String id;
        BeanDefinition child;
        if (first.getNamespaceURI().equals(BeanDefinitionParserDelegate.BEANS_NAMESPACE_URI)) {
            BeanDefinitionHolder bdh = ctx.getDelegate().parseBeanDefinitionElement(first);
            child = bdh.getBeanDefinition();
            id = bdh.getBeanName();
            
        } else {
            child = ctx.getDelegate().parseCustomElement(first, bean.getBeanDefinition());
            id = child.toString();
        }
       
        ctx.getRegistry().registerBeanDefinition(id, child);
        bean.addPropertyReference(string, id);
        
    }

    protected Element getFirstChild(Element element) {
        Element first = null;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                first = (Element) n;
            }
        }
        return first;
    }
    
    protected void mapElementToJaxbProperty(Element parent, BeanDefinitionBuilder bean, QName name,
                                            String string) {
        mapElementToJaxbProperty(parent, bean, name, string, null);
    }

    protected void mapElementToJaxbProperty(Element parent, BeanDefinitionBuilder bean, QName name,
                                            String string, Class<?> c) {
        Node data = null;
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && name.getLocalPart().equals(n.getLocalName())
                && name.getNamespaceURI().equals(n.getNamespaceURI())) {
                data = n;
                break;
            }
        }

        if (data == null) {
            return;
        }

        JAXBContext context = null;
        Object obj = null;
        try {
            context = JAXBContext.newInstance(getJaxbPackage(), getClass().getClassLoader());
            Unmarshaller u = context.createUnmarshaller();
            if (c != null) {
                obj = u.unmarshal(data, c);
            } else {
                obj = u.unmarshal(data);
            }

            if (obj instanceof JAXBElement<?>) {
                JAXBElement<?> el = (JAXBElement<?>)obj;
                obj = el.getValue();

            }
        } catch (JAXBException e) {
            throw new RuntimeException("Could not parse configuration.", e);
        }

        if (obj != null) {
            bean.addPropertyValue(string, obj);
        }
    }

    protected String getJaxbPackage() {
        return "";
    }

    protected void mapAttributeToProperty(Element element, BeanDefinitionBuilder bean, String attrName,
                                          String propertyName) {
        String val = element.getAttribute(attrName);
        mapToProperty(bean, propertyName, val);
    }

    protected void mapToProperty(BeanDefinitionBuilder bean, String propertyName, String val) {
        if (ID_ATTRIBUTE.equals(propertyName)) {
            return;
        }
        
        if (StringUtils.hasText(val)) {
            if (val.startsWith("#")) {
                bean.addPropertyReference(propertyName, val.substring(1));
            } else {
                bean.addPropertyValue(propertyName, val);
            }
        }
    }

}
