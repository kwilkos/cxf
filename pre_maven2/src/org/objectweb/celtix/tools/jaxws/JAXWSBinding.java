package org.objectweb.celtix.tools.jaxws;

import java.io.*;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import org.w3c.dom.*;

public class JAXWSBinding implements ExtensibilityElement, Serializable {

    private boolean isSetAsyncMapping;
    private boolean enableAsyncMapping;
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

    public boolean isSetAsyncMapping() {
        return this.isSetAsyncMapping;
    }

    public void setAsyncMapping(boolean set) {
        this.isSetAsyncMapping = set;
    }
    
    public boolean isEnableAsyncMapping() {
        return this.enableAsyncMapping;
    }

    public void setEnableAsyncMapping(boolean enableAsync) {
        this.enableAsyncMapping = enableAsync;
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
}
