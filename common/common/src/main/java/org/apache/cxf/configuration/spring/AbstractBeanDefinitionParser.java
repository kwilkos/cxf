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

import org.apache.cxf.helpers.DOMUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;

public abstract class AbstractBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected String resolveId(Element el, AbstractBeanDefinition arg1, 
                               ParserContext arg2) throws BeanDefinitionStoreException {
        String id = el.getAttribute("id");
        String createdFromAPI = el.getAttribute("createdFromAPI");
        
        if (createdFromAPI != null && "true".equals(createdFromAPI.toLowerCase())) {
            return id + getSuffix();
        }
        
        return super.resolveId(el, arg1, arg2);
    }

    protected String getSuffix() {
        return "";
    }

    protected void setFirstChildAsProperty(Element element, ParserContext ctx, 
                                         BeanDefinitionBuilder bean, String propertyName) {
        String id = getAndRegisterFirstChild(element, ctx, bean, propertyName);
        bean.addPropertyReference(propertyName, id);
        
    }

    protected String getAndRegisterFirstChild(Element element, ParserContext ctx, 
                                              BeanDefinitionBuilder bean, String propertyName) {
        Element first = getFirstChild(element);
        
        if (first == null) {
            throw new IllegalStateException(propertyName + " property must have child elements!");
        }
        
        // Seems odd that we have to do the registration, I wonder if there is a better way
        String id;
        BeanDefinition child;
        if (first.getNamespaceURI().equals(BeanDefinitionParserDelegate.BEANS_NAMESPACE_URI)) {
            String name = first.getLocalName();
            if ("ref".equals(name)) {
                id = first.getAttribute("bean");
                if (id == null) {
                    throw new IllegalStateException("<ref> elements must have a \"bean\" attribute!");
                }
                return id;
            } else if ("bean".equals(name)) {
                BeanDefinitionHolder bdh = ctx.getDelegate().parseBeanDefinitionElement(first);
                child = bdh.getBeanDefinition();
                id = bdh.getBeanName();
            } else {
                throw new UnsupportedOperationException("Elements with the name " + name  
                                                        + " are not currently "
                                                        + "supported as sub elements of " 
                                                        + element.getLocalName());
            }
            
        } else {
            child = ctx.getDelegate().parseCustomElement(first, bean.getBeanDefinition());
            id = child.toString();
        }
       
        ctx.getRegistry().registerBeanDefinition(id, child);
        return id;
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
    
    protected boolean isAttribute(String pre, String name) {
        return !"xmlns".equals(name) && (pre == null || !pre.equals("xmlns"))
            && !"abstract".equals(name) && !"lazy-init".equals(name) && !"id".equals(name);
    }

    protected QName parseQName(Element element, String t) {
        String ns = null;
        String pre = null;
        String local = null;

        if (t.startsWith("{")) {
            int i = t.indexOf('}');
            if (i == -1) {
                throw new RuntimeException("Namespace bracket '{' must having a closing bracket '}'.");
            }

            ns = t.substring(1, i);
            t = t.substring(i + 1);
        }

        int colIdx = t.indexOf(':');
        if (colIdx == -1) {
            local = t;
            pre = "";
            
            ns = DOMUtils.getNamespace(element, "");
        } else {
            pre = t.substring(0, colIdx);
            local = t.substring(colIdx + 1);
            
            ns = DOMUtils.getNamespace(element, pre);
        }

        return new QName(ns, local, pre);
    }

}
