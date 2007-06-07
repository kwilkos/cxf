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
package org.apache.cxf.transport.http_jetty.spring;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Element;

import org.apache.cxf.configuration.spring.AbstractBeanDefinitionParser;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;
import org.apache.cxf.transports.http_jetty.configuration.JettyHTTPServerEngineFactoryConfigType;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;

public class JettyHTTPServerEngineFactoryBeanDefinitionParser
        extends AbstractBeanDefinitionParser {

    @Override
    public void doParse(Element engineFactory, ParserContext ctx, BeanDefinitionBuilder bean) {
        // Unmarshal the JAXB Generated Type from Config 
        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(
                    JettyHTTPServerEngineFactoryConfigType.class.getPackage().getName(), 
                    getClass().getClassLoader());
            Unmarshaller u = context.createUnmarshaller();
            
            JAXBElement<JettyHTTPServerEngineFactoryConfigType> jaxb = 
                u.unmarshal(engineFactory, 
                        JettyHTTPServerEngineFactoryConfigType.class);
            
            JettyHTTPServerEngineFactoryConfigType params = jaxb.getValue();
            
            JettyHTTPServerEngineFactoryConfig config =
                new JettyHTTPServerEngineFactoryConfig(params);
            
            bean.addPropertyValue("config", config);
            bean.addPropertyReference("bus", params.getBus());
            
        } catch (Exception e) {
            throw new RuntimeException("Could not process configuration.", e);
        }
    }
    
    /*
     * We do not require an id from the configuration.
     * 
     * (non-Javadoc)
     * @see org.springframework.beans.factory.xml.AbstractBeanDefinitionParser#shouldGenerateId()
     */
    @Override
    protected boolean shouldGenerateId() {
        return true;
    }

    @Override
    protected Class getBeanClass(Element arg0) {
        return JettyHTTPServerEngineFactory.class;
    }

}
