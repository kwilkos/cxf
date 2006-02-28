package org.objectweb.celtix.bus.management.jmx.export.runtime;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;


public class RunTimeModelMBean extends RequiredModelMBean {
    
    public RunTimeModelMBean() throws MBeanException, RuntimeOperationsException  {
        super();
    }
 
    public RunTimeModelMBean(ModelMBeanInfo mbi) throws MBeanException, RuntimeOperationsException  {
        super(mbi);
    }

    public void setManagedResource(Object managedResource, String managedResourceType)
        throws MBeanException, InstanceNotFoundException, InvalidTargetObjectTypeException {
        // check for the managed resource
        super.setManagedResource(managedResource, managedResourceType);
    }
    
    
    public Object invoke(final String opName, final Object[] opArgs, final String[] sig)
        throws MBeanException, ReflectionException {
        return super.invoke(opName, opArgs, sig);
    }
    
    
    public Object getAttribute(final String attrName)
        throws AttributeNotFoundException, MBeanException, ReflectionException {
        return super.getAttribute(attrName);
    }

    public AttributeList getAttributes(String[] attrNames) {
        return super.getAttributes(attrNames);
    }
    
    
    public void setAttribute(Attribute attribute)
        throws AttributeNotFoundException, InvalidAttributeValueException, 
        MBeanException, ReflectionException {
   
        super.setAttribute(attribute);
    }
    
   
    public AttributeList setAttributes(AttributeList attributes) {   
        return super.setAttributes(attributes);
    }

    
    
}
