package org.objectweb.celtix.bus.bindings.xml;

import org.w3c.dom.*;
import org.objectweb.celtix.helpers.XMLUtils;

public class XMLFault {

    private Node faultRoot;
    
    private String faultString;
    private Node faultDetail;
    private Node detailRoot;
    
    private XMLUtils xmlUtils = new XMLUtils();
    
    public void setFaultString(String str) {
        this.faultString = str;
    }

    public void addFaultString(String str) {
        assert faultRoot != null;
        
        Text text = xmlUtils.createTextNode(this.faultRoot, str);
        Node faultStringNode = xmlUtils.createElementNS(this.faultRoot, XMLConstants.XML_FAULT_STRING);
        faultStringNode.appendChild(text);
        this.faultRoot.appendChild(faultStringNode);

        this.faultString = str;
    }

    public void setFaultDetail(Node detail) {
        this.detailRoot = detail;
        
        NodeList list = detail.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node entry = list.item(i);
            if (entry.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            this.faultDetail = detail;
        }
    }
    
    public void appendFaultDetail(Node detail) {
        assert faultRoot != null;
        assert detailRoot != null;

        this.detailRoot.appendChild(detail);
        this.faultDetail = detail;
    }

    public Node addFaultDetail() {
        assert faultRoot != null;

        this.detailRoot = xmlUtils.createElementNS(this.faultRoot, XMLConstants.XML_FAULT_DETAIL);
        this.faultRoot.appendChild(this.detailRoot);
        return this.detailRoot;
    }

    public String getFaultString() {
        return this.faultString;
    }

    public Node getFaultDetail() {
        return this.faultDetail;
    }

    public Node getFaultDetailRoot() {
        return this.detailRoot;
    }

    public Node getFaultRoot() {
        return this.faultRoot;
    }

    protected void setFaultRoot(Node root) {
        this.faultRoot = root;
    }

    public void removeChild(Node node) {
        this.faultRoot.removeChild(node);
    }

    public boolean hasChildNodes() {
        return this.faultRoot.hasChildNodes();
    }
}
