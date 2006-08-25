
package org.objectweb.hello_world_rpclit.types;

import javax.xml.bind.annotation.AccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for myComplexStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="myComplexStruct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="elem1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="elem2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="elem3" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(AccessType.FIELD)
@XmlType(name = "myComplexStruct", propOrder = {"elem1", "elem2", "elem3" })
public class MyComplexStruct {

    @XmlElement(namespace = "http://objectweb.org/hello_world_rpclit/types")
    protected String elem1;
    @XmlElement(namespace = "http://objectweb.org/hello_world_rpclit/types")
    protected String elem2;
    @XmlElement(namespace = "http://objectweb.org/hello_world_rpclit/types", type = Integer.class)
    protected int elem3;

    /**
     * Gets the value of the elem1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElem1() {
        return elem1;
    }

    /**
     * Sets the value of the elem1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElem1(String value) {
        this.elem1 = value;
    }

    /**
     * Gets the value of the elem2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElem2() {
        return elem2;
    }

    /**
     * Sets the value of the elem2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElem2(String value) {
        this.elem2 = value;
    }

    /**
     * Gets the value of the elem3 property.
     * 
     */
    public int getElem3() {
        return elem3;
    }

    /**
     * Sets the value of the elem3 property.
     * 
     */
    public void setElem3(int value) {
        this.elem3 = value;
    }

}
