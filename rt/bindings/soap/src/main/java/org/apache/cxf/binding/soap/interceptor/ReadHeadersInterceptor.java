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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.binding.soap.Soap11;
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

import static org.apache.cxf.message.Message.DECOUPLED_CHANNEL_MESSAGE;

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
            if (xmlReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                String ns = xmlReader.getNamespaceURI();
                
                SoapVersion soapVersion = SoapVersionFactory.getInstance().getSoapVersion(ns);
                if (soapVersion == null) {
                    throw new SoapFault(new Message("INVALID_VERSION", LOG, ns), Soap11.getInstance()
                        .getSender());
                }
                message.setVersion(soapVersion);

                XMLStreamReader filteredReader = new PartialXMLStreamReader(xmlReader, message.getVersion()
                    .getBody());

                Document doc = StaxUtils.read(filteredReader);

                message.setContent(Node.class, doc);

                // Find header
                Element element = doc.getDocumentElement();
                QName header = soapVersion.getHeader();
                NodeList headerEls = element.getElementsByTagNameNS(header.getNamespaceURI(), header
                    .getLocalPart());
                for (int i = 0; i < headerEls.getLength(); i++) {
                    Node node = headerEls.item(i);
                    message.setHeaders(Element.class, (Element)node);
                }

                // advance just past body.
                xmlReader.nextTag();
                if (message.getVersion().getFault().equals(xmlReader.getName())) {
                    Endpoint ep = message.getExchange().get(Endpoint.class);
                    if (!isDecoupled(message)) {
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
            throw new SoapFault(new Message("XML_STREAM_EXC", BUNDLE), e, message.getVersion().getSender());
        }
    }

    private boolean isDecoupled(SoapMessage message) {
        Boolean decoupled = (Boolean)message.get(DECOUPLED_CHANNEL_MESSAGE);
        return decoupled != null && decoupled.booleanValue();
    }
}
