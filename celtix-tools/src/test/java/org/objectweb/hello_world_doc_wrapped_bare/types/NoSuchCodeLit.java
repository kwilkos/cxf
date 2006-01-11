
package org.objectweb.hello_world_doc_wrapped_bare.types;

import javax.xml.bind.annotation.AccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NoSuchCodeLit element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="NoSuchCodeLit">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *&lt;element name="code" type="{http://objectweb.org/hello_world_doc_wrapped_bare/types}ErrorCode"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(AccessType.FIELD)
@XmlType(name = "", propOrder = {"code" })
@XmlRootElement(name = "NoSuchCodeLit")
public class NoSuchCodeLit {

    @XmlElement(namespace = "http://objectweb.org/hello_world_doc_wrapped_bare/types")
    protected ErrorCode code;

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link ErrorCode }
     *     
     */
    public ErrorCode getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link ErrorCode }
     *     
     */
    public void setCode(ErrorCode value) {
        this.code = value;
    }

}
