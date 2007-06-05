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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.configuration.security.TLSServerParametersType;
import org.apache.cxf.configuration.spring.AbstractBeanDefinitionParser;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngine;
import org.apache.cxf.transports.http.configuration.HTTPListenerPolicy;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;

public class ListenerBeanDefinitionParser  extends AbstractBeanDefinitionParser {
     
    private static final String LISTENER_NS = "http://cxf.apache.org/transport/http/listener";
    
    @Override
    protected Class getBeanClass(Element arg0) {
        return JettyHTTPServerEngine.class;
    }
    
    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder bean) {
        bean.setAbstract(true);
        mapElementToJaxbProperty(element, bean, new QName(LISTENER_NS, "listener"), "listener",
            HTTPListenerPolicy.class);

        mapElementToJaxbProperty(element, bean, new QName(LISTENER_NS, "sslServer"), "sslServer",
            SSLServerPolicy.class);
        mapElementToJaxbProperty(element, bean, new QName(LISTENER_NS, "tlsServerParameters"), 
            "tlsServerParameters",
            TLSServerParametersType.class);

    }
}
