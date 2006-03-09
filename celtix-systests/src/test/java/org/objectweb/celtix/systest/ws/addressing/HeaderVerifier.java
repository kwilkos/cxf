package org.objectweb.celtix.systest.ws.addressing;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.addressing.Names;
import org.objectweb.celtix.bus.ws.addressing.soap.VersionTransformer;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.v200408.AttributedURI;

import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;


/**
 * Verifies presence of expected SOAP headers.
 */
public class HeaderVerifier implements SOAPHandler<SOAPMessageContext> {
    VerificationCache verificationCache;
    String currentNamespaceURI;
    
    public void init(Map<String, Object> map) {
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        addPartialResponseHeader(context);
        verify(context);
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        verify(context);
        return true;
    }

    public void close(MessageContext context) {
    }

    public void destroy() {
    }
        
    private void addPartialResponseHeader(SOAPMessageContext context) {
        try {
            // add piggybacked wsa:From header to partial response
            if (isOutgoingPartialResponse(context)) {
                SOAPEnvelope env = context.getMessage().getSOAPPart().getEnvelope();
                SOAPHeader header = env.getHeader() != null 
                                    ? env.getHeader()
                                    : env.addHeader();
                marshallFrom("urn:piggyback_responder", header, getMarshaller());
            }
        } catch (Exception e) {
            verificationCache.put("SOAP header addition failed: " + e);
            e.printStackTrace();
        }
    }

    private void verify(SOAPMessageContext context) {
        try {
            List<String> wsaHeaders = new ArrayList<String>();
            SOAPHeader header = 
                context.getMessage().getSOAPPart().getEnvelope().getHeader();
            if (header != null) {
                Iterator headerElements = header.examineAllHeaderElements();
                while (headerElements.hasNext()) {
                    Name headerName = ((SOAPHeaderElement)headerElements.next()).getElementName();
                    if (isAddressingNamespace(headerName.getURI())) {
                        wsaHeaders.add(headerName.getLocalName());
                    }
                }
            }
            boolean partialResponse = isIncomingPartialResponse(context)
                                      || isOutgoingPartialResponse(context);
            verificationCache.put(MAPTest.verifyHeaders(wsaHeaders, 
                                                        partialResponse));
        } catch (SOAPException se) {
            verificationCache.put("SOAP header verification failed: " + se);
        }
    }
    
    private boolean isAddressingNamespace(String namespace) {
        boolean isAddressing = false;
        if (Names.WSA_NAMESPACE_NAME.equals(namespace)
            || VersionTransformer.Names200408.WSA_NAMESPACE_NAME.equals(
                                                                namespace)) {
            currentNamespaceURI = namespace;
            isAddressing = true;
        }
        return isAddressing;
    }
    
    private boolean isOutgoingPartialResponse(SOAPMessageContext context) {
        AddressingProperties maps = 
            (AddressingProperties)context.get(SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
        return ContextUtils.isOutbound(context)
               && ContextUtils.isRequestor(context)
               && Names.WSA_ANONYMOUS_ADDRESS.equals(maps.getTo().getValue());
    }
    
    private boolean isIncomingPartialResponse(SOAPMessageContext context) 
        throws SOAPException {
        SOAPBody body = 
            context.getMessage().getSOAPPart().getEnvelope().getBody();
        return !ContextUtils.isOutbound(context)
               && ContextUtils.isRequestor(context)
               && !body.getChildElements().hasNext();
    }
    
    private Marshaller getMarshaller() throws JAXBException {
        JAXBContext jaxbContext =
            VersionTransformer.getExposedJAXBContext(currentNamespaceURI);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        return marshaller;
    }

    private void marshallFrom(String from, SOAPHeader header, Marshaller marshaller) 
        throws JAXBException {
        if (Names.WSA_NAMESPACE_NAME.equals(currentNamespaceURI)) {
            AttributedURIType value =
                ContextUtils.getAttributedURI("urn:piggyback_responder");
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
}
