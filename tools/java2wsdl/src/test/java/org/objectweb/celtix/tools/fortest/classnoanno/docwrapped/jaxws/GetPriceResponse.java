
package org.objectweb.celtix.tools.fortest.classnoanno.docwrapped.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getPriceResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getPriceResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="responseType" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"responseType" })
@XmlRootElement(name = "getPriceResponse")
public class GetPriceResponse {

    protected float responseType;

    /**
     * Gets the value of the responseType property.
     * 
     */
    public float getResponseType() {
        return responseType;
    }

    /**
     * Sets the value of the responseType property.
     * 
     */
    public void setResponseType(float value) {
        this.responseType = value;
    }

}
