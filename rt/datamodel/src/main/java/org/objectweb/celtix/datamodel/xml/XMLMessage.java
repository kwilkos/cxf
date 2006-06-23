package org.objectweb.celtix.datamodel.xml;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;

import org.objectweb.celtix.helpers.XMLUtils;

public class XMLMessage {

    private Document root;
    private XMLUtils xmlUtils = new XMLUtils();
    private XMLFault xmlFault;

    public XMLMessage() throws ParserConfigurationException {
        this.root = xmlUtils.newDocument();
    }
    
    public void writeTo(OutputStream out) throws IOException {
        xmlUtils.writeTo(this.root, out);
    }

    public Document getRoot() {
        return this.root;
    }

    public void setRoot(Document r) {
        this.root = r;
    }

    public void appendChild(Node child) {
        if (root != null) {
            root.appendChild(child);
        }
    }

    public boolean hasChildNodes() {
        return this.root.hasChildNodes();
    }

    public void removeContents() {
        xmlUtils.removeContents(this.root);
    }

    public void setFault(XMLFault fault) {
        this.xmlFault = fault;
    }
    
    //  Creates a new XMLFault object and adds it to this XML Message root object.
    public XMLFault addFault() {
        xmlFault = new XMLFault();
        Node faultRoot = xmlUtils.createElementNS(this.root, XMLConstants.XML_FAULT_ROOT);
        appendChild(faultRoot);
        xmlFault.setFaultRoot(faultRoot);
        return xmlFault;
    }
    
    public XMLFault getFault() {
        return this.xmlFault;
    }

    public boolean hasFault() {
        return this.xmlFault != null;
    }

    public String toString() {
        return xmlUtils.toString(this.root);
    }
}
