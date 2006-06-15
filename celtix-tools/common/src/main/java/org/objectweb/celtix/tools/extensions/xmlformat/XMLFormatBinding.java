package org.objectweb.celtix.tools.extensions.xmlformat;

import java.io.Serializable;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

public class XMLFormatBinding implements ExtensibilityElement, Serializable {

    private boolean required;
    private QName elementType;
    private Element element;
    private String documentBaseURI;
    
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

    public String getDocumentBaseURI() {
        return documentBaseURI;
    }

    public void setDocumentBaseURI(String pDocumentBaseURI) {
        this.documentBaseURI = pDocumentBaseURI;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element pElement) {
        this.element = pElement;
    }

}
