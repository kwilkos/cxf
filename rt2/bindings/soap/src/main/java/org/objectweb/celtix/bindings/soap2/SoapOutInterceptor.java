package org.objectweb.celtix.bindings.soap2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import org.objectweb.celtix.bindings.soap2.attachments.AttachmentUtil;
import org.objectweb.celtix.bindings.soap2.attachments.CachedOutputStream;
import org.objectweb.celtix.message.Attachment;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.staxutils.StaxUtils;

public class SoapOutInterceptor extends AbstractSoapInterceptor {

    private static long threshCount;
    private SoapMessage soapMessage;
    private XMLStreamWriter xtw;

    public void handleMessage(SoapMessage message) {
        // Create XML Stream Writer from Output Stream setted by
        // TransportOutInterceptor
        soapMessage = (SoapMessage)message;
        OutputStream ops = (OutputStream)soapMessage.getResult(OutputStream.class);
        try {
            threshCount++;
            CachedOutputStream cos = new CachedOutputStream(threshCount, null);
            xtw = StaxUtils.createXMLStreamWriter(cos);
            soapMessage.setResult(XMLStreamWriter.class, xtw);
            SoapVersion soapVersion = soapMessage.getVersion();
            xtw.writeStartElement(soapVersion.getPrefix(), soapVersion.getEnvelope().getLocalPart(),
                                  soapVersion.getNamespace());
            xtw.writeNamespace(soapVersion.getPrefix(), soapVersion.getNamespace());
            Element eleHeaders = soapMessage.getHeaders(Element.class);
            serializeDom2XmlStreamWriter(eleHeaders, xtw, new HashSet<String>());
            // Calling for Wrapped/Rpt/Doc/ Interceptor for writing SOAP body
            message.getInterceptorChain().doIntercept(message);
            // Write Envelop end element
            xtw.writeEndElement();
            xtw.flush();
            soapMessage.setResult(InputStream.class, cos.getInputStream());
            Collection<Attachment> attachments = message.getAttachments();
            if (attachments.size() > 0) {
                AttachmentUtil.serializeMultipartMessage(soapMessage, ops);
            } else {
                streamCopy(cos.getInputStream(), ops);
            }
        } catch (Exception e) {
            soapMessage.put(Message.OUTBOUND_EXCEPTION, e);
            return;
        }
        // Continue the Chain processing
        message.getInterceptorChain().doIntercept(message);
    }

    private static void streamCopy(InputStream input, OutputStream output) throws IOException {
        try {
            final byte[] buffer = new byte[8096];
            int n = input.read(buffer);
            while (n > 0) {
                output.write(buffer, 0, n);
                n = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }

    private static void serializeDom2XmlStreamWriter(Element element, XMLStreamWriter xtw,
                                                     Set<String> eleNsCache)
        throws XMLStreamException {

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
