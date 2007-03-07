package org.apache.cxf.aegis.type.java5;

@XmlType(extensibleElements = false, extensibleAttributes = false)
public class AnnotatedBean4
{
    private String nillableProperty = null;

    private String minOccursProperty = null;

    @XmlElement(nillable = false)
    public String getNillableProperty()
    {
        return nillableProperty;
    }

    public void setNillableProperty(String nillableProperty)
    {
        this.nillableProperty = nillableProperty;
    }

    @XmlElement(minOccurs = "1")
    public String getMinOccursProperty()
    {
        return minOccursProperty;
    }

    public void setMinOccursProperty(String minOccursProperty)
    {
        this.minOccursProperty = minOccursProperty;
    }
}
