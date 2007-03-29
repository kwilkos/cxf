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

package org.apache.cxf.ws.rm.soap;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;


import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.BareInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.soap.MAPCodec;
import org.apache.cxf.ws.rm.AbstractRMInterceptor;
import org.apache.cxf.ws.rm.AckRequestedType;
import org.apache.cxf.ws.rm.RMConstants;
import org.apache.cxf.ws.rm.RMContextUtils;
import org.apache.cxf.ws.rm.RMEndpoint;
import org.apache.cxf.ws.rm.RMManager;
import org.apache.cxf.ws.rm.RMMessageConstants;
import org.apache.cxf.ws.rm.RMProperties;
import org.apache.cxf.ws.rm.SequenceAcknowledgement;
import org.apache.cxf.ws.rm.SequenceType;


/**
 * Protocol Handler responsible for {en|de}coding the RM 
 * Properties for {outgo|incom}ing messages.
 */
public class RMSoapInterceptor extends AbstractSoapInterceptor {

    protected static JAXBContext jaxbContext;

    private static final Logger LOG = LogUtils.getL7dLogger(RMSoapInterceptor.class);
    private static final String WS_RM_PACKAGE = 
        PackageUtils.getPackageName(SequenceType.class);
    
    private Set<String> after = Collections.singleton(MAPCodec.class.getName());

    /**
     * Constructor.
     */
    public RMSoapInterceptor() {
    } 
    
    // PhaseInterceptor interface

    public Set<String> getBefore() {
        return CastUtils.cast(Collections.EMPTY_SET);        
    }

    public Set<String> getAfter() {
        return after;
    }

    public String getId() {
        return RMSoapInterceptor.class.getName();
    }

    public String getPhase() {
        return Phase.PRE_PROTOCOL;
    }
      
    // AbstractSoapInterceptor interface 
    
    /**
     * @return the set of SOAP headers understood by this handler 
     */
    public Set<QName> getUnderstoodHeaders() {
        return RMConstants.getHeaders();
    }
    
    // Interceptor interface

    /* (non-Javadoc)
     * @see org.apache.cxf.interceptor.Interceptor#handleFault(org.apache.cxf.message.Message)
     */
    public void handleFault(SoapMessage message) {
        mediate(message);        
    }

    /* (non-Javadoc)
     * @see org.apache.cxf.interceptor.Interceptor#handleMessage(org.apache.cxf.message.Message)
     */

    public void handleMessage(SoapMessage message) throws Fault {
        mediate(message);
    }


    /**
     * Mediate message flow, peforming RMProperties {en|de}coding.
     * 
     * @param message the messsage
     */ 

    void mediate(SoapMessage message) {
        if (MessageUtils.isOutbound(message)) {
            encode(message);
        } else {
            decode(message);
            updateServiceModelInfo(message);
        }
    }
    
    /**
     * Encode the current RM properties in protocol-specific headers.
     *
     * @param message the SOAP message
     */
    
    void encode(SoapMessage message) {
        RMProperties rmps = RMContextUtils.retrieveRMProperties(message, true);
        if (null == rmps) {
            // nothing to encode
            return;
        }
        encode(message, rmps);
    }

    /**
     * Encode the current RM properties in protocol-specific headers.
     *
     * @param message the SOAP message.
     * @param rmps the current RM properties.
     */

    public static void encode(SoapMessage message, RMProperties rmps) {
        if (null == rmps) {
            return;
        }
        LOG.log(Level.FINE, "encoding RMPs in SOAP headers");
        
        try {
            Element header = message.getHeaders(Element.class);
            discardRMHeaders(header);
            
            // add WSRM namespace declaration to header, instead of
            // repeating in each individual child node
            header.setAttributeNS("http://www.w3.org/2000/xmlns/",
                                  "xmlns:" + RMConstants.getNamespacePrefix(),
                                 RMConstants.getNamespace());
            Marshaller marshaller = getJAXBContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
           
            SequenceType seq = rmps.getSequence();
            if (null != seq) {
                encodeProperty(seq, 
                               RMConstants.getSequenceQName(), 
                               SequenceType.class, 
                               header,
                               marshaller);
            } 
            Collection<SequenceAcknowledgement> acks = rmps.getAcks();
            if (null != acks) {
                for (SequenceAcknowledgement ack : acks) {
                    encodeProperty(ack, 
                                   RMConstants.getSequenceAckQName(), 
                                   SequenceAcknowledgement.class, 
                                   header,
                                   marshaller);
                }
            }
            Collection<AckRequestedType> requested = rmps.getAcksRequested();
            if (null != requested) {
                for (AckRequestedType ar : requested) {
                    encodeProperty(ar, 
                                   RMConstants.getAckRequestedQName(), 
                                   AckRequestedType.class, 
                                   header,
                                   marshaller);
                }
            }         
        } catch (SOAPException se) {
            LOG.log(Level.WARNING, "SOAP_HEADER_ENCODE_FAILURE_MSG", se); 
        } catch (JAXBException je) {
            LOG.log(Level.WARNING, "SOAP_HEADER_ENCODE_FAILURE_MSG", je);
        }        
    }
    
    /**
     * Decode the RM properties from protocol-specific headers
     * and store them in the message.
     *  
     * @param message the SOAP mesage
     */
    void decode(SoapMessage message) {
        RMProperties rmps = unmarshalRMProperties(message);
        RMContextUtils.storeRMProperties(message, rmps, false);
    }
    
    /**
     * Decode the RM properties from protocol-specific headers.
     * 
     * @param message the SOAP message
     * @return the RM properties
     */
    public RMProperties unmarshalRMProperties(SoapMessage message) { 
        RMProperties rmps = new RMProperties();
        
        try {
            Collection<SequenceAcknowledgement> acks = new ArrayList<SequenceAcknowledgement>();
            Collection<AckRequestedType> requested = new ArrayList<AckRequestedType>();           
            
            Element header = message.getHeaders(Element.class);
 
            if (header != null) {
                Unmarshaller unmarshaller = 
                    getJAXBContext().createUnmarshaller();
                NodeList headerElements = header.getChildNodes();
                for (int i = 0; i < headerElements.getLength(); i++) {
                    Node node = headerElements.item(i);
                    if (Node.ELEMENT_NODE != node.getNodeType()) {
                        continue;
                    }
                    Element headerElement = (Element)headerElements.item(i);
                    String headerURI = headerElement.getNamespaceURI();
                    String localName = headerElement.getLocalName();
                    if (RMConstants.getNamespace().equals(headerURI)) {
                        LOG.log(Level.FINE, "decoding RM header {0}", localName);
                        if (RMConstants.getSequenceName().equals(localName)) {
                            SequenceType s = decodeProperty(SequenceType.class,
                                                            headerElement,
                                                            unmarshaller);
                            
                            rmps.setSequence(s);
                        } else if (RMConstants.getSequenceAckName().equals(localName)) {
                            SequenceAcknowledgement ack = decodeProperty(SequenceAcknowledgement.class,
                                                            headerElement,
                                                            unmarshaller);
                            acks.add(ack);                            
                        } else if (RMConstants.getAckRequestedName().equals(localName)) {
                            AckRequestedType ar = decodeProperty(AckRequestedType.class,
                                                            headerElement,
                                                            unmarshaller);
                            requested.add(ar);
                        }
                    }
                }
                if (acks.size() > 0) {
                    rmps.setAcks(acks);
                }
                if (requested.size() > 0) {
                    rmps.setAcksRequested(requested);
                }
            } 
        } catch (JAXBException ex) {
            LOG.log(Level.WARNING, "SOAP_HEADER_DECODE_FAILURE_MSG", ex); 
        }
        return rmps;
    }


    /**
     * @return a JAXBContext
     */
    private static synchronized JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(WS_RM_PACKAGE);
        }
        return jaxbContext;
    }
    
    /**
     * Encodes an RM property as a SOAP header.
     *
     * @param value the value to encode
     * @param qname the QName for the header 
     * @param clz the class
     * @param header the SOAP header element
     * @param marshaller the JAXB marshaller to use
     */
    private static <T> void encodeProperty(T value, 
                                           QName qname, 
                                           Class<T> clz, 
                                           Element header,
                                           Marshaller marshaller)
        throws JAXBException {
        if (value != null) {
            LOG.log(Level.FINE, "encoding " + value + " into RM header {0}", qname);
            marshaller.marshal(new JAXBElement<T>(qname, clz, value), header);
        }
    }
    
    /**
     * Decodes an RM property from a SOAP header.
     * 
     * @param clz the class
     * @param headerElement the SOAP header element
     * @param marshaller the JAXB marshaller to use
     * @return the decoded EndpointReference
     */
    public static <T> T decodeProperty(Class<T> clz,
                                       Element headerElement,
                                       Unmarshaller unmarshaller)
        throws JAXBException {
        if (null == unmarshaller) {
            unmarshaller = getJAXBContext().createUnmarshaller();
        }
        JAXBElement<T> element =
            unmarshaller.unmarshal(headerElement, clz);
        return element.getValue();
    }


    /**
     * Discard any pre-existing RM headers - this may occur if the runtime
     * re-uses a SOAP message.
     *
     * @param header the SOAP header element
     */
    private static void discardRMHeaders(Element header) throws SOAPException {
        NodeList headerElements =
            header.getElementsByTagNameNS(RMConstants.getNamespace(), "*");
        
        for (int i = 0; i < headerElements.getLength(); i++) {
            Node headerElement = headerElements.item(i);
            if (RMConstants.getNamespace().equals(headerElement.getNamespaceURI())) {
                header.removeChild(headerElement);
            }

            
            // REVISIT should detach wsa:Action on resend
            /*
            if (org.apache.cxf.ws.addressing.Names.WSA_NAMESPACE_NAME
                .equals(headerName.getURI())
                && org.apache.cxf.ws.addressing.Names.WSA_ACTION_NAME
                .equals(headerName.getLocalName())) {
                headerElement.detachNode();
            }
            */
        }
    }
    
    
    /**
     * When invoked inbound, check if the action indicates that this is one of the 
     * RM protocol messages (CreateSequence, CreateSequenceResponse, TerminateSequence)
     * and if so, replace references to the application service model with references to
     * the RM service model.
     * The addressing protocol handler must have extracted the action beforehand. 
     * @see org.apache.cxf.transport.ChainInitiationObserver
     * 
     * @param message the message
     */
    private void updateServiceModelInfo(SoapMessage message) {

        AddressingProperties maps = RMContextUtils.retrieveMAPs(message, false, false);
        AttributedURIType actionURI = null == maps ? null : maps.getAction();
        String action = null == actionURI ? null : actionURI.getValue().trim();
        
        LOG.fine("action: " + action);
   
        if (!(RMConstants.getCreateSequenceAction().equals(action)
            || RMConstants.getCreateSequenceResponseAction().equals(action)
            || RMConstants.getTerminateSequenceAction().equals(action)
            || RMConstants.getSequenceAckAction().equals(action)
            || RMConstants.getLastMessageAction().equals(action))) {
            return;
        }
        
        LOG.info("Updating service model info in exchange");
        
        RMManager manager = getManager(message);
        assert manager != null;
        
        RMEndpoint rme = manager.getReliableEndpoint(message);
  
        Exchange exchange = message.getExchange();
        
        exchange.put(Endpoint.class, rme.getEndpoint());
        exchange.put(Service.class, rme.getService());
        exchange.put(Binding.class, rme.getEndpoint().getBinding());
        
        // Also set BindingOperationInfo as some operations (SequenceAcknowledgment) have
        // neither in nor out messages, and thus the WrappedInInterceptor cannot
        // determine the operation name.
        
        BindingInfo bi = rme.getEndpoint().getEndpointInfo().getBinding();
        BindingOperationInfo boi = null;
        boolean isOneway = true;
        if (RMConstants.getCreateSequenceAction().equals(action)) {
            if (RMContextUtils.isServerSide(message)) {
                boi = bi.getOperation(RMConstants.getCreateSequenceOperationName());
                isOneway = false;
            } else {
                boi = bi.getOperation(RMConstants.getCreateSequenceOnewayOperationName());
            }
        } else if (RMConstants.getCreateSequenceResponseAction().equals(action)) {
            if (RMContextUtils.isServerSide(message)) {
                boi = bi.getOperation(RMConstants.getCreateSequenceResponseOnewayOperationName());
            } else {
                boi = bi.getOperation(RMConstants.getCreateSequenceOperationName());
                isOneway = false;
            }
        } else if (RMConstants.getSequenceAckAction().equals(action)) {
            boi = bi.getOperation(RMConstants.getSequenceAckOperationName()); 
        } else if (RMConstants.getTerminateSequenceAction().equals(action)) {
            boi = bi.getOperation(RMConstants.getTerminateSequenceOperationName()); 
        }
        assert boi != null;
        exchange.put(BindingOperationInfo.class, boi);
        exchange.put(OperationInfo.class, boi.getOperationInfo());
        exchange.setOneWay(isOneway); 
        
        // Fix requestor role (as the client side message observer always sets it to TRUE) 
        // to allow unmarshalling the body of a server originated TerminateSequence request.
        // In the logical RM interceptor set it back to what it was so that the logical
        // addressing interceptor does not try to send a partial response to 
        // server originated oneway RM protocol messages.        
        // 
        
        if (!RMConstants.getCreateSequenceResponseAction().equals(action)) {
            LOG.fine("Changing requestor role from " + message.get(Message.REQUESTOR_ROLE)
                     + " to false");
            Object originalRequestorRole = message.get(Message.REQUESTOR_ROLE);
            if (null != originalRequestorRole) {
                message.put(RMMessageConstants.ORIGINAL_REQUESTOR_ROLE, originalRequestorRole);
            }
            message.put(Message.REQUESTOR_ROLE, Boolean.FALSE);
        }       
        
        // replace WrappedInInterceptor with BareInInterceptor if necessary
        // as RM protocol messages use paremeter style BARE

        InterceptorChain chain = message.getInterceptorChain();
        ListIterator it = chain.getIterator();            
        boolean bareIn = false;
        boolean wrappedIn = false;
        while (it.hasNext() && !wrappedIn && !bareIn) {
            PhaseInterceptor pi = (PhaseInterceptor)it.next();
            if (WrappedInInterceptor.class.getName().equals(pi.getId())) {
                wrappedIn = true;
                it.remove();
                LOG.fine("Removed WrappedInInterceptor from chain.");
            } else if (BareInInterceptor.class.getName().equals(pi.getId())) {
                bareIn = true;
            }
      
        }
        if (!bareIn) {
            chain.add(new BareInInterceptor());
            LOG.fine("Added BareInInterceptor to chain.");
        }
    }

    private RMManager getManager(SoapMessage message) {
        InterceptorChain chain = message.getInterceptorChain();
        ListIterator it = chain.getIterator();
        while (it.hasNext()) {
            Interceptor i = (Interceptor)it.next();
            if (i instanceof AbstractRMInterceptor) {
                return ((AbstractRMInterceptor)i).getManager();
            }
        }
        return null;
    }
}






