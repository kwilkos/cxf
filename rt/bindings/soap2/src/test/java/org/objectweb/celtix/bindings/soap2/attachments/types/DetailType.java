package org.objectweb.celtix.bindings.soap2.attachments.types;

import java.awt.Image;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DetailType", 
         propOrder = {
                      "sName", 
                      "photo", 
                      "sound"
                      }
)
public class DetailType {

    @XmlElement(namespace = "http://celtix.objectweb.org/bindings/soap2/attachments/types", required = true)
    protected String sName;
    @XmlElement(namespace = "http://celtix.objectweb.org/bindings/soap2/attachments/types", required = true)
    protected byte[] sound;
    @XmlElement(namespace = "http://celtix.objectweb.org/bindings/soap2/attachments/types", required = true)
    @XmlMimeType("image/jpeg")
    protected Image photo;

    /**
     * Gets the value of the elName property.
     * 
     * @return possible object is {@link String }
     */
    public String getSName() {
        return sName;
    }

    /**
     * Sets the value of the elName property.
     * 
     * @param value allowed object is {@link String }
     */
    public void setSName(String value) {
        this.sName = value;
    }

    /**
     * Gets the value of the photo property.
     * 
     * @return possible object is byte[]
     */
    public byte[] getSound() {
        return sound;
    }

    /**
     * Sets the value of the photo property.
     * 
     * @param value allowed object is byte[]
     */
    public void setSound(byte[] value) {
        this.sound = (byte[])value;
    }

    /**
     * Gets the value of the image property.
     * 
     * @return possible object is {@link Image }
     */
    public Image getPhoto() {
        return photo;
    }

    /**
     * Sets the value of the image property.
     * 
     * @param value allowed object is {@link Image }
     */
    public void setImage(Image value) {
        this.photo = value;
    }

}
