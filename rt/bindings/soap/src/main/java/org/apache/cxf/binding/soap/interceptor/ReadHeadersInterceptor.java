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
//import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.PartialXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class ReadHeadersInterceptor extends AbstractSoapInterceptor {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ReadHeadersInterceptor.class);

    public ReadHeadersInterceptor() {
        super();
        setPhase(Phase.READ);
    }

    public void handleMessage(SoapMessage message) {
        XMLStreamReader xmlReader = message.getContent(XMLStreamReader.class);
     
        
        
        if (xmlReader == null) {
            InputStream in = (InputStream)message.getContent(InputStream.class);
            if (in == null) {
                throw new RuntimeException("Can't found input stream in message");
            }
            xmlReader = StaxUtils.createXMLStreamReader(in);
        }

        try {
            /*System.out.println("the xml fragment is ");
            System.out.println(XMLUtils.toString(StaxUtils.read(xmlReader)));*/
            if (xmlReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                String ns = xmlReader.getNamespaceURI();
                SoapVersion soapVersion = SoapVersionFactory.getInstance().getSoapVersion(ns);
                message.setVersion(soapVersion);
                
                XMLStreamReader filteredReader = 
                    new PartialXMLStreamReader(xmlReader, message.getVersion().getBody());
                
                Document doc = StaxUtils.read(filteredReader);
                
                Element envelop = (Element)doc.getChildNodes().item(0);
                String header = soapVersion.getHeader().getLocalPart();
                for (int i = 0; i < envelop.getChildNodes().getLength(); i++) {
                    if (envelop.getChildNodes().item(i) instanceof Element) {
                        Element element = (Element)envelop.getChildNodes().item(i);
                        if (element.getLocalName().equals(header)) {
                            message.setHeaders(Element.class, element);
                            message.put(Element.class, element);
                        }
                    }
                }
                
                // advance just past body.
                xmlReader.next();
            }
        } catch (XMLStreamException e) {
            throw new SoapFault(new Message("XML_STREAM_EXC", BUNDLE), e, SoapFault.SENDER);
        }
    }

}
