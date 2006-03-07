package org.objectweb.celtix.bus.ws.addressing.soap;


import java.util.Iterator;
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
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.addressing.Names;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.addressing.RelatesToType;


/**
 * Protocol Handler responsible for {en|de}coding the Message Addressing 
 * Properties for {outgo|incom}ing messages.
 */
public class MAPCodec 
    implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOG = LogUtils.getL7dLogger(MAPCodec.class);
    private static SOAPFactory soapFactory;
    
    private VersionTransformer transformer;

    /**
     * Constructor.
     */
    public MAPCodec() {
        transformer = new VersionTransformer(this);
    } 

    /**
     * Initialize the handler.
     */
    public void init(Map<String, Object> map) {
    }

    /**
     * @return the set of SOAP headers understood by this handler 
     */
    public Set<QName> getHeaders() {
        return VersionTransformer.HEADERS;
    }
    
    /**
     * Invoked for normal processing of inbound and outbound messages.
     *
     * @param context the messsage context
     */
    public boolean handleMessage(SOAPMessageContext context) {
        return mediate(context);
    }

    /**
     * Invoked for fault processing.
     *
     * @param context the messsage context
     */
    public boolean handleFault(SOAPMessageContext context) {
        return mediate(context);
    }

    /**
     * Called at the conclusion of a message exchange pattern just prior to
     * the JAX-WS runtime dispatching a message, fault or exception.
     *
     * @param context the message context
     */
    public void close(MessageContext context) {
    }

    /**
     * Release handler resources.
     */
    public void destroy() {
    }

    /**
     * Mediate message flow, peforming MAP {en|de}coding.
     * 
     * @param context the messsage context
     * @return true if processing should continue on dispatch path 
     */     
    private boolean mediate(SOAPMessageContext context) {
        if (ContextUtils.isOutbound(context)) {
            encode(context, ContextUtils.retrieveMAPs(context, false, true));
        } else {
            ContextUtils.storeMAPs(decode(context), context, false);
        }
        return true;
    }

    /**
     * Encode the current MAPs in protocol-specific headers.
     *
     * @param context the messsage context
     * @param maps the MAPs to encode
     */
    private void encode(SOAPMessageContext context, 
                        AddressingProperties maps) {
        if (maps != null) {
            SOAPMessage message = context.getMessage();
            LOG.log(Level.INFO, "encoding MAPs in SOAP headers");
            try {
                SOAPEnvelope env = message.getSOAPPart().getEnvelope();
                SOAPHeader header = env.getHeader() != null 
                                    ? env.getHeader()
                                    : env.addHeader();
                discardMAPs(header);
                header.addNamespaceDeclaration(Names.WSA_NAMESPACE_PREFIX,
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
                applyMAPValidation(context);
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
     * @param context the messsage context
     * @param the decoded MAPs
     * @exception SOAPFaultException if decoded MAPs are invalid 
     */
    private AddressingProperties decode(SOAPMessageContext context) {
        // REVISIT generate MessageAddressingHeaderRequired fault if an
        // expected header is missing 
        AddressingPropertiesImpl maps = null;
        boolean isRequestor = ContextUtils.isRequestor(context);
        try {
            SOAPMessage message = context.getMessage();
            SOAPEnvelope env = message.getSOAPPart().getEnvelope();
            SOAPHeader header = env.getHeader();
            if (header != null) {
                Unmarshaller unmarshaller = null;
                Iterator headerElements = header.examineAllHeaderElements();
                while (headerElements.hasNext()) {
                    SOAPHeaderElement headerElement = 
                        (SOAPHeaderElement)headerElements.next();
                    Name headerName = headerElement.getElementName();
                    String headerURI = headerName.getURI();
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
                        String localName = headerName.getLocalName();
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
                        } else if (Names.WSA_RELATESTO_NAME.equals(localName)) {
                            maps.setRelatesTo(transformer.decodeAsNative(
                                                       headerURI,
                                                       RelatesToType.class,
                                                       headerElement, 
                                                       unmarshaller));
                            if (isRequestor) {
                                ContextUtils.storeCorrelationID(maps.getRelatesTo(),
                                                                false,
                                                                context);
                            }
                        } else if (Names.WSA_ACTION_NAME.equals(localName)) {
                            maps.setAction(transformer.decodeAsNative(
                                                      headerURI,
                                                      AttributedURIType.class,
                                                      headerElement, 
                                                      unmarshaller));
                        }
                    } else {
                        LOG.log(Level.WARNING, 
                                "UNSUPPORTED_VERSION_MSG",
                                headerURI);
                    }
                }
            }
        } catch (SOAPException se) {
            LOG.log(Level.WARNING, "SOAP_HEADER_DECODE_FAILURE_MSG", se); 
        } catch (JAXBException je) {
            LOG.log(Level.WARNING, "SOAP_HEADER_DECODE_FAILURE_MSG", je); 
        }
        return maps;
    }

    /**
     * Encodes an MAP as a SOAP header.
     *
     * @param value the value to encode
     * @param qname the QName for the header 
     * @param clz the class
     * @param header the SOAP header
     * @param marshaller the JAXB marshaller to use
     */
    protected <T> void encodeMAP(T value,
                                 QName qname,
                                 Class<T> clz,
                                 SOAPHeader header,
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
                              SOAPHeaderElement headerElement,
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
     */
    private void discardMAPs(SOAPHeader header) throws SOAPException {
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement =
                (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            if (Names.WSA_NAMESPACE_NAME.equals(headerName.getURI())) {
                headerElement.detachNode();
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
                                 SOAPMessage message) {
        if (!(action == null || "".equals(action.getValue()))) {
            MimeHeaders mimeHeaders = message.getMimeHeaders();
            String[] soapActionHeaders = 
                mimeHeaders.getHeader(Names.SOAP_ACTION_HEADER);
            // only propogate to SOAPAction header if currently non-empty
            if (!(soapActionHeaders == null
                  || soapActionHeaders.length == 0
                  || "".equals(soapActionHeaders[0]))) {
                LOG.log(Level.INFO, 
                        "encoding wsa:Action in SOAPAction header {0}",
                        action.getValue());
                String soapAction = "\"" + action.getValue() + "\"";
                mimeHeaders.setHeader(Names.SOAP_ACTION_HEADER, soapAction);
            }
        }
    }

    /**
     * Apply results of validation of incoming MAPs.
     *
     * @param context the message context
     * @exception SOAPFaultException if the MAPs are invalid
     * @exception SOAPException if SOAPFault cannot be constructed
     */
    private void applyMAPValidation(SOAPMessageContext context)
        throws SOAPException {
        String faultName = ContextUtils.retrieveMAPFaultName(context);
        if (faultName != null) {
            String reason = ContextUtils.retrieveMAPFaultReason(context);
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






