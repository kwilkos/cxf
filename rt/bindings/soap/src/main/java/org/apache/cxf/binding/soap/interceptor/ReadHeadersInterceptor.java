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

package org.apache.cxf.binding.soap.interceptor;

import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.SoapVersionFactory;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.PartialXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class ReadHeadersInterceptor extends AbstractSoapInterceptor {
    private static final Logger LOG = Logger.getLogger(ReadHeadersInterceptor.class.getName());
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ReadHeadersInterceptor.class);

    public ReadHeadersInterceptor() {
        super();
        setPhase(Phase.READ);
    }

    public void handleMessage(SoapMessage message) {
        if (isGET(message)) {
            LOG.info("ReadHeadersInterceptor skipped in HTTP GET method");
            return;
        }
        XMLStreamReader xmlReader = message.getContent(XMLStreamReader.class);
        
        if (xmlReader == null) {
            InputStream in = (InputStream)message.getContent(InputStream.class);
            if (in == null) {
                throw new RuntimeException("Can't found input stream in message");
            }
            xmlReader = StaxUtils.createXMLStreamReader(in);
        }

        try {
            while (xmlReader.isWhiteSpace()) { 
                xmlReader.next(); 
            } 
            if (xmlReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                String ns = xmlReader.getNamespaceURI();
                SoapVersion soapVersion = SoapVersionFactory.getInstance().getSoapVersion(ns);
                message.setVersion(soapVersion);
                
                QName qn = xmlReader.getName();
                while (!qn.equals(message.getVersion().getBody())
                    && !qn.equals(message.getVersion().getHeader())) {
                    while (xmlReader.nextTag() != XMLStreamConstants.START_ELEMENT) {
                        //nothing to do
                    }
                    qn = xmlReader.getName();
                }
                if (qn.equals(message.getVersion().getHeader())) {
                    XMLStreamReader filteredReader = 
                        new PartialXMLStreamReader(xmlReader, message.getVersion().getBody());
                
                    Document doc = StaxUtils.read(filteredReader);

                    Element element = (Element)doc.getChildNodes().item(0);
                    message.setHeaders(Element.class, element);
                    message.put(Element.class, element);                    
                }
                // advance just past body.
                xmlReader.nextTag();
                if (xmlReader.getName().equals(message.getVersion().getBody())) {
                    xmlReader.nextTag();
                }                    
                if (message.getVersion().getFault().equals(xmlReader.getName())) {
                    Endpoint ep = message.getExchange().get(Endpoint.class);
                    if (ep != null) {
                        message.getInterceptorChain().abort();
                        if (ep.getInFaultObserver() != null) {
                            ep.getInFaultObserver().onMessage(message);
                        }
                    } else {
                        message.getExchange().put("deferred.fault.observer.notification", Boolean.TRUE);
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new SoapFault(new Message("XML_STREAM_EXC", BUNDLE), e, 
                                message.getVersion().getSender());
        }
    }

}
