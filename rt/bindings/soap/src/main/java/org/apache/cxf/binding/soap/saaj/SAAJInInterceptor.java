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

package org.apache.cxf.binding.soap.saaj;


import java.util.ResourceBundle;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;

/**
 * Builds a SAAJ tree from the Document fragment inside the message which contains
 * the SOAP headers and from the XMLStreamReader.
 */
public class SAAJInInterceptor extends AbstractSoapInterceptor {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(SAAJInInterceptor.class);

    public SAAJInInterceptor() {
        setPhase(Phase.PRE_PROTOCOL);
    }
    
    public void handleMessage(SoapMessage message) throws Fault {
        try {
            MessageFactory factory = null;
            if (message.getVersion() instanceof Soap11) {
                factory = MessageFactory.newInstance();
            } else {
                factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            }
            
            SOAPMessage soapMessage = factory.createMessage();
            message.setContent(SOAPMessage.class, soapMessage);
            
            SOAPPart part = soapMessage.getSOAPPart();
            
            Document node = (Document) message.getContent(Node.class);
            DOMSource source = new DOMSource(node);
            part.setContent(source);
            
            // TODO: add attachments and mime headers
            
            //replace header element if necessary
            if (message.hasHeaders(Element.class)) {
                Element headerElements = soapMessage.getSOAPHeader();    
                message.setHeaders(Element.class, headerElements);
            }
            
            XMLStreamReader xmlReader = message.getContent(XMLStreamReader.class);
            StaxUtils.readDocElements(soapMessage.getSOAPBody(), xmlReader, true);
            
            DOMSource bodySource = new DOMSource(soapMessage.getSOAPBody());
            xmlReader = StaxUtils.createXMLStreamReader(bodySource);
            xmlReader.nextTag();
            xmlReader.nextTag(); // move past body tag
            message.setContent(XMLStreamReader.class, xmlReader);
        } catch (SOAPException soape) {
            throw new SoapFault(new org.apache.cxf.common.i18n.Message(
                    "SOAPHANDLERINTERCEPTOR_EXCEPTION", BUNDLE), soape,
                    message.getVersion().getSender());
        } catch (XMLStreamException e) {
            throw new SoapFault(new org.apache.cxf.common.i18n.Message(
                    "SOAPHANDLERINTERCEPTOR_EXCEPTION", BUNDLE), e, message
                    .getVersion().getSender());
        }
    }

}
