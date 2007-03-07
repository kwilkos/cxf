package org.apache.cxf.aegis.type.java5;

public class AnnotatedBean1
{
    private String elementProperty;
    private String attributeProperty;
    private String bogusProperty;
    
    @XmlAttribute
    public String getAttributeProperty()
    {
        return attributeProperty;
    }
    
    public void setAttributeProperty(String attributeProperty)
    {
        this.attributeProperty = attributeProperty;
    }
    
    public String getBogusProperty()
    {
        return bogusProperty;
    }
    
    public void setBogusProperty(String bogusProperty)
    {
        this.bogusProperty = bogusProperty;
    }
    
    @XmlElement(type=CustomStringType.class)
    public String getElementProperty()
    {
        return elementProperty;
    }
    
    public void setElementProperty(String elementProperty)
    {
        this.elementProperty = elementProperty;
    }
}
