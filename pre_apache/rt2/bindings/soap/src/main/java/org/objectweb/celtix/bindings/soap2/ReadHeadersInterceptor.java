package org.objectweb.celtix.bindings.soap2;

import java.io.InputStream;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.staxutils.StaxUtils;

public class ReadHeadersInterceptor extends AbstractSoapInterceptor {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ReadHeadersInterceptor.class);

    public ReadHeadersInterceptor() {
        super();
        setPhase(Phase.READ);
    }

    public void handleMessage(SoapMessage message) {
        XMLStreamReader xmlReader = message.getContent(XMLStreamReader.class);
        if (xmlReader == null) {
            InputStream in = (InputStream)message.getContent(InputStream.class);
            if (in == null) {
                throw new RuntimeException("Can't found input stream in message");
            }
            xmlReader = StaxUtils.createXMLStreamReader(in);
        }
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SoapFault(new Message("PARSER_EXC", BUNDLE), e, SoapFault.SENDER);
        }
        Document doc = builder.newDocument();
        try {
            if (xmlReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                String ns = xmlReader.getNamespaceURI();
                SoapVersion soapVersion = SoapVersionFactory.getInstance().getSoapVersion(ns);
                message.setVersion(soapVersion);
                StaxUtils.readDocElements(doc, xmlReader, true, message.getVersion().getBody());
                Element envelop = (Element)doc.getChildNodes().item(0);
                String header = soapVersion.getHeader().getLocalPart();
                for (int i = 0; i < envelop.getChildNodes().getLength(); i++) {
                    if (envelop.getChildNodes().item(i) instanceof Element) {
                        Element element = (Element)envelop.getChildNodes().item(i);
                        if (element.getLocalName().equals(header)) {
                            message.setHeaders(Element.class, element);
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new SoapFault(new Message("XML_STREAM_EXC", BUNDLE), e, SoapFault.SENDER);
        }
    }

}
