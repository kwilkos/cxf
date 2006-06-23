package org.objectweb.celtix.datamodel.xml;

import java.io.*;
import javax.xml.namespace.QName;
import org.w3c.dom.*;
import org.objectweb.celtix.helpers.XMLUtils;

public final class XMLMessageFactory {

    private XMLUtils xmlUtils = new XMLUtils();
    
    private XMLMessageFactory() {
    }
    
    public XMLMessage createMessage() {
        try {
            return new XMLMessage();
        } catch (Exception exp) {
            throw new XMLBindingException("creation of XML message failed:", exp);
        }
    }

    public XMLMessage createMessage(InputStream in) throws XMLBindingException {
        XMLMessage message = null;
        try {
            message = new XMLMessage();
            Document doc = xmlUtils.parse(in);
            if (doc != null) {
                message.setRoot(doc);
            }

            buildFaultMessage(message, doc);
        } catch (Exception exp) {
            throw new XMLBindingException("creation or parsing of XML message failed:", exp);
        }
        return message;
    }


    private void buildFaultMessage(XMLMessage message, Document doc) {
        assert doc != null;
        if (doc.hasChildNodes()) {
            doBuild(message, doc);
        }
    }
    
    private void doBuild(XMLMessage message, Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (isFaultNode(child)) {
                addFault(message, child);
            }
            
            if (isFaultStringNode(child)) {
                addFaultString(message, child);
            }

            if (isDetailNode(child)) {
                addDetail(message, child);
            }
            
            if (child.hasChildNodes()) {
                doBuild(message, child);
            }
        }
    }

    private void addFaultString(XMLMessage message, Node child) {
        XMLFault xmlFault = message.getFault();
        if (xmlFault == null) {
            xmlFault = message.addFault();
        }
        xmlFault.setFaultString(child.getTextContent());
    }

    private boolean isFaultStringNode(Node child) {
        QName name = getQNameFromNode(child);
        return XMLConstants.XML_FAULT_STRING.equals(name);
    }

    private void addDetail(XMLMessage message, Node child) {
        XMLFault xmlFault = message.getFault();
        if (xmlFault == null) {
            xmlFault = message.addFault();
        }

        xmlFault.setFaultDetail(child);
    }

    private boolean isDetailNode(Node child) {
        QName name = getQNameFromNode(child);
        return XMLConstants.XML_FAULT_DETAIL.equals(name);
    }
    
    private void addFault(XMLMessage message, Node child) {
        XMLFault xmlFault = new XMLFault();
        xmlFault.setFaultRoot(child);
        message.setFault(xmlFault);
    }
    
    private boolean isFaultNode(Node child) {
        QName name = getQNameFromNode(child);
        return XMLConstants.XML_FAULT_ROOT.equals(name);
    }

    private QName getQNameFromNode(Node node) {
        String localName = node.getLocalName();
        String namespaceURI = node.getNamespaceURI();
        return localName == null ? null : new QName(namespaceURI, localName);
    }

    public static XMLMessageFactory newInstance() {
        return new XMLMessageFactory();
    }
}
