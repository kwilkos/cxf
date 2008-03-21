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

package org.apache.cxf.bus.spring;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Document;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.EntityResolver2;

import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.springframework.beans.factory.xml.DefaultDocumentLoader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * 
 */
public class TunedDocumentLoader extends DefaultDocumentLoader {

    @Override
    public Document loadDocument(InputSource inputSource, EntityResolver entityResolver,
                                 ErrorHandler errorHandler, int validationMode, boolean namespaceAware)
        throws Exception {
        if (validationMode == XmlBeanDefinitionReader.VALIDATION_NONE) {

            final EntityResolver finalResolver = entityResolver;
            final ErrorHandler finalHandler = errorHandler;

            W3CDOMStreamWriter domStreamWriter = new W3CDOMStreamWriter();

            final XMLInputFactory inputFactory = StaxUtils.createXMLInputFactory(namespaceAware);
            inputFactory.setXMLReporter(new XMLReporter() {

                public void report(String message, String errorType, Object relatedInformation,
                                   Location location) throws XMLStreamException {
                    final Location finalLocation = location;
                    org.xml.sax.Locator saxLocator = new org.xml.sax.Locator() {

                        public int getColumnNumber() {
                            return finalLocation.getColumnNumber();
                        }

                        public int getLineNumber() {
                            return finalLocation.getLineNumber();
                        }

                        public String getPublicId() {
                            return finalLocation.getPublicId();
                        }

                        public String getSystemId() {
                            return finalLocation.getSystemId();
                        }
                    };

                    try {
                        finalHandler.error(new SAXParseException(message, saxLocator));
                    } catch (SAXException e) {
                        // this is not what we want, but
                        // I don't see how to get it right.
                        throw new XMLStreamException(e);
                    }
                }
            });

            inputFactory.setXMLResolver(new XMLResolver() {

                public Object resolveEntity(String publicID, 
                                            String systemID, String baseURI, String namespace)
                    throws XMLStreamException {
                    try {
                        if (finalResolver instanceof EntityResolver2) {
                            EntityResolver2 er2 = (EntityResolver2)finalResolver;

                            InputSource entitySource = er2
                                .resolveEntity("[dtd]", publicID, baseURI, systemID);
                            return inputFactory.createXMLStreamReader(new SAXSource(entitySource));
                        } else {
                            InputSource entitySource = finalResolver.resolveEntity(publicID, systemID);
                            return inputFactory.createXMLStreamReader(new SAXSource(entitySource));
                        }
                    } catch (Exception e) {
                        throw new XMLStreamException(e);
                    }
                }
            });

            SAXSource saxSource = new SAXSource(inputSource);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(saxSource);
            StaxUtils.copy(reader, domStreamWriter);
            return domStreamWriter.getDocument();
        } else {
            return super.loadDocument(inputSource, entityResolver, errorHandler, validationMode,
                                      namespaceAware);
        }
    }

    @Override
    protected DocumentBuilderFactory createDocumentBuilderFactory(int validationMode, boolean namespaceAware)
        throws ParserConfigurationException {
        DocumentBuilderFactory factory = super.createDocumentBuilderFactory(validationMode, namespaceAware);
        try {
            factory.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
        } catch (ParserConfigurationException pce) {
            // blank
        }
        return factory;
    }

}
