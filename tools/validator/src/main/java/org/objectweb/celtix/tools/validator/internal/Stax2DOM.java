package org.objectweb.celtix.tools.validator.internal;

import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.WSDLConstants;

public class Stax2DOM {

    private  Element currentElement;
    private  Document doc;
   

    public Document getDocument(XMLEventReader xmlEventReader) throws ToolException {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            throw new ToolException(e1);
        }
        doc = builder.newDocument();

        Element ele = doc.createElement("definitions");
        doc.appendChild(ele);
        currentElement = ele;

        while (xmlEventReader.hasNext()) {
            XMLEvent xmleve = (XMLEvent)xmlEventReader.next();

            if (xmleve.getEventType() == XMLStreamConstants.END_ELEMENT) {

                endElement();

            }

            if (xmleve.getEventType() == XMLStreamConstants.START_ELEMENT) {
                StartElement element = (StartElement)xmleve;
                startElement(element);

            }

        }

        return doc;
    }

    public void startElement(StartElement ele) {
        
        Element element = doc.createElementNS(ele.getName().getNamespaceURI(), ele.getName().getLocalPart());
        Iterator ite = ele.getAttributes();

        while (ite.hasNext()) {
            Attribute attr = (Attribute)ite.next();
            element.setAttribute(attr.getName().getLocalPart(), attr.getValue());
        }
        
        currentElement.appendChild(element);
        currentElement = element;
        element.setUserData(WSDLConstants.NODE_LOCATION, ele.getLocation(), null);

    }

    public void endElement() {
        currentElement = (Element)currentElement.getParentNode();
    }

}
