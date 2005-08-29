package org.objectweb.celtix.bindings;

import java.net.URL;

import javax.xml.ws.Binding;

public interface BindingBase {
    
    boolean isCompatibleWithAddress(URL address);
    
    Binding getBinding();
}
