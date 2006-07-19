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

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.staxutils.StaxUtils;

public class ReadHeadersInterceptor extends AbstractPhaseInterceptor {

    private SoapMessage message;
    private XMLStreamReader xmlReader;
    private Document doc;

    public void handleMessage(Message messageParam) {
        // TODO Auto-generated method stub
        this.message = (SoapMessage)messageParam;
        try {
            init();
            process();
        } catch (Exception e) {
            message.put(Message.INBOUND_EXCEPTION, e);
            return;
        }
        // continue interceptor chain processing
        message.getInterceptorChain().doIntercept(message);
    }

    private void init() throws Exception {
        InputStream in = (InputStream)message.getSource(InputStream.class);
        if (in == null) {
            throw new WebServiceException("Missing Soap part input stream in soap message");
        }
        xmlReader = StaxUtils.createXMLStreamReader(in);
        message.setSource(XMLStreamReader.class, xmlReader);
    }

    private void process() throws Exception {
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
                // retrive the header tag under the envelop tag, build a dom
                // node represent it
                createDomDocument();
                addHeaderElementIntoDoc();
            } else {
                throw new SOAPException("Parsing soap message error, "
                                        + "can't find <Soap:Header> in message part!");
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw e;
        }
    }

    private void addHeaderElementIntoDoc() throws XMLStreamException {
        QName name = new QName(xmlReader.getNamespaceURI(), xmlReader.getLocalName());
        Element eleHeaders = doc.createElementNS(name.getNamespaceURI(), name.getLocalPart());
        eleHeaders.setPrefix(message.getVersion().getPrefix());
        processElement(null, eleHeaders);
        message.setHeaders(Element.class, eleHeaders);
    }

    private void processElement(Element parent, Element ele) throws XMLStreamException {
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
                processElement(ele, eleChild);
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

    private void createDomDocument() throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        builder = builderFactory.newDocumentBuilder();
        doc = builder.newDocument();
    }
}
