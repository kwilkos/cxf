package org.objectweb.celtix.bindings;

import javax.xml.ws.Binding;

public interface BindingBase {
    
    boolean isCompatibleWithAddress(String address);
    
    Binding getBinding();
}
