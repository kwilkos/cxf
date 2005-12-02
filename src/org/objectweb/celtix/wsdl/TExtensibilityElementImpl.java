package org.objectweb.celtix.wsdl;


import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

/**
 * Implements the <code>ExtensibilityElement</code> interface.
 */
public class TExtensibilityElementImpl
    extends TExtensibilityElement
    implements ExtensibilityElement {

    @XmlTransient()
    QName elementType;

    /**
     * Returns the type of this extensibility element.
     * @return QName the type of this element.
     */
    public QName getElementType() {
        return elementType;
    }
    
    /**
     * Sets the type of this extensibility element.
     * @param type QName the type of this element.
     */
    public void setElementType(QName type) {
        elementType = type;
    }

    /**
     * Get whether or not the semantics of this extension are required.
     * Relates to the wsdl:required attribute.
     * @return Boolean
     */
    public Boolean getRequired() {
        return isRequired();
    }
    public void setRequired(Boolean value) {
        this.required = value;
    }

}
