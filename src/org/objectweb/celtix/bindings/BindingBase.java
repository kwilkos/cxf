package org.objectweb.celtix.bindings;

import javax.xml.ws.Binding;

import org.objectweb.celtix.context.ObjectMessageContext;

public interface BindingBase {
    
    boolean isCompatibleWithAddress(String address);
    
    Binding getBinding();
    
    ObjectMessageContext createObjectContext();
}
