package org.objectweb.celtix.wsdl;


import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

public class TExtensibilityElementImpl
    extends TExtensibilityElement
    implements ExtensibilityElement {

    @XmlTransient()
    QName elementType;

    public QName getElementType() {
        return elementType;
    }
    
    public void setElementType(QName type) {
        elementType = type;
    }

    public Boolean getRequired() {
        return isRequired();
    }

}
