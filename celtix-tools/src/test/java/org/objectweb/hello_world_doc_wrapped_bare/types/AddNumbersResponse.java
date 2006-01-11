
package org.objectweb.hello_world_doc_wrapped_bare.types;

import javax.xml.bind.annotation.AccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for addNumbersResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="addNumbersResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(AccessType.FIELD)
@XmlType(name = "addNumbersResponse", propOrder = { "_return" })
public class AddNumbersResponse {

    @XmlElement(name = "return", 
                namespace = "http://objectweb.org/hello_world_doc_wrapped_bare/types", type = Integer.class)
    protected int res;

    /**
     * Gets the value of the return property.
     * 
     */
    public int getReturn() {
        return res;
    }

    /**
     * Sets the value of the return property.
     * 
     */
    public void setReturn(int value) {
        this.res = value;
    }

}
