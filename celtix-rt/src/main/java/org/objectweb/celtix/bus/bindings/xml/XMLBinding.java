package org.objectweb.celtix.bus.bindings.xml;

import java.io.*;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import org.w3c.dom.*;

public class XMLBinding implements ExtensibilityElement, Serializable {

    private QName rootNode;
    private Element element;
    private boolean required;
    private QName elementType;
    private String documentBaseURI;

    public void setDocumentBaseURI(String baseURI) {
        this.documentBaseURI = baseURI;
    }

    public String getDocumentBaseURI() {
        return this.documentBaseURI;
    }

    public QName getRootNode() {
        return this.rootNode;
    }

    public void setRootNode(QName node) {
        this.rootNode = node;
    }

    public void setElement(Element elem) {
        this.element = elem;
    }

    public Element getElement() {
        return element;
    }

    public void setRequired(Boolean r) {
        this.required = r;
    }

    public Boolean getRequired() {
        return required;
    }
    public void setElementType(QName elemType) {
        this.elementType = elemType;
    }

    public QName getElementType() {
        return elementType;
    }

    public String toString() {
        return "ROOT NODE:" + this.rootNode.toString();
    }
}
