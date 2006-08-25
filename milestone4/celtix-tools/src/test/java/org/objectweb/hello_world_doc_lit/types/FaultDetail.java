
package org.objectweb.hello_world_doc_lit.types;

import javax.xml.bind.annotation.AccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for faultDetail element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="faultDetail">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="minor" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *           &lt;element name="major" type="{http://www.w3.org/2001/XMLSchema}short"/>
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
@XmlType(name = "", propOrder = {"minor", "major" })
@XmlRootElement(name = "faultDetail")
public class FaultDetail {

    @XmlElement(namespace = "http://objectweb.org/hello_world_doc_lit/types", type = Short.class)
    protected short minor;
    @XmlElement(namespace = "http://objectweb.org/hello_world_doc_lit/types", type = Short.class)
    protected short major;

    /**
     * Gets the value of the minor property.
     * 
     */
    public short getMinor() {
        return minor;
    }

    /**
     * Sets the value of the minor property.
     * 
     */
    public void setMinor(short value) {
        this.minor = value;
    }

    /**
     * Gets the value of the major property.
     * 
     */
    public short getMajor() {
        return major;
    }

    /**
     * Sets the value of the major property.
     * 
     */
    public void setMajor(short value) {
        this.major = value;
    }

}
