package org.apache.cxf.aegis.type.java5;

public class AnnotatedBean2
{
    private String elementProperty;
    private String attributeProperty;
    private String ignoredProperty;
    
    @XmlAttribute(name="attribute")
    public String getAttributeProperty()
    {
        return attributeProperty;
    }
    
    public void setAttributeProperty(String attributeProperty)
    {
        this.attributeProperty = attributeProperty;
    }

    @XmlElement(name="element")
    public String getElementProperty()
    {
        return elementProperty;
    }
    
    public void setElementProperty(String elementProperty)
    {
        this.elementProperty = elementProperty;
    }

    @IgnoreProperty
    public String getIgnoredProperty()
    {
        return ignoredProperty;
    }

    public void setIgnoredProperty(String ignoredProperty)
    {
        this.ignoredProperty = ignoredProperty;
    }
}
