package org.objectweb.celtix.bindings.soap2;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.phase.Phase;

public class SoapOutInterceptor extends AbstractSoapInterceptor {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(SoapOutInterceptor.class);

    public SoapOutInterceptor() {
        super();
        setPhase(Phase.WRITE);
    }
    
    public void handleMessage(SoapMessage message) {
        try {
            XMLStreamWriter xtw = message.getContent(XMLStreamWriter.class);
            message.setContent(XMLStreamWriter.class, xtw);
            SoapVersion soapVersion = message.getVersion();
            if (soapVersion == null
                && message.getExchange().getInMessage() instanceof SoapMessage) {
                soapVersion = ((SoapMessage)message.getExchange().getInMessage()).getVersion();
                message.setVersion(soapVersion);
            }
            
            if (soapVersion == null) {
                soapVersion = Soap11.getInstance();
                message.setVersion(soapVersion);
            }
            
            xtw.writeStartElement(soapVersion.getPrefix(), 
                                  soapVersion.getEnvelope().getLocalPart(),
                                  soapVersion.getNamespace());
            xtw.writeNamespace(soapVersion.getPrefix(), soapVersion.getNamespace());
            Element eleHeaders = message.getHeaders(Element.class);

            if (eleHeaders != null) {
                serializeDom2XmlStreamWriter(eleHeaders, xtw, new HashSet<String>());
            }
            
            xtw.writeStartElement(soapVersion.getPrefix(), 
                                  soapVersion.getBody().getLocalPart(),
                                  soapVersion.getNamespace());
            
            // Calling for Wrapped/RPC/Doc/ Interceptor for writing SOAP body
            message.getInterceptorChain().doIntercept(message);

            xtw.writeEndElement();
            
            // Write Envelop end element
            xtw.writeEndElement();
            xtw.flush();
        } catch (XMLStreamException e) {
            throw new SoapFault(new Message("XML_STREAM_EXC", BUNDLE), SoapFault.SENDER);
        }
    }
    
    private static void serializeDom2XmlStreamWriter(Element element, XMLStreamWriter xtw,
                                                     Set<String> eleNsCache) throws XMLStreamException {

        xtw.writeStartElement(element.getPrefix(), element.getLocalName(), element.getNamespaceURI());
        if (!eleNsCache.contains(element.getPrefix() + element.getNamespaceURI())) {
            xtw.writeNamespace(element.getPrefix(), element.getNamespaceURI());
            eleNsCache.add(element.getPrefix() + element.getNamespaceURI());
        }
        Set<String> attrNsCache = new HashSet<String>();
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attributeNode = (Node)element.getAttributes().item(i);
            if (!attrNsCache.contains(attributeNode.getPrefix() + attributeNode.getNamespaceURI())) {
                xtw.writeNamespace(attributeNode.getPrefix(), attributeNode.getNamespaceURI());
                attrNsCache.add(attributeNode.getPrefix() + attributeNode.getNamespaceURI());
            }
            xtw.writeAttribute(attributeNode.getPrefix(), attributeNode.getNamespaceURI(), attributeNode
                .getLocalName(), attributeNode.getTextContent());
        }

        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node child = element.getChildNodes().item(i);
            if (child instanceof Element) {
                serializeDom2XmlStreamWriter((Element)child, xtw, eleNsCache);
            } else if (child instanceof Text) {
                xtw.writeCharacters(((Text)child).getWholeText());
            } else if (child instanceof CharacterData) {
                xtw.writeCharacters(((CharacterData)child).getData());
            } else if (child instanceof CDATASection) {
                xtw.writeCharacters(((CDATASection)child).getWholeText());
            }
        }
        xtw.writeEndElement();
    }
}
