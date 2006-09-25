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

package org.apache.cxf.systest.ws.addressing;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.addressing.Names;
import org.apache.cxf.ws.addressing.soap.VersionTransformer;
import org.apache.cxf.ws.addressing.v200408.AttributedURI;

import static org.apache.cxf.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;


/**
 * Verifies presence of expected SOAP headers.
 */
public class HeaderVerifier extends AbstractSoapInterceptor {
    VerificationCache verificationCache;
    String currentNamespaceURI;
    
    public HeaderVerifier() {
        super();
        setPhase(Phase.POST_PROTOCOL);
    }
    
    public Set<QName> getUnderstoodHeaders() {
        return Names.HEADERS;
    }

    public void handleMessage(SoapMessage message) {
        addPartialResponseHeader(message);
        verify(message);
    }

    public void handleFault(SoapMessage message) {
        verify(message);
    }

    private void addPartialResponseHeader(SoapMessage message) {
        try {
            // add piggybacked wsa:From header to partial response
            if (isOutgoingPartialResponse(message)) {
                Element header = message.getHeaders(Element.class);
                marshallFrom("urn:piggyback_responder", header, getMarshaller());
            }
        } catch (Exception e) {
            verificationCache.put("SOAP header addition failed: " + e);
            e.printStackTrace();
        }
    }

    private void verify(SoapMessage message) {
        try {
            List<String> wsaHeaders = new ArrayList<String>();
            Element headers = message.getHeaders(Element.class);
            if (headers != null) {
                recordWSAHeaders(headers,
                                 wsaHeaders,
                                 Names.WSA_NAMESPACE_NAME);
                recordWSAHeaders(headers,
                                 wsaHeaders,
                                 VersionTransformer.Names200408.WSA_NAMESPACE_NAME);
            }
            boolean partialResponse = isIncomingPartialResponse(message)
                                      || isOutgoingPartialResponse(message);
            verificationCache.put(MAPTest.verifyHeaders(wsaHeaders, 
                                                        partialResponse));
        } catch (SOAPException se) {
            verificationCache.put("SOAP header verification failed: " + se);
        }
    }

    private void recordWSAHeaders(Element headers,
                                  List<String> wsaHeaders,
                                  String namespaceURI) {
        NodeList headerElements =
            headers.getElementsByTagNameNS(namespaceURI, "*");
        for (int i = 0; i < headerElements.getLength(); i++) {
            Node headerElement = headerElements.item(i);
            if (namespaceURI.equals(headerElement.getNamespaceURI())) {
                currentNamespaceURI = namespaceURI;
                wsaHeaders.add(headerElement.getLocalName());
            }
        }
    }

    private boolean isOutgoingPartialResponse(SoapMessage message) {
        AddressingProperties maps = 
            (AddressingProperties)message.get(SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
        return ContextUtils.isOutbound(message)
               && ContextUtils.isRequestor(message)
               && maps != null
               && Names.WSA_ANONYMOUS_ADDRESS.equals(maps.getTo().getValue());
    }
    
    private boolean isIncomingPartialResponse(SoapMessage message) 
        throws SOAPException {
        AddressingProperties maps = 
            (AddressingProperties)message.get(CLIENT_ADDRESSING_PROPERTIES_INBOUND);
        return !ContextUtils.isOutbound(message)
               && ContextUtils.isRequestor(message)
               && Names.WSA_ANONYMOUS_ADDRESS.equals(maps.getTo().getValue());
    }
    
    private Marshaller getMarshaller() throws JAXBException {
        JAXBContext jaxbContext =
            VersionTransformer.getExposedJAXBContext(currentNamespaceURI);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        return marshaller;
    }

    private void marshallFrom(String from, Element header, Marshaller marshaller) 
        throws JAXBException {
        if (Names.WSA_NAMESPACE_NAME.equals(currentNamespaceURI)) {
            String u = "urn:piggyback_responder";
            AttributedURIType value =
                org.apache.cxf.ws.addressing.ContextUtils.getAttributedURI(u);
            marshaller.marshal(
                new JAXBElement<AttributedURIType>(Names.WSA_FROM_QNAME,
                                                   AttributedURIType.class,
                                                   value),
                header);
        } else if (VersionTransformer.Names200408.WSA_NAMESPACE_NAME.equals(
                                                      currentNamespaceURI)) {
            AttributedURI value =
                VersionTransformer.Names200408.WSA_OBJECT_FACTORY.createAttributedURI();
            value.setValue(from);
            QName qname = new QName(VersionTransformer.Names200408.WSA_NAMESPACE_NAME, 
                                    Names.WSA_FROM_NAME);
            marshaller.marshal(
                new JAXBElement<AttributedURI>(qname,
                                               AttributedURI.class,
                                               value),
                header);
        }                                                                    
    }
    
    public void setVerificationCache(VerificationCache cache) {
        verificationCache = cache;
    }
}
