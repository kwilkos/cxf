package org.objectweb.celtix.tools.extensions.jms;

import java.io.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import org.w3c.dom.*;

public class JMSAddress implements ExtensibilityElement, Serializable {
    private String address;
    private String jndiURL;
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

    public void setAddress(String addr) {
        this.address = addr;
    }

    public String getAddress() {
        return this.address;
    }

    public void setJndiURL(String url) {
        this.jndiURL = url;
    }

    public String getJndiURL() {
        return this.jndiURL;
    }
}
