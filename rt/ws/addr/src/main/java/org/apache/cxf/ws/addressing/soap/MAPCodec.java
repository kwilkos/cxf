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

package org.apache.cxf.ws.addressing.soap;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AddressingPropertiesImpl;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.Names;
import org.apache.cxf.ws.addressing.RelatesToType;

import static org.apache.cxf.message.Message.MIME_HEADERS;


/**
 * SOAP interceptor responsible for {en|de}coding the Message Addressing 
 * Properties for {outgo|incom}ing messages.
 */
public class MAPCodec extends AbstractSoapInterceptor {

    private static final Logger LOG = LogUtils.getL7dLogger(MAPCodec.class);
    private static SOAPFactory soapFactory;
    
    private VersionTransformer transformer;

    /**
     * Constructor.
     */
    public MAPCodec() {
        super();
        setPhase(Phase.PRE_PROTOCOL);
        transformer = new VersionTransformer(this);
    } 

    /**
     * @return the set of SOAP headers understood by this handler 
     */
    public Set<QName> getUnderstoodHeaders() {
        return VersionTransformer.HEADERS;
    }
    
    /**
     * Invoked for normal processing of inbound and outbound messages.
     *
     * @param message the messsage
     */
    public void handleMessage(SoapMessage message) {
        mediate(message);
    }

    /**
     * Invoked for fault processing.
     *
     * @param message the messsage message
     */
    public void handleFault(SoapMessage message) {
        mediate(message);
    }

    /**
     * Mediate message flow, peforming MAP {en|de}coding.
     * 
     * @param message the messsage message
     */     
    private void mediate(SoapMessage message) {
        if (ContextUtils.isOutbound(message)) {
            encode(message, ContextUtils.retrieveMAPs(message, false, true));
        } else {
            ContextUtils.storeMAPs(decode(message), message, false);
        }
    }

    /**
     * Encode the current MAPs in protocol-specific headers.
     *
     * @param message the messsage message
     * @param maps the MAPs to encode
     */
    private void encode(SoapMessage message, 
                        AddressingProperties maps) {
        if (maps != null) { 
            LOG.log(Level.INFO, "encoding MAPs in SOAP headers");
            try {
                Element header = message.getHeaders(Element.class);
                discardMAPs(header, maps);
                // add WSA namespace declaration to header, instead of
                // repeating in each individual child node
                header.setAttributeNS("http://www.w3.org/2000/xmlns/",
                                      "xmlns:" + Names.WSA_NAMESPACE_PREFIX,
                                      maps.getNamespaceURI());
                JAXBContext jaxbContext = 
                    VersionTransformer.getExposedJAXBContext(
                                                     maps.getNamespaceURI());
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                transformer.encodeAsExposed(maps.getNamespaceURI(),
                                            maps.getMessageID(), 
                                            Names.WSA_MESSAGEID_NAME,
                                            AttributedURIType.class, 
                                            header, 
                                            marshaller);
                transformer.encodeAsExposed(maps.getNamespaceURI(),
                                            maps.getTo(), 
                                            Names.WSA_TO_NAME,
                                            AttributedURIType.class,  
                                            header, 
                                            marshaller);
                transformer.encodeAsExposed(maps.getNamespaceURI(),
                                            maps.getReplyTo(), 
                                            Names.WSA_REPLYTO_NAME, 
                                            EndpointReferenceType.class,
                                            header,
                                            marshaller);
                transformer.encodeAsExposed(maps.getNamespaceURI(),
                                            maps.getFaultTo(), 
                                            Names.WSA_FAULTTO_NAME, 
                                            EndpointReferenceType.class,
                                            header,
                                            marshaller);
                transformer.encodeAsExposed(maps.getNamespaceURI(),
                                            maps.getRelatesTo(),
                                            Names.WSA_RELATESTO_NAME,
                                            RelatesToType.class,
                                            header,
                                            marshaller);
                transformer.encodeAsExposed(maps.getNamespaceURI(),
                                            maps.getAction(), 
                                            Names.WSA_ACTION_NAME,
                                            AttributedURIType.class, 
                                            header, 
                                            marshaller);
                propogateAction(maps.getAction(), message);
                applyMAPValidation(message);
            } catch (SOAPException se) {
                LOG.log(Level.WARNING, "SOAP_HEADER_ENCODE_FAILURE_MSG", se); 
            } catch (JAXBException je) {
                LOG.log(Level.WARNING, "SOAP_HEADER_ENCODE_FAILURE_MSG", je);
            }
        }
    }

    /**
     * Decode the MAPs from protocol-specific headers.
     *  
     * @param message the SOAP message
     * @param the decoded MAPs
     * @exception SOAPFaultException if decoded MAPs are invalid 
     */
    public AddressingProperties unmarshalMAPs(SoapMessage message) {
        // REVISIT generate MessageAddressingHeaderRequired fault if an
        // expected header is missing 
        AddressingPropertiesImpl maps = null;
        try {
            Element header = message.getHeaders(Element.class);
            if (header != null) {
                Unmarshaller unmarshaller = null;
                NodeList headerElements = header.getChildNodes();
                int headerCount = headerElements.getLength();
                for (int i = 0; i < headerCount; i++) {
                    if (headerElements.item(i) instanceof Element) {
                        Element headerElement = (Element)headerElements.item(i);
                        String headerURI = headerElement.getNamespaceURI();
                        if (unmarshaller == null) {
                            JAXBContext jaxbContext = 
                                VersionTransformer.getExposedJAXBContext(headerURI);
                            unmarshaller = 
                                jaxbContext.createUnmarshaller();
                        }
                        if (transformer.isSupported(headerURI)) {
                            if (maps == null) {
                                maps = new AddressingPropertiesImpl();
                                maps.exposeAs(headerURI);
                            }
                            String localName = headerElement.getLocalName();
                            LOG.log(Level.INFO, "decoding WSA header {0}", localName);
                            if (Names.WSA_MESSAGEID_NAME.equals(localName)) {
                                maps.setMessageID(transformer.decodeAsNative(
                                                          headerURI,
                                                          AttributedURIType.class,
                                                          headerElement, 
                                                          unmarshaller));
                            } else if (Names.WSA_TO_NAME.equals(localName)) {
                                maps.setTo(transformer.decodeAsNative(
                                                          headerURI,
                                                          AttributedURIType.class,
                                                          headerElement, 
                                                          unmarshaller));
                            } else if (Names.WSA_REPLYTO_NAME.equals(localName)) {
                                maps.setReplyTo(transformer.decodeAsNative(
                                                           headerURI,
                                                           EndpointReferenceType.class,
                                                           headerElement, 
                                                           unmarshaller));
                            } else if (Names.WSA_FAULTTO_NAME.equals(localName)) {
                                maps.setFaultTo(transformer.decodeAsNative(
                                                           headerURI,
                                                           EndpointReferenceType.class,
                                                           headerElement, 
                                                           unmarshaller));
                            } else if (Names.WSA_RELATESTO_NAME.equals(localName)) {
                                maps.setRelatesTo(transformer.decodeAsNative(
                                                           headerURI,
                                                           RelatesToType.class,
                                                           headerElement, 
                                                           unmarshaller));
                            } else if (Names.WSA_ACTION_NAME.equals(localName)) {
                                maps.setAction(transformer.decodeAsNative(
                                                          headerURI,
                                                          AttributedURIType.class,
                                                          headerElement, 
                                                          unmarshaller));
                            }
                        } else if (headerURI.contains(Names.WSA_NAMESPACE_PATTERN)) {
                            LOG.log(Level.WARNING, 
                                    "UNSUPPORTED_VERSION_MSG",
                                    headerURI);
                        }
                    }
                }
            }
        } catch (JAXBException je) {
            LOG.log(Level.WARNING, "SOAP_HEADER_DECODE_FAILURE_MSG", je); 
        }
        return maps;
    }
    
    /**
     * Decode the MAPs from protocol-specific headers.
     *  
     * @param message the messsage
     * @param the decoded MAPs
     * @exception SOAPFaultException if decoded MAPs are invalid 
     */
    private AddressingProperties decode(SoapMessage message) {
        // REVISIT generate MessageAddressingHeaderRequired fault if an
        // expected header is missing 
        AddressingProperties maps = null;
        boolean isRequestor = ContextUtils.isRequestor(message);
        maps = unmarshalMAPs(message);
        if (isRequestor && null != maps.getRelatesTo()) {
            ContextUtils.storeCorrelationID(maps.getRelatesTo(),
                                            false,
                                            message);
        }
        return maps;
    }

    /**
     * Encodes an MAP as a SOAP header.
     *
     * @param value the value to encode
     * @param qname the QName for the header 
     * @param clz the class
     * @param header the SOAP header element
     * @param marshaller the JAXB marshaller to use
     */
    protected <T> void encodeMAP(T value,
                                 QName qname,
                                 Class<T> clz,
                                 Element header,
                                 Marshaller marshaller) throws JAXBException {
        LOG.log(Level.INFO, "encoding WSA header {0}", qname);
        if (value != null) {
            marshaller.marshal(new JAXBElement<T>(qname, clz, value), header);
        }
    }

    /**
     * Decodes a MAP from a SOAP header.
     *
     * @param clz the class
     * @param headerElement the SOAP header element
     * @param marshaller the JAXB marshaller to use
     * @return the decoded value
     */
    protected <T> T decodeMAP(Class<T> clz,
                              Element headerElement,
                              Unmarshaller unmarshaller) throws JAXBException {
        JAXBElement<T> element =
            unmarshaller.unmarshal(headerElement, clz);
        return element.getValue();
    }

    /**
     * Discard any pre-existing MAP headers - this may occur if the runtime
     * re-uses a SOAP message.
     *
     * @param header the SOAP header
     * @param maps the current MAPs
     */
    private void discardMAPs(Element header, AddressingProperties maps) throws SOAPException {
        NodeList headerElements =
            header.getElementsByTagNameNS(maps.getNamespaceURI(), "*");
        for (int i = 0; i < headerElements.getLength(); i++) {
            Node headerElement = headerElements.item(i);
            if (Names.WSA_NAMESPACE_NAME.equals(headerElement.getNamespaceURI())) {
                header.removeChild(headerElement);
            }
        }
    }

    /**
     * Propogate action to SOAPAction header
     *
     * @param action the Action property
     * @param message the SOAP message
     */
    private void propogateAction(AttributedURIType action, 
                                 SoapMessage message) {
        if (!(action == null || "".equals(action.getValue()))) {
            Map<String, List<String>> mimeHeaders = CastUtils.cast((Map<?, ?>)
                message.get(MIME_HEADERS));
            if (mimeHeaders != null) {
                List<String> soapActionHeaders =
                    mimeHeaders.get(Names.SOAP_ACTION_HEADER);
                // only propogate to SOAPAction header if currently non-empty
                if (!(soapActionHeaders == null
                      || soapActionHeaders.size() == 0
                      || "".equals(soapActionHeaders.get(0)))) {
                    LOG.log(Level.INFO, 
                            "encoding wsa:Action in SOAPAction header {0}",
                            action.getValue());
                    soapActionHeaders.clear();
                    soapActionHeaders.add("\"" + action.getValue() + "\"");
                }
            }
        }
    }

    /**
     * Apply results of validation of incoming MAPs.
     *
     * @param message the message
     * @exception SOAPFaultException if the MAPs are invalid
     * @exception SOAPException if SOAPFault cannot be constructed
     */
    private void applyMAPValidation(SoapMessage message)
        throws SOAPException {
        String faultName = ContextUtils.retrieveMAPFaultName(message);
        if (faultName != null) {
            String reason = ContextUtils.retrieveMAPFaultReason(message);
            throw createSOAPFaultException(faultName, 
                                           Names.WSA_NAMESPACE_PREFIX,
                                           Names.WSA_NAMESPACE_NAME,
                                           reason);
        }
    }

    /**
     * @return SOAPFactory
     */
    private static synchronized SOAPFactory getSOAPFactory() throws SOAPException {
        if (soapFactory == null) {
            soapFactory = SOAPFactory.newInstance();
        }
        return soapFactory;
    }

    /**
     * Create a SOAPFaultException.
     *
     * @param localName the fault local name
     * @param prefix the fault prefix
     * @param namespace the fault namespace
     * @param reason the fault reason
     * @return a new SOAPFaultException
     */ 
    private SOAPFaultException createSOAPFaultException(String localName,
                                                        String prefix,
                                                        String namespace,
                                                        String reason) 
        throws SOAPException {
        SOAPFactory factory = getSOAPFactory();
        SOAPFault fault = factory.createFault();
        Name qname = factory.createName(localName, prefix, namespace);
        fault.setFaultCode(qname);
        fault.setFaultString(reason);
        return new SOAPFaultException(fault);
    }
}






