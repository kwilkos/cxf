package org.apache.cxf.aegis.type.java5;

public class AnnotatedService
{
    public AnnotatedBean1 getAnnotatedBean1()
    {
        AnnotatedBean1 bean = new AnnotatedBean1();
        bean.setAttributeProperty("attribute");
        bean.setBogusProperty("bogus");
        bean.setElementProperty("element");
        
        return bean;
    }
    
    public AnnotatedBean2 getAnnotatedBean2()
    {
        AnnotatedBean2 bean = new AnnotatedBean2();
        bean.setAttributeProperty("attribute");
        bean.setElementProperty("element");
        
        return bean;
    }
}
