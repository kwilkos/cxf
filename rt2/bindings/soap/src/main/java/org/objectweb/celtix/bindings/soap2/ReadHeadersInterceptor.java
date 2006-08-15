package org.objectweb.celtix.bindings.soap2;

import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.phase.Phase;

public class ReadHeadersInterceptor extends AbstractSoapInterceptor {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ReadHeadersInterceptor.class);

    public ReadHeadersInterceptor() {
        super();
        setPhase(Phase.READ);
    }

    public void handleMessage(SoapMessage message) {
        XMLStreamReader xmlReader = message.getContent(XMLStreamReader.class);

        boolean found = false;
        try {
            if (xmlReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                String ns = xmlReader.getNamespaceURI();
                SoapVersion soapVersion = SoapVersionFactory.getInstance().getSoapVersion(ns);
                message.setVersion(soapVersion);

                if (xmlReader.getLocalName().equals(message.getVersion().getEnvelope().getLocalPart())) {
                    xmlReader.nextTag();
                    if (xmlReader.getLocalName().equals(message.getVersion().getHeader().getLocalPart())) {
                        found = true;
                    }
                }
            }

            if (found) {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = null;
                builder = builderFactory.newDocumentBuilder();
                Document doc = builder.newDocument();
                addHeaderElementIntoDoc(xmlReader, message, doc);
            }
        } catch (XMLStreamException e) {
            throw new SoapFault(new Message("XML_STREAM_EXC", BUNDLE), e, SoapFault.SENDER);
        } catch (ParserConfigurationException e) {
            throw new SoapFault(new Message("PARSER_EXC", BUNDLE), e, SoapFault.SENDER);
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
