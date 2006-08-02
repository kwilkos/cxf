package org.objectweb.celtix.bindings.soap2;

import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.objectweb.celtix.staxutils.StaxUtils;

public class ReadHeadersInterceptor extends AbstractSoapInterceptor {

    public void handleMessage(SoapMessage messageParam) {

        SoapMessage message = (SoapMessage)messageParam;
        try {
            InputStream in = (InputStream)message.getContent(InputStream.class);
            if (in == null) {
                throw new WebServiceException("Missing Soap part input stream in soap message");
            }
            XMLStreamReader xmlReader = StaxUtils.createXMLStreamReader(in);
            message.setContent(XMLStreamReader.class, xmlReader);
            process(xmlReader, message);
        } catch (Exception e) {
            message.setContent(Exception.class, e);
            return;
        }
    }

    private void process(XMLStreamReader xmlReader, SoapMessage message) throws Exception {
        boolean found = false;
        try {
            while (xmlReader.hasNext()) {
                xmlReader.nextTag();
                if (xmlReader.getLocalName().equals(message.getVersion().getEnvelope().getLocalPart())) {
                    xmlReader.nextTag();
                    if (xmlReader.getLocalName().equals(message.getVersion().getHeader().getLocalPart())) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = null;
                builder = builderFactory.newDocumentBuilder();
                Document doc = builder.newDocument();
                addHeaderElementIntoDoc(xmlReader, message, doc);
            } else {
                throw new SOAPException("Parsing soap message error, "
                                        + "can't find <Soap:Header> in message part!");
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw e;
        }
    }

    private void addHeaderElementIntoDoc(XMLStreamReader xmlReader, SoapMessage message, Document doc)
        throws XMLStreamException {
        QName name = new QName(xmlReader.getNamespaceURI(), xmlReader.getLocalName());
        Element eleHeaders = doc.createElementNS(name.getNamespaceURI(), name.getLocalPart());
        eleHeaders.setPrefix(message.getVersion().getPrefix());
        processElement(null, eleHeaders, xmlReader, doc);
        message.setHeaders(Element.class, eleHeaders);
    }

    private void processElement(Element parent, Element ele, XMLStreamReader xmlReader, Document doc)
        throws XMLStreamException {
        int attIndex = 0;
        StringBuffer sb = null;
        boolean hasChild = false;
        boolean readEndTag = false;
        int eventType = -1;
        while (xmlReader.hasNext()) {
            eventType = xmlReader.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                QName name = new QName(xmlReader.getNamespaceURI(), xmlReader.getLocalName());
                Element eleChild = doc.createElementNS(name.getNamespaceURI(), name.getLocalPart());
                for (int i = 0; i < xmlReader.getAttributeCount(); i++) {
                    Attr attr = doc.createAttributeNS(xmlReader.getAttributeNamespace(i), xmlReader
                        .getAttributeLocalName(i));
                    attr.setPrefix(xmlReader.getAttributePrefix(i));
                    attr.setTextContent(xmlReader.getAttributeValue(i));
                    eleChild.setAttributeNode(attr);
                }
                eleChild.setPrefix(xmlReader.getPrefix());
                hasChild = true;
                processElement(ele, eleChild, xmlReader, doc);
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                if (!hasChild && sb != null) {
                    ele.setTextContent(sb.toString());
                }
                if (parent == null) {
                    doc.appendChild(ele);
                } else {
                    parent.appendChild(ele);
                }
                readEndTag = true;
                break;
            } else if (eventType == XMLStreamConstants.ATTRIBUTE) {
                String localName = xmlReader.getAttributeLocalName(attIndex);
                String ns = xmlReader.getAttributeNamespace(attIndex);
                String value = xmlReader.getAttributeValue(attIndex);
                ele.setAttributeNS(ns, localName, value);
                attIndex++;
            } else if (eventType == XMLStreamConstants.CHARACTERS) {
                sb = new StringBuffer(xmlReader.getTextLength());
                String text = xmlReader.getText();
                sb.append(text);
            } else if (eventType == XMLStreamConstants.CDATA) {
                sb = new StringBuffer(xmlReader.getTextLength());
                String text = xmlReader.getText();
                sb.append(text);
            }
        }
        if (!readEndTag) {
            throw new XMLStreamException("</" + ele.getLocalName() + "> missed!");
        }
    }

}
