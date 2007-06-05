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
package org.apache.cxf.transport.http.spring;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.jsse.spring.TLSServerParametersConfig;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.configuration.security.TLSServerParametersType;
import org.apache.cxf.configuration.spring.AbstractBeanDefinitionParser;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transports.http.configuration.HTTPServerPolicy;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public class HttpDestinationBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String HTTP_NS = "http://cxf.apache.org/transports/http/configuration";

    @Override
    public void doParse(Element element, BeanDefinitionBuilder bean) {
        bean.setAbstract(true);
        mapElementToJaxbProperty(element, bean, new QName(HTTP_NS, "server"), "server", 
                                 HTTPServerPolicy.class);
        
        // DEPRECATED: This element is deprecated in favor of tlsServerParameters.
        mapElementToJaxbProperty(element, bean, new QName(HTTP_NS, "sslServer"), "sslServer", 
                                 SSLServerPolicy.class);
        
        mapElementToJaxbProperty(element, bean, new QName(HTTP_NS, "authorization"), "authorization", 
                                 AuthorizationPolicy.class);
        
        mapSpecificElements(element, bean);
    }

    /**
     * This method specifically maps the "tlsServerParameters" on the 
     * HTTPDestination.
     * 
     * @param parent This should represent "destination".
     * @param bean   The bean parser.
     */
    private void mapSpecificElements(
        Element               parent, 
        BeanDefinitionBuilder bean
    ) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (Node.ELEMENT_NODE != n.getNodeType() 
                || !HTTP_NS.equals(n.getNamespaceURI())) {
                continue;
            }
            Element elem = (Element)n;
            String elementName = n.getLocalName(); 
            if ("tlsServerParameters".equals(elementName)) {
                this.mapTLSServerParameters(n, bean);
            } else if ("contextMatchStrategy".equals(elementName)) {
                bean.addPropertyValue(elementName, DatatypeConverter.parseString(elem.getTextContent()));
            } else if ("fixedParameterOrder".equals(elementName)) {
                bean.addPropertyValue(elementName, DatatypeConverter.parseBoolean(elem.getTextContent()));
            }
        }
    }

    /**
     * Inject the "setTlsServerParameters" method with
     * a TLSServerParametersConfig object initialized with the JAXB
     * generated type unmarshalled from the selected node.
     */
    public void mapTLSServerParameters(Node n, BeanDefinitionBuilder bean) {

        // Unmarshal the JAXB Generated Type from Config and inject
        // the configured TLSClientParameters into the HTTPDestination.
        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(TLSServerParametersType.class.getPackage().getName(), 
                                  getClass().getClassLoader());
            Unmarshaller u = context.createUnmarshaller();
            JAXBElement<TLSServerParametersType> jaxb = 
                u.unmarshal(n, TLSServerParametersType.class);
            TLSServerParameters params = 
                new TLSServerParametersConfig(jaxb.getValue());
            bean.addPropertyValue("tlsServerParameters", params);
        } catch (Exception e) {
            throw new RuntimeException("Could not process configuration.", e);
        }
    }

    @Override
    protected Class getBeanClass(Element arg0) {
        return AbstractHTTPDestination.class;
    }

}
