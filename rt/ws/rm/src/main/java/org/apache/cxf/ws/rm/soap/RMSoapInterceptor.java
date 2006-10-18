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

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.addressing.soap.MAPCodec;
import org.apache.cxf.ws.rm.AckRequestedType;
import org.apache.cxf.ws.rm.RMConstants;
import org.apache.cxf.ws.rm.RMContextUtils;
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
    
    private Set<String> before = Collections.singleton(MAPCodec.class.getName());

    /**
     * Constructor.
     */
    public RMSoapInterceptor() {
    } 
    
    // PhaseInterceptor interface

    public Set<String> getAfter() {
        return CastUtils.cast(Collections.EMPTY_SET);        
    }

    public Set<String> getBefore() {
        return before;
    }

    public String getId() {
        return RMSoapInterceptor.class.getName();
    }

    public String getPhase() {
        return Phase.PRE_LOGICAL;
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
        if (RMContextUtils.isOutbound(message)) {
            encode(message);
        } else {
            decode(message);
            // storeBindingInfo(context);
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
                                  "xmlns:" + RMConstants.WSRM_NAMESPACE_PREFIX,
                                 RMConstants.WSRM_NAMESPACE_NAME);
            Marshaller marshaller = getJAXBContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
           
            SequenceType seq = rmps.getSequence();
            if (null != seq) {
                encodeProperty(seq, 
                               RMConstants.WSRM_SEQUENCE_QNAME, 
                               SequenceType.class, 
                               header,
                               marshaller);
            } 
            Collection<SequenceAcknowledgement> acks = rmps.getAcks();
            if (null != acks) {
                for (SequenceAcknowledgement ack : acks) {
                    encodeProperty(ack, 
                                   RMConstants.WSRM_SEQUENCE_ACK_QNAME, 
                                   SequenceAcknowledgement.class, 
                                   header,
                                   marshaller);
                }
            }
            Collection<AckRequestedType> requested = rmps.getAcksRequested();
            if (null != requested) {
                for (AckRequestedType ar : requested) {
                    encodeProperty(ar, 
                                   RMConstants.WSRM_ACK_REQUESTED_QNAME, 
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
                    if (RMConstants.WSRM_NAMESPACE_NAME.equals(headerURI)) {
                        LOG.log(Level.INFO, "decoding RM header {0}", localName);
                        if (RMConstants.WSRM_SEQUENCE_NAME.equals(localName)) {
                            SequenceType s = decodeProperty(SequenceType.class,
                                                            headerElement,
                                                            unmarshaller);
                            
                            rmps.setSequence(s);
                        } else if (RMConstants.WSRM_SEQUENCE_ACK_NAME.equals(localName)) {
                            SequenceAcknowledgement ack = decodeProperty(SequenceAcknowledgement.class,
                                                            headerElement,
                                                            unmarshaller);
                            acks.add(ack);                            
                        } else if (RMConstants.WSRM_ACK_REQUESTED_NAME.equals(localName)) {
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
            LOG.log(Level.INFO, "encoding " + value + " into RM header {0}", qname);
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
            header.getElementsByTagNameNS(RMConstants.WSRM_NAMESPACE_NAME, "*");
        
        for (int i = 0; i < headerElements.getLength(); i++) {
            Node headerElement = headerElements.item(i);
            if (RMConstants.WSRM_NAMESPACE_NAME.equals(headerElement.getNamespaceURI())) {
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
     * and if so, store method, operation name and data binding callback in the context.
     * The action has already been extracted from its associated soap header into the
     * addressing properties as the addressing protocol handler is executed. 
     * 
     * @param context
     */
    /*
    private void storeBindingInfo(MessageContext context) {
        assert !ContextUtils.isOutbound(context);
        AddressingProperties maps = ContextUtils.retrieveMAPs(context, false, false);
        AttributedURIType actionURI = null == maps ? null : maps.getAction();
        String action = null == actionURI ? null : actionURI.getValue();
        DataBindingCallback callback = null;
        String operationName = null;
        boolean rmProtocolMessage = true;

        if (RMUtils.getRMConstants().getCreateSequenceAction().equals(action)) {
            callback = CreateSequenceRequest.createDataBindingCallback();
            operationName = CreateSequenceRequest.getOperationName();
        } else if (RMUtils.getRMConstants().getCreateSequenceResponseAction().equals(action)) {
            callback = CreateSequenceResponse.createDataBindingCallback();
            operationName = CreateSequenceResponse.getOperationName();
        } else if (RMUtils.getRMConstants().getTerminateSequenceAction().equals(action)) {
            callback = TerminateSequenceRequest.createDataBindingCallback();
            operationName = TerminateSequenceRequest.getOperationName();
        } else if (RMUtils.getRMConstants().getLastMessageAction().equals(action) 
            || RMUtils.getRMConstants().getSequenceAcknowledgmentAction().equals(action)) {
            // It does not really matter what callback we are using here as the body
            // in messages with these actions is always empty
            callback = TerminateSequenceRequest.createDataBindingCallback();
            operationName = TerminateSequenceRequest.getOperationName();
        } else {
            rmProtocolMessage = false;
        }
        
        if (rmProtocolMessage) {
            BindingContextUtils.storeDispatch(context, false);
            BindingContextUtils.storeDataBindingCallback(context, callback);
            context.put(MessageContext.WSDL_OPERATION, new QName("", operationName));
            context.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);            
        }
    }
    */

}






