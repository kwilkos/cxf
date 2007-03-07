package org.apache.cxf.aegis.type.java5;

public class AnnotatedBean3
{
    private String attributeProperty;

    @XmlAttribute(name="attribute")
    public String getAttributeProperty()
    {
        return attributeProperty;
    }
    
    public void setAttributeProperty(String attributeProperty)
    {
        this.attributeProperty = attributeProperty;
    }
}
